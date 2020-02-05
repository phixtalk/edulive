package com.edulive;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CourseDetailsActivity extends AppCompatActivity {

    private String courseid,coursename,institutionVal,departmentVal,userid,email,is_member,thisuser,thisusername;
    private MenuItem item_edit, item_delete, item_join_course;
    SessionManager session;//declare the session manager class
    RequestQueue requestQueue;//declare the volley request queue manager class
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Bundle recdData = getIntent().getExtras();
        //send to db
        courseid = recdData.getString("courseid");
        coursename = recdData.getString("coursename");
        institutionVal = recdData.getString("institution");
        departmentVal = recdData.getString("department");
        userid = recdData.getString("userid");
        email = recdData.getString("email");
        is_member = recdData.getString("is_member");

        HashMap<String, String> user = session.getUserDetails();
        thisusername = user.get(SessionManager.KEY_LASTNAME).concat(" "+user.get(SessionManager.KEY_FIRSTNAME));
        thisuser = user.get(SessionManager.KEY_TOKEN);

        setTitle(coursename);

        if(is_member.equals("null")){//user is not yet a member,
            Toast.makeText(getApplicationContext(),"Please Send Request to Join This Course", Toast.LENGTH_LONG).show();
        }else{
            //get list item
            String[] settingsOpt = {"Polls", "Announcements", "Course Files", "Members"};
            ListAdapter theAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, settingsOpt);
            ListView theListView = (ListView) findViewById(R.id.theListView);
            theListView.setAdapter(theAdapter);

            theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<? > arg0, View view, int position, long id) {
                    // When clicked, show a toast with the TextView text
                    //System.out.println("mytag"+position);
                    if(position == 0){//manage polls
                        Intent i = new Intent(CourseDetailsActivity.this, PollsActivity.class);
                        i.putExtra("courseid", courseid);
                        i.putExtra("viewtype", "member");
                        startActivity(i);
                    } else if (position == 1){//
                        Intent i = new Intent(CourseDetailsActivity.this, AnnouncementActivity.class);
                        i.putExtra("courseid", courseid);
                        i.putExtra("viewtype", "member");
                        startActivity(i);
                    } else if (position == 2){//files activity
                        Intent i = new Intent(CourseDetailsActivity.this, FilesActivity.class);
                        i.putExtra("courseid", courseid);
                        i.putExtra("viewtype", "member");
                        startActivity(i);
                    } else if (position == 3){//members activity
                        Intent i = new Intent(CourseDetailsActivity.this, MembersActivity.class);
                        i.putExtra("courseid", courseid);
                        i.putExtra("viewtype", "active");
                        i.putExtra("usertype", "member");
                        startActivity(i);
                    }
                    //Toast.makeText(getApplicationContext(), ((TextView) view).getText(),Toast.LENGTH_SHORT).show();

                }

            });
        }
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
            case R.id.action_join_course:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                // set title
                alertDialogBuilder.setTitle("Join Course?");
                // set dialog message
                alertDialogBuilder
                        .setMessage("Tap YES to Send Request to Course Admin.")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                sendJoinRequest();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendJoinRequest() {
        pDialog = ProgressDialog.show(CourseDetailsActivity.this, "", "Please wait...", true);
        try {
            String link = getString(R.string.base_url).concat("api/joincourse");//load remote url
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("courseid",courseid);
            postParam.put("coursename",coursename);
            postParam.put("userid",thisuser);
            postParam.put("username",thisusername);
            postParam.put("email",email);
            postParam.put("ownerid",userid);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.cancel();//stop loading dialog box
                            try{
                                String status = response.getString("status");
                                System.out.println("show_error_"+status);
                                if(status.equals("success")){
                                    Toast.makeText(getApplicationContext(),"Request was sent successfully.", Toast.LENGTH_LONG).show();
                                    //navigate to courses list
                                    Intent intent = new Intent(CourseDetailsActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else if(status.equals("pending")){
                                    callAlert("Pending Request.", "Your request is still pending", "exist");
                                } else if(status.equals("exist")){
                                    callAlert("Course Member.", "You are already a member of this course", "exist");
                                } else if(status.equals("failure")){
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
                    System.out.println("show_error_"+error.getLocalizedMessage());
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
        }
        catch (Exception ex) {
            pDialog.cancel();//stop loading dialog box
            System.out.println("show_error_"+ex.getLocalizedMessage());
            //Log.e("ProfileActivity", "updateTask: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course, menu);
        item_edit = menu.findItem(R.id.action_edit);
        item_delete = menu.findItem(R.id.action_delete);
        item_join_course = menu.findItem(R.id.action_join_course);
        //hide the other memnus
        item_edit.setVisible(false);
        item_delete.setVisible(false);
        //check if user is a member
        if(is_member.equals("null")){
            item_join_course.setVisible(true);
        }else{
            item_join_course.setVisible(false);
        }
        return true;
    }
    private void callAlert(String title, String message, String  action) {
        UtilityClass.messageAlertBox(title, message, action,this);
    }
}
