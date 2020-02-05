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
import com.edulive.adapter.AnnouncementAdapter;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SessionManager session;
    private ProgressDialog pDialog;
    private ListView listView;
    private AnnouncementAdapter listAdapter;
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
        setContentView(R.layout.activity_announcement);

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
        listAdapter = new AnnouncementAdapter(this, feedItems, viewtype);
        listView.setAdapter(listAdapter);

        refresh.setVisibility(View.GONE);
        loadAllAnnouncements();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.GONE);
                loadAllAnnouncements();
            }
        });

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.setHint("Search Announcements");
        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the text
                AnnouncementActivity.this.listAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });
    }

    private void loadAllAnnouncements() {
        try{
            showProgress(true);
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("userid",userid);
            postParam.put("courseid",courseid);
            postParam.put("viewtype",viewtype);

            String URL_FEED = getString(R.string.base_url).concat("api/announcements");
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
                                        item.setName(feedObj.getString("name"));
                                        item.setDescription(feedObj.getString("text"));
                                        item.setDateJoined(feedObj.getString("timestamp"));
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
    private void createNewAnnouncement(String courseid, String coursename, String description) {
        pDialog = ProgressDialog.show(AnnouncementActivity.this, "", "Please wait...", true);
        try {
            String link = getString(R.string.base_url).concat("api/announceaction");//load remote url
            Map<String, String> postParam = new HashMap<String, String>();
            postParam.put("courseid", courseid);
            postParam.put("coursename", coursename);
            postParam.put("description",description);
            postParam.put("action","newcourse");
            postParam.put("userid", userid);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.cancel();//stop loading dialog box
                            try {
                                String status = response.getString("status");
                                System.out.println("show_error_" + response.toString());
                                if (status.equals("success")) {
                                    Toast.makeText(getApplicationContext(), "Announcement added successfully.", Toast.LENGTH_LONG).show();
                                    finish();
                                    //navigate to courses list
                                    //Intent intent = new Intent(AnnouncementActivity.this, MainActivity.class);
                                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
            //Log.e("ProfileActivity", "updateTask: " + e.getMessage());
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

    private void addAnnouncement(View view) {
        // Set an EditText view to get user input
        final EditText name = new EditText(AnnouncementActivity.this);
        final EditText description = new EditText(AnnouncementActivity.this);

        name.setHint("Title");
        description.setHint("Content");

        // Set edit texts to be one line
        name.setLines(1);
        description.setLines(3);

        // Create the label
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(description);

        // Create the dialog
        final AlertDialog d = new AlertDialog.Builder(AnnouncementActivity.this)
                .setIcon(R.drawable.ic_add)
                .setTitle("Create Announcement")
                .setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        createNewAnnouncement(courseid, name.getText().toString(),description.getText().toString());
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
                if (!s.toString().isEmpty() && !description.getText().toString().isEmpty())
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
        description.addTextChangedListener(new TextWatcher(){
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
                addAnnouncement(getCurrentFocus());
        }
        return super.onOptionsItemSelected(item);
    }
}
