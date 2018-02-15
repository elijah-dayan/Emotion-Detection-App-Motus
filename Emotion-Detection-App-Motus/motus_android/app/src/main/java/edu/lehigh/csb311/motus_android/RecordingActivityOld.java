package edu.lehigh.csb311.motus_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordingActivityOld extends AppCompatActivity {

    private static final String LOG_TAG = "Recording Activity ";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private Button   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;

    private Button mSendAudioButton = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        Log.d(LOG_TAG, "Create test1");

        // Initializes TransferUtility, always do this before using it.
        transferUtility = Util.getTransferUtility(this);

        Date createdTime = new Date();
        String createdTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(createdTime);
        //mFileName = "/mnt/sdcard/" + createdTimeStr + "_rec.3gp";
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + createdTimeStr + "_rec.3gp";
        Log.d(LOG_TAG, mFileName);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //mRecordButton = (ImageButton) findViewById(R.id.recordBtn2);
        //mPlayButton = (ImageButton) findViewById(R.id.playBtn2);
        mSendAudioButton = (Button) findViewById(R.id.sendBtn2);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RECORDING", "Pressed the Record Button");
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop Recording");
                } else {
                    mRecordButton.setText("Start Recording");
                }
                mStartRecording = !mStartRecording;
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setText("Stop Playing");
                } else {
                    mPlayButton.setText("Start Playing");
                }
                mStartPlaying= !mStartPlaying;
            }
        });

        mSendAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send audio file to S3
                Log.d(LOG_TAG, "Prepare sending to s3");
                File file = new File(mFileName);

                TransferObserver observer = transferUtility.upload("csb-motus", "recordings/"+file.getName(), file);

                observer.setTransferListener(new UploadListener());

                Log.d(LOG_TAG, "Transferring");

                /**
                 * Attempt to upload byte string onto a server
                 */

                //converting audio to base64 string

                /**
                 * Transition to Results Activity
                 */
                Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
             //   intent.putExtra("FILE_NAME", temp);
                startActivity(intent);
                finish();
            }
        });
    }



    private void onRecord(boolean start) {
        Log.d("RECORDING ","Started Recording");
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);



        mRecorder.setOutputFile(mFileName);
        Log.d(LOG_TAG, mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            e.printStackTrace();
        }


    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
       // mRecorder = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
         //   mRecorder.reset();
            mRecorder.release();
            //mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


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
      //  updateList();
    }

    @Override
    public void onStateChanged(int id, TransferState newState) {
        Log.d(LOG_TAG, "onStateChanged: " + id + ", " + newState);
      //  updateList();
    }
}
}