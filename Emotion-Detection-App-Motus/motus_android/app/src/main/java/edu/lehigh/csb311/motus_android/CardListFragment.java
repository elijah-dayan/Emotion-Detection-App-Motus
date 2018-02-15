package edu.lehigh.csb311.motus_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sami on 10/18/17.
 */

public class CardListFragment extends Fragment {
    private static final String TAG = CardListFragment.class.getSimpleName();

    SwipeRefreshLayout mSwipeRefreshLayout;

    // keys for data in the bundle
    private static final String KEY_DATA_LIST = "data_list";
    ArrayList<RecordingResult> mData = new ArrayList<>();
    CardListAdapter mAdapter;
    GridLayoutManager mLayoutManager;
    RecyclerView rView;

    private SessionManager session;
    private String username;

    /**
     * Static method to create a new instance of CardListFragment (Singleton pattern)
     */
    public static CardListFragment newInstance(ArrayList<RecordingResult> dataset) {
        // setting bundle as argument for the created CardListFragment
        final Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_DATA_LIST, dataset);
        final CardListFragment fragment = new CardListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Retrieve the item list from the bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "On Create CardListFragment");
        super.onCreate(savedInstanceState);
        // session manager
        session = new SessionManager(getActivity());
        this.username = session.pref.getString(SessionManager.KEY_USERNAME,null);
        mData = getArguments().getParcelableArrayList(KEY_DATA_LIST);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.result_card_list, container, false);
        final Activity activity = getActivity();
        rView = (RecyclerView) view.findViewById(R.id.results_recycler_view);
        int numberOfItemsPerRow = 1;

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        mAdapter = new CardListAdapter(activity);
        rView.setAdapter(mAdapter);
        mLayoutManager = new GridLayoutManager(activity, numberOfItemsPerRow);
        rView.setLayoutManager(mLayoutManager);
        return view;
    }

    public void fetchTimelineAsync() {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.


        //get the list of results from server
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, AppConstants.getRecordingsUrl(username), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            ArrayList<RecordingResult> datasetTemp = new ArrayList<>();
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
                                datasetTemp.add(recordingResult);
                            }
                            mAdapter.clear();
                            mAdapter.addAll(datasetTemp);
                            // Now we call setRefreshing(false) to signal refresh has finished
                            mSwipeRefreshLayout.setRefreshing(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Fetch timeline error: " + error.toString());
                        error.printStackTrace();

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });

        // Access the RequestQueue through your singleton class.
        MyRequestQueue.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "On Destroy View");
        super.onDestroyView();
        this.getArguments().clear();
    }

    /**
     * A Adapter class for coordinating data between the view and the model
     */
    class CardListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LayoutInflater mLayoutInflater;
        private Context aContext;

        public CardListAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            this.aContext = context;
        }

        // Clean all elements of the recycler
        public void clear() {
            mData.clear();
            notifyDataSetChanged();
        }

        // Add a list of items -- change to type used
        public void addAll(ArrayList<RecordingResult> list) {
            mData.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            Log.d(TAG, "On CreateVIEWHOLDER");
            return new ViewHolder(mLayoutInflater.inflate(R.layout.card_list_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            // set data to each card
            viewHolder.setData(mData.get(position));

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    /**
     * View Holder class for this Card List Adapter Recycler view
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView vFileName;
        private TextView vScore;
        private TextView vTranscript;
        private CardView vCardView;

        /**
         * Constructor for this ViewHolder
         */
        private ViewHolder(View itemView) {
            super(itemView);
            vFileName = (TextView) itemView.findViewById(R.id.info_filename);
            vScore = (TextView) itemView.findViewById(R.id.info_score);
            vTranscript = (TextView) itemView.findViewById(R.id.info_transcript);
        }


        private void setData(RecordingResult res) {
            vFileName.setText("File Name: " + res.fileName);
            vScore.setText("Score: " + Integer.toString(res.score) + " - " + res.sentiment);
            vTranscript.setText("Transcript: \"" + res.transcript + "\"");

            // switch color based on positive/ negative result
            switch(res.sentiment.toLowerCase()){
                case "very negative":
                    this.itemView.setBackgroundColor(Color.parseColor("#FF8A65"));
                    break;
                case "negative":
                    this.itemView.setBackgroundColor(Color.parseColor("#FFE082"));
                    break;
                case "neutral":
                    this.itemView.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    break;
                case "positive":
                    this.itemView.setBackgroundColor(Color.parseColor("#AED581"));
                    break;
                case "very positive":
                    this.itemView.setBackgroundColor(Color.parseColor("#81C784"));
                    break;
            }

        }

    }


}
