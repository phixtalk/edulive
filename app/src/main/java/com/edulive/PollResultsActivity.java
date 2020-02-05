package com.edulive;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.edulive.adapter.PollsAdapter;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;
import com.edulive.helpers.DynamicListScroll;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollResultsActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SessionManager session;
    private ProgressDialog pDialog;

    TextView refresh;
    String userid, pollid,question,option1,option2,option3;
    private View mProgressView;
    TextView questionView, optiona, optionb, optionc;

    private ListView listView;
    private PollsAdapter listAdapter;
    private List<FeedItem> feedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_results);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Session Manager
        session = new SessionManager(getApplicationContext());
        //mStatusText = (EditText) findViewById(R.id.status_title);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        refresh = (TextView) findViewById( R.id.refresh );
        mProgressView = findViewById(R.id.progress_overlay);

        //get session values
        HashMap<String, String> userz = session.getUserDetails();
        userid = userz.get(SessionManager.KEY_TOKEN);
        //get values passed on from previous page
        Bundle recdData = getIntent().getExtras();
        pollid = recdData.getString("pollid");
        question = recdData.getString("question");
        option1 = recdData.getString("option1");
        option2 = recdData.getString("option2");
        option3 = recdData.getString("option3");

        String count_a = recdData.getString("count_a");
        String count_b = recdData.getString("count_b");
        String count_c = recdData.getString("count_c");

        //initialize the textview variables
        questionView = (TextView) findViewById(R.id.question);
        optiona = (TextView) findViewById(R.id.option1);
        optionb = (TextView) findViewById(R.id.option2);
        optionc = (TextView) findViewById(R.id.option3);
        //set the values for textviews
        questionView.setText(question);
        optiona.setText(option1+": "+count_a+" votes");
        optionb.setText(option2+": "+count_b+" votes");
        optionc.setText(option3+": "+count_c+" votes");

        //next load all members that voted
        listView = (ListView) findViewById(R.id.list);
        feedItems = new ArrayList<FeedItem>();

        //listAdapter = new CustomerReviewAdapter(this, feedItems);
        listAdapter = new PollsAdapter(this, feedItems, "results");
        listView.setAdapter(listAdapter);


        refresh.setVisibility(View.GONE);
        getVoters();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.GONE);
                getVoters();
            }
        });
    }

    private void getVoters() {
        try{
            showProgress(true);
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("pollid",pollid);
            String URL_FEED = getString(R.string.base_url).concat("api/pollvoters");
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED,
                    new JSONObject(postParam), new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    showProgress(false);
                    try{
                        if (response != null) {
                            try {
                                JSONArray feedArray = response.getJSONArray("results");
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    for (int i = 0; i < feedArray.length(); i++) {
                                        JSONObject feedObj = (JSONObject) feedArray.get(i);
                                        FeedItem item = new FeedItem();
                                        item.setId(feedObj.getString("id"));
                                        item.setName(feedObj.getString("member"));
                                        item.setDescription(feedObj.getString("vote"));
                                        item.setDateJoined(feedObj.getString("timestamp"));
                                        feedItems.add(item);
                                    }
                                    // notify data changes to list adapater
                                    listAdapter.notifyDataSetChanged();
                                    DynamicListScroll.getListViewSize(listView);
                                }else if(status.equals("empty")){
                                    Toast.makeText(getApplicationContext(),R.string.no_records,Toast.LENGTH_LONG).show();
                                }
                            }catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }catch (Exception e){
                        //System.out.println("show_error"+e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    showProgress(false);
                    refresh.setVisibility(View.VISIBLE);
                    // Toast.makeText(getApplicationContext(), R.string.no_connection,Toast.LENGTH_LONG).show();
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);

        } catch (Exception e) {
            System.out.println("show_error"+e.getMessage());
            showProgress(false);
            refresh.setVisibility(View.VISIBLE);
            //Toast.makeText(getApplicationContext(),R.string.error_msg,Toast.LENGTH_LONG).show();
        }
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    //make back button work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
