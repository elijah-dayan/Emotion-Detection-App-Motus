package edu.lehigh.csb311.motus_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.EditText;
import android.view.View;
import android.util.Log;
import android.widget.*;
import android.graphics.*;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{
    private GoogleApiClient mGoogleApiClient;
    SessionManager session;
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    Button login;

    TextView user, password, guest;

    Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        session = new SessionManager(getApplicationContext());
        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
        // User is already logged in. Take him to main activity
            Log.d("LOGIN", "Already logged in, redirecting to RecordingActivity");
            Intent intent = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        user = (TextView) findViewById(R.id.user);
        password = (TextView) findViewById(R.id.password);
        guest = (TextView) findViewById(R.id.guestTxt);


        user = (TextView) findViewById(R.id.user);

        login = (Button) findViewById(R.id.loginButton);

        tf = Typeface.createFromAsset(getAssets(), "BPreplay.otf");

        user.setTypeface(tf);

        password.setTypeface(tf);
        login.setTypeface(tf);
        guest.setTypeface(tf);

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), RecordingActivity.class);
                startActivity(intent);
            }
        });

        login.setTypeface(tf);




        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText userNameText = (EditText) findViewById(R.id.usernameTxt);
                String username = userNameText.getText().toString();
                signInWithUsername(username);
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


    }

    /**
     * Sign in function using only user name (no auth)
     * @param username - the username entered in textfield
     */
    private void signInWithUsername(String username){
        if (username.trim().length() > 0){
            session.setLogin(true);
            session.createLoginSession(username);
            Intent intent = new Intent(this, RecordingActivity.class);
            startActivity(intent);
            finish();
        } else {
            // empty username - do not go to next activity
            Toast.makeText(getApplicationContext(), "Please enter username" , Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sign in function with Google (not in use due to permissions)
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Result from Google Sign in (not in use)
     * @param result authenticated user info/ unauthenticated user
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    /**
     * Update the UI of the Google Sign in button (not in use)
     * @param signedIn flag of sign in status
     */
    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);

        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;

        }
    }

}

