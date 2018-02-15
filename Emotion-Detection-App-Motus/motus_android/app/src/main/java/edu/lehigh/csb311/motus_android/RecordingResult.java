package edu.lehigh.csb311.motus_android;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sami on 10/18/17.
 */

public class RecordingResult implements Parcelable {

    private String TAG = RecordingResult.class.getSimpleName();
    private int index;

    protected Date recordedAt;

    protected String sentiment;

    protected String fileName;

    protected String transcript;

    protected int score;

    public RecordingResult(String nFileName, int nScore, String nTranscript, String nSentiment){
        this.fileName = nFileName;
        this.score = nScore;
        this.sentiment = nSentiment;
        this.recordedAt = getRecordingDate(nFileName);
        this.transcript = nTranscript;

    }

    private Date getRecordingDate(String nFileName) {
        Date date;
        String dateStr = nFileName.substring(nFileName.length()-25, nFileName.length()-8);
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH);
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    /**
     * Parcelable interface functions
     */
    protected RecordingResult(Parcel in) {
        index = in.readInt();
        fileName = in.readString();
        long tmpRecordedDate = in.readLong();
        recordedAt = tmpRecordedDate != -1 ? new Date(tmpRecordedDate) : null;
        score = in.readInt();
        transcript = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RecordingResult> CREATOR = new Parcelable.Creator<RecordingResult>() {
        @Override
        public RecordingResult createFromParcel(Parcel in) {
            return new RecordingResult(in);
        }

        @Override
        public RecordingResult[] newArray(int size) {
            return new RecordingResult[size];
        }
    };
}
