package edu.lehigh.csb311.motus_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
/**
 * Created by Sami on 11/28/17.
*/
public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

        /** Shared Preferences */
        SharedPreferences pref;

        /** Editor for the Shared Preferences */
        Editor editor;

        /** Shared Preferences */
        Context context;

        /** Shared Pref Mode */
        int PRIVATE_MODE = 0;

        /** Shared preferences file name */
        private static final String PREF_NAME = "MotusLogin";

        /** All Shared Preferences Keys */
        private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

        /** Username */
        public static final String KEY_USERNAME = "username";

        /** Constructor */
        public SessionManager(Context nContext) {
                this.context = nContext;
                pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
                editor = pref.edit();
        }


        public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

            if (!isLoggedIn){
                editor.clear();
            }
            // commit changes
            editor.commit();

            Log.d(TAG, "User login session modified!");
        }

            public void createLoginSession(String username){
                editor.putString(KEY_USERNAME, username);
                // commit changes
                editor.commit();
            }


            public boolean isLoggedIn(){
                return pref.getBoolean(KEY_IS_LOGGEDIN, false);
            }
}
