package com.edulive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.edulive.adapter.MembersAdapter;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MembersActivity extends AppCompatActivity {

    //declare session and requestQueue variable
    RequestQueue requestQueue;
    SessionManager session;

    private ListView listView;
    private MembersAdapter listAdapter;
    private List<FeedItem> feedItems;
    private View mProgressView;
    FeedItem item;
    // Search EditText
    EditText inputSearch;
    TextView refresh;
    String userid, viewtype, courseid, usertype, coursename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Session Manager
        session = new SessionManager(getApplicationContext());
        //mStatusText = (EditText) findViewById(R.id.status_title);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mProgressView = findViewById(R.id.progress_overlay);
        refresh = (TextView) findViewById( R.id.refresh );

        HashMap<String, String> userz = session.getUserDetails();
        userid = userz.get(SessionManager.KEY_TOKEN);

        Bundle recdData = getIntent().getExtras();
        viewtype = recdData.getString("viewtype");
        usertype = recdData.getString("usertype");
        courseid = recdData.getString("courseid");
        coursename = recdData.getString("coursename");
        if(viewtype.equals("active")){
            setTitle("Active Members");
        }else{
            setTitle("Pending Members");
        }
        //setup the list view
        listView = (ListView) findViewById(R.id.list);

        feedItems = new ArrayList<FeedItem>();
        listAdapter = new MembersAdapter(this, feedItems, viewtype, usertype, coursename);
        listView.setAdapter(listAdapter);

        refresh.setVisibility(View.GONE);
        loadAllMembers();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.GONE);
                loadAllMembers();
            }
        });

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.setHint("Search Members");
        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the text
                MembersActivity.this.listAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });
    }

    private void loadAllMembers() {
        try{
            showProgress(true);
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("userid",userid);
            postParam.put("courseid",courseid);
            postParam.put("viewtype",viewtype);

            String URL_FEED = getString(R.string.base_url).concat("api/members");
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
                                        item.setId(feedObj.getString("cid"));
                                        item.setUserId(feedObj.getString("uid"));
                                        item.setName(feedObj.getString("lastname")+" "+feedObj.getString("firstname"));
                                        item.setEmail(feedObj.getString("email"));
                                        item.setDateJoined(feedObj.getString("date_joined"));
                                        feedItems.add(item);
                                    }
                                    // notify data changes to list adapater
                                    listAdapter.notifyDataSetChanged();
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
                //Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                //startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
