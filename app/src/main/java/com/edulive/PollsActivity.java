package com.edulive;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.edulive.adapter.PollsAdapter;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollsActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SessionManager session;
    private ProgressDialog pDialog;
    private ListView listView;
    private PollsAdapter listAdapter;
    private List<FeedItem> feedItems;
    private View mProgressView;
    private MenuItem item_add_announcement;
    FeedItem item;
    // Search EditText
    EditText inputSearch;
    TextView refresh;
    String userid, viewtype, courseid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polls);

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
        courseid = recdData.getString("courseid");

        if(viewtype.equals("owner")){
        }else{
        }
        //setup the list view
        listView = (ListView) findViewById(R.id.list);

        feedItems = new ArrayList<FeedItem>();
        listAdapter = new PollsAdapter(this, feedItems, viewtype);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                item = feedItems.get(position);
                Intent intent = new Intent(PollsActivity.this, PollVoteActivity.class);
                intent.putExtra("pollid", item.getId());
                intent.putExtra("question", item.getName());
                intent.putExtra("option1", item.getDescription());
                intent.putExtra("option2", item.getPostTags());
                intent.putExtra("option3", item.getBodyContent());
                intent.putExtra("courseid",courseid);
                startActivity(intent);
            }
        });

        refresh.setVisibility(View.GONE);
        loadAllPolls();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.GONE);
                loadAllPolls();
            }
        });

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.setHint("Search Polls");
        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the text
                PollsActivity.this.listAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });

    }

    private void loadAllPolls() {
        try{
            showProgress(true);
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("userid",userid);
            postParam.put("courseid",courseid);
            postParam.put("viewtype",viewtype);

            String URL_FEED = getString(R.string.base_url).concat("api/polls");
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
                                        item.setName(feedObj.getString("question"));
                                        item.setDescription(feedObj.getString("option_1"));
                                        item.setPostTags(feedObj.getString("option_2"));
                                        item.setBodyContent(feedObj.getString("option_3"));
                                        item.setDateJoined(feedObj.getString("timestamp"));
                                        //this methods holds the vote summary
                                        item.setCountHype(feedObj.getString("count_a"));
                                        item.setCountCast(feedObj.getString("count_b"));
                                        item.setCountComments(feedObj.getString("count_c"));

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
    private void callAlert(String title, String message, String  action) {
        UtilityClass.messageAlertBox(title, message, action,this);
    }
    private void createNewPoll(String courseid, String coursename, String option1, String option2, String option3) {
        pDialog = ProgressDialog.show(PollsActivity.this, "", "Please wait...", true);
        try {
            String link = getString(R.string.base_url).concat("api/pollaction");//load remote url
            Map<String, String> postParam = new HashMap<String, String>();
            postParam.put("courseid", courseid);
            postParam.put("coursename", coursename);
            postParam.put("option1", option1);
            postParam.put("option2",option2);
            postParam.put("option3",option3);
            postParam.put("action","newpoll");
            postParam.put("userid", userid);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.cancel();//stop loading dialog box
                            try {
                                String status = response.getString("status");
                                if (status.equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Poll was added successfully.", Toast.LENGTH_LONG).show();
                                    finish();
                                } else if (status.equals("failure")) {
                                    callAlert("Action Failed.", "Something went wrong. Please try later.", "failure");
                                }
                            } catch (Exception e) {
                                callAlert("Error Occured.", e.getLocalizedMessage().toString(), "error");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.cancel();//stop loading dialog box
                    System.out.println("show_error_" + error.getLocalizedMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            // Adding request to request queue
            requestQueue.add(jsonObjReq);
        }catch (Exception ex) {
            System.out.println("show_error_" + ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.announcement, menu);
        item_add_announcement = menu.findItem(R.id.action_add);
        if(viewtype.equals("owner")){
            item_add_announcement.setVisible(true);
        }else{
            item_add_announcement.setVisible(false);
        }
        return true;
    }

    private void addPoll(View view) {
        // Set an EditText view to get user input
        final EditText name = new EditText(PollsActivity.this);
        final EditText option1 = new EditText(PollsActivity.this);
        final EditText option2 = new EditText(PollsActivity.this);
        final EditText option3 = new EditText(PollsActivity.this);


        name.setHint("Your Question");
        option1.setHint("option 1");
        option2.setHint("option 2");
        option3.setHint("option 3");

        // Set edit texts to be one line
        name.setLines(1);
        option1.setLines(1);
        option2.setLines(1);
        option3.setLines(1);

        // Create the label
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(option1);
        layout.addView(option2);
        layout.addView(option3);

        // Create the dialog
        final AlertDialog d = new AlertDialog.Builder(PollsActivity.this)
                .setIcon(R.drawable.ic_add)
                .setTitle("Create Poll")
                .setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        createNewPoll(courseid, name.getText().toString(),option1.getText().toString(),option2.getText().toString(),option3.getText().toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }).show();

        //button disabled by default
        d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

        // Text listener for enabled dialog positive button
        name.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s)
            {
                if (!s.toString().isEmpty() && !option1.getText().toString().isEmpty())
                    d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        //text listener for lastname
        option1.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && !name.getText().toString().isEmpty()) {
                    d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
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
            case R.id.action_add:
                addPoll(getCurrentFocus());
        }
        return super.onOptionsItemSelected(item);
    }

}
