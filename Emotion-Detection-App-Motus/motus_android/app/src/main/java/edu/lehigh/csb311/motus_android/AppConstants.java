package edu.lehigh.csb311.motus_android;

/**
 * Created by Sami on 4/23/17.
 */

public class AppConstants {

    public final static String ROOT_URL = "https://wv4hnoho4f.execute-api.us-west-2.amazonaws.com/prod";

    public final static  String getRecordingsUrl(String username){
        return String.format("%s/recording/%s",ROOT_URL,username);
    }

    public final static String getGraphUrl(String username){
        return String.format("https://3p1jz36gua.execute-api.us-west-2.amazonaws.com/prod/graph/%s",username);
    }

    public final static String getUserUrl(int username){
        return String.format("%s/user/%s",ROOT_URL,username);
    }

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "CHANGE_ME";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "CHANGE_ME";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "csb-motus";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-west-2";

}
