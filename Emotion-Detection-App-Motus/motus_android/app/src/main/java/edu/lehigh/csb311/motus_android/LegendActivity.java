package edu.lehigh.csb311.motus_android;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;

public class LegendActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    Typeface tf;
    TextView t, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12;
    String username;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legend);

        // session manager
        session = new SessionManager(getApplicationContext());
        this.username = session.pref.getString(SessionManager.KEY_USERNAME,null);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tf = Typeface.createFromAsset(getAssets(), "BPreplay.otf");
        t = (TextView)findViewById(R.id.textView);
        t3 = (TextView)findViewById(R.id.textView3);
        t4 = (TextView)findViewById(R.id.textView4);
        t5 = (TextView)findViewById(R.id.textView5);
        t6 = (TextView)findViewById(R.id.textView6);
        t7 = (TextView)findViewById(R.id.textView7);
        t8 = (TextView)findViewById(R.id.textView8);
        t9 = (TextView)findViewById(R.id.textView9);
        t10 = (TextView)findViewById(R.id.textView10);
        t11 = (TextView)findViewById(R.id.textView11);
        t12 = (TextView)findViewById(R.id.textView12);
        t.setTypeface(tf);
        t3.setTypeface(tf);
        t4.setTypeface(tf);
        t5.setTypeface(tf);
        t6.setTypeface(tf);
        t7.setTypeface(tf);
        t8.setTypeface(tf);
        t9.setTypeface(tf);
        t10.setTypeface(tf);
        t11.setTypeface(tf);
        t12.setTypeface(tf);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
}
