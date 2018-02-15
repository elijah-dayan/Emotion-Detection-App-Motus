package edu.lehigh.csb311.motus_android;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.*;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private static final String LOG_TAG = "Recording Activity";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static String mFileName = null;
    private TransferUtility transferUtility;

    private SessionManager session;
    private String  username = "";

    private boolean mStartRecording = false;

    private Button mSendAudioButton = null; //ImageButton
    //private TextView mRecordingStatus = null;

    // Requesting permission to RECORD_AUDIO

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private Button b;

    //timer
    TextView timerTextView;
    Button start;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;


    int Seconds, Minutes, MilliSeconds ;

    //timer

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        // session manager
        session = new SessionManager(getApplicationContext());
        this.username = session.pref.getString(SessionManager.KEY_USERNAME,null);
        if (!session.isLoggedIn()) {
            session.setLogin(false);
            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent2);
            finish();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //timer
        timerTextView = (TextView) findViewById(R.id.timerTextView);

        b = (Button) findViewById(R.id.recordBtn2);
        b.setText("START RECORDING");

        mSendAudioButton = (Button) findViewById(R.id.sendBtn2);


        // Initializes TransferUtility, always do this before using it.
        transferUtility = Util.getTransferUtility(this);

        Date createdTime = new Date();
        String createdTimeStr = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(createdTime);

        mSendAudioButton = (Button) findViewById(R.id.sendBtn2); //imagebutton


        mSendAudioButton = (Button) findViewById(R.id.sendBtn2);
        //mRecordingStatus = (TextView) findViewById(R.id.recordingStatus);


        setButtonHandlers();
        enableButtons(false);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        mSendAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send audio file to S3
                Log.d(LOG_TAG, "Prepare sending to s3");
                File file = new File(mFileName);

                TransferObserver observer = transferUtility.upload("csb-motus", "recordings/"+file.getName(), file);

                observer.setTransferListener(new RecordingActivity.UploadListener());

                Log.d(LOG_TAG, "Transferring");

                /**
                 * Attempt to upload byte string onto a server
                 */

                //converting audio to base64 string

                /**
                 * Transition to Results Activity
                 */
                Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.i("message: ", "in onNavigationItemsSelected");
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String sId = Integer.toString(id);
        Log.i("id: ", sId);
        if (id == R.id.nav_recording) {
            Intent intent1 = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(intent1);
            finish();
            return true;

        } else if (id == R.id.nav_sign_out) {
            Log.i("button click", " SIGN OUT");
            session.setLogin(false);
            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent2);
            finish();
            return true;

        } else if(id == R.id.nav_analysis){
            Log.i("button click", " ANALYSIS");
            Intent intent3 = new Intent(getApplicationContext(), ResultsActivity.class);
            startActivity(intent3);
            finish();
            return true;

        } else if(id == R.id.nav_legend){
            Log.i("button click", " LEGEND");
            Intent intent4 = new Intent(getApplicationContext(), LegendActivity.class);
            startActivity(intent4);
            finish();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intentRecord = new Intent(getApplicationContext(), RecordingActivity.class);
        startActivity(intentRecord);
        finish();
    }

    private void setButtonHandlers() {
        Log.i(LOG_TAG, "in setButtonHandlers");
        ((Button) findViewById(R.id.recordBtn2)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.recordBtn2, !isRecording);
        //enableButton(R.id.stopBtn, isRecording);
    }

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {
        System.out.println("in Start Recording");
        //mRecordingStatus.setText("Recording...");
        b.setText("STOP RECORDING");
        StartTime = SystemClock.uptimeMillis();
        System.out.print("Start Time: " + StartTime);

        //mRecordingStatus.setTypeface(tf);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();// starts recording
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();

        mStartRecording = true;
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        Date createdTime = new Date();
        String createdTimeStr = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(createdTime);
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + username + createdTimeStr + "_rec.pcm";
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + username + createdTimeStr + "_rec.wav";


        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("after try catch file output");

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            System.out.println("Short writing to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Translate to WAV
        byte buffer[] = null;
        int TOTAL_SIZE = 0;
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }
        TOTAL_SIZE = (int) file.length();

        WaveHeader header = new WaveHeader(TOTAL_SIZE);


        byte[] h = null;
        try {
            h = header.getHeader();
        } catch (IOException e1) {
            Log.e("PcmToWav", e1.getMessage());
            return;
        }

        if (h.length != 44)
            return;


        File destfile = new File(mFileName);
        if (destfile.exists())
            destfile.delete();


        try {
            buffer = new byte[1024 * 4]; // Length of All Files, Total Size
            InputStream inStream = null;
            OutputStream ouStream = null;

            ouStream = new BufferedOutputStream(new FileOutputStream(mFileName));
            ouStream.write(h, 0, h.length);
            inStream = new BufferedInputStream(new FileInputStream(file));
            int size = inStream.read(buffer);
            while (size != -1) {
                ouStream.write(buffer);
                size = inStream.read(buffer);
            }
            inStream.close();
            ouStream.close();
        } catch (FileNotFoundException e) {
            Log.e("PcmToWav", e.getMessage());
            return;
        } catch (IOException ioe) {
            Log.e("PcmToWav", ioe.getMessage());
            return;
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        // stops the recording activity
        //cdt.cancel();
        //mRecordingStatus.setText("Recording Stopped...");
        TimeBuff += MillisecondTime;
        b.setText("START RECORDING");

        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        mStartRecording = false;
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(LOG_TAG, "recording button clicked; start recording button = " + mStartRecording);
            if(mStartRecording){
                //enableButtons(false);
                stopRecording();
            }
            else {
                Log.i(LOG_TAG, "PLEASE WORK");
                //enableButtons(true);
                startRecording();
            }
            //break;
        }
    };

    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(LOG_TAG, "Error during upload: " + id, e);
            // updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(LOG_TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(LOG_TAG, "onStateChanged: " + id + ", " + newState);
        }

    }


}
