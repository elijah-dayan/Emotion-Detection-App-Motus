package edu.lehigh.csb311.motus_android;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    public final static String TAG = ResultsActivity.class.getSimpleName();
    CardListFragment cardListFragment;
    ArrayList<RecordingResult> dataset;
    String username;
    private SessionManager session;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // session manager
        session = new SessionManager(getApplicationContext());
        this.username = session.pref.getString(SessionManager.KEY_USERNAME,null);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dataset = new ArrayList<>();

        //get the list of results from server
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, AppConstants.getRecordingsUrl(username) , null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("Recordings");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject res = results.getJSONObject(i);
                                int score = res.getInt("score");
                                String sentiment = "";
                                if (score >= 0 && score <=20) {
                                    sentiment = "Very Negative";
                                } else if (score >= 21 && score <=40){
                                    sentiment = "Negative";
                                } else if (score >= 41 && score <=60) {
                                    sentiment = "Neutral";
                                } else if (score >= 61 && score <=80) {
                                    sentiment = "Positive";
                                } else {
                                    sentiment = "Very Positive";
                                }
                                String fileName = res.getString("filename");
                                String transcript = res.getString("transcript");
                                RecordingResult recordingResult = new RecordingResult(fileName, score, transcript, sentiment);
                                dataset.add(recordingResult);
                            }
                            cardListFragment = CardListFragment.newInstance(dataset);


                            if (savedInstanceState == null) {
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.root_layout, cardListFragment, "cardItemList")
                                        .commit();
                            }
                            setGraph();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error!!!!");
                        error.printStackTrace();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MyRequestQueue.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Function for setting the graph data points based on result sent from server
     */
    public void setGraph(){
        //get the list of results from server
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, AppConstants.getGraphUrl(username) , null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray("data");
                            String minDateStr = response.getString("min");
                            String maxDateStr = response.getString("max");


                            //put it in the graph
                            //get graph
                            GraphView graph = (GraphView) findViewById(R.id.graph);

                            // first series is a line
                            DataPoint[] points = new DataPoint[data.length()];
                            JSONObject element;
                            for (int i = 0; i < points.length; i++) {
                                element = data.getJSONObject(i);
                                String date = (String) element.keys().next();
                                Date d = Util.getDate(date);
                                Log.d(TAG, "Date at index " + i  + " is " + date);
                                double average = element.getDouble(date);
                                Log.d(TAG, "Average at index " + i  + " is " + average);


                                points[i] = new DataPoint(d, average);

                                Log.d(TAG, "x = " + points[i].getX() + ", y = " + points[i].getY() );
                            }
                            Log.d(TAG, "Created graph with " + points.length + " data points");
                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                            series.setDrawDataPoints(true);
                            series.setDataPointsRadius(8);
                            // set manual X bounds

                            graph.getViewport().setMinX(Util.getDate(minDateStr).getTime());
                            graph.getViewport().setMaxX(Util.getDate(maxDateStr).getTime());
                            graph.getViewport().setXAxisBoundsManual(true);

                            // as we use dates as labels, the human rounding to nice readable numbers
                            // is not necessary
                            graph.getGridLabelRenderer().setHumanRounding(false);

//
                            graph.addSeries(series);
                            // set date label formatter
                            DateFormat format = new SimpleDateFormat("MM/dd", Locale.ENGLISH);
                            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext(), format));
                            graph.getGridLabelRenderer().setNumHorizontalLabels(Util.daysBetween(Util.getDate(minDateStr), Util.getDate(maxDateStr))/2);
                            graph.setVisibility(View.VISIBLE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error!!!!");
                        error.printStackTrace();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MyRequestQueue.getInstance(this).addToRequestQueue(jsObjRequest);
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


}

