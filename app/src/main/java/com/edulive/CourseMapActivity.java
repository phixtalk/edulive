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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

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

public class CourseMapActivity extends AppCompatActivity {

    private String courseid,coursename,institutionVal,departmentVal,userid;
    private MenuItem item_edit;
    private MenuItem item_delete;
    SessionManager session;//declare the session manager class
    RequestQueue requestQueue;//declare the volley request queue manager class
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_map);

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

        setTitle(coursename);

        //get list item
        String[] settingsOpt = {"Manage Polls", "Announcements", "Manage Files", "Members", "Pending Requests"};
        ListAdapter theAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, settingsOpt);
        ListView theListView = (ListView) findViewById(R.id.theListView);
        theListView.setAdapter(theAdapter);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<? > arg0, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
                if(position == 0){//manage polls
                    Intent i = new Intent(CourseMapActivity.this, PollsActivity.class);
                    i.putExtra("courseid", courseid);
                    i.putExtra("viewtype", "owner");
                    startActivity(i);
                } else if (position == 1){//manage announcements
                    Intent i = new Intent(CourseMapActivity.this, AnnouncementActivity.class);
                    i.putExtra("courseid", courseid);
                    i.putExtra("viewtype", "owner");
                    startActivity(i);
                } else if (position == 2){//picture update
                    Intent i = new Intent(CourseMapActivity.this, FilesActivity.class);
                    i.putExtra("courseid", courseid);
                    i.putExtra("viewtype", "owner");
                    startActivity(i);
                } else if (position == 3){//manage active members
                    Intent i = new Intent(CourseMapActivity.this, MembersActivity.class);
                    i.putExtra("courseid", courseid);
                    i.putExtra("viewtype", "active");
                    i.putExtra("usertype", "owner");
                    startActivity(i);
                } else if (position == 4){//Manage pending member requests
                    Intent i = new Intent(CourseMapActivity.this, MembersActivity.class);
                    i.putExtra("courseid", courseid);
                    i.putExtra("coursename", coursename);
                    i.putExtra("viewtype", "pending");
                    i.putExtra("usertype", "owner");
                    startActivity(i);
                }
            }

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
            case R.id.action_edit:
                editCourse(getCurrentFocus());
                return true;
            case R.id.action_delete:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                // set title
                alertDialogBuilder.setTitle("Delete Course?");
                // set dialog message
                alertDialogBuilder
                        .setMessage("Tap YES to Delete this Course.")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                deleteCourse(courseid);
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

    private void editCourse(View view) {
        // Set an EditText view to get user input
        final EditText name = new EditText(CourseMapActivity.this);
        final EditText institution = new EditText(CourseMapActivity.this);
        final EditText department = new EditText(CourseMapActivity.this);

        name.setHint("Name");
        name.setText(coursename);
        institution.setHint("Institution");
        institution.setText(institutionVal);
        department.setHint("Department");
        department.setText(departmentVal);

        // Set edit texts to be one line
        name.setLines(1);
        institution.setLines(1);
        department.setLines(1);

        // Create the label
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(institution);
        layout.addView(department);

        // Create the dialog
        final AlertDialog d = new AlertDialog.Builder(CourseMapActivity.this)
                .setIcon(R.drawable.ic_action_edit)
                .setTitle("Edit Course")
                .setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(name.getText().toString().equals(coursename)){//check if use entered a new course name
                            callAlert("Course Name Unchanged","Please enter a new course name","error");
                        }else{
                            saveCourse(courseid, name.getText().toString(),institution.getText().toString(),department.getText().toString());
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }).show();

        //d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

        // Text listener for enabled dialog positive button
        name.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s)
            {
                if (!s.toString().isEmpty())
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
    }

    private void callAlert(String title, String message, String  action) {
        UtilityClass.messageAlertBox(title, message, action,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course, menu);

        item_edit = menu.findItem(R.id.action_edit);
        item_delete = menu.findItem(R.id.action_delete);

        return true;
    }

    private void saveCourse(String courseId, String name,String instutition,String department) {
        pDialog = ProgressDialog.show(CourseMapActivity.this, "", "Please wait...", true);

        try {
            String link = getString(R.string.base_url).concat("api/editcourse");//load remote url
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("courseid",courseId);
            postParam.put("name",name);
            postParam.put("institution",instutition);
            postParam.put("department",department);
            postParam.put("userid",userid);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.cancel();//stop loading dialog box
                            try{
                                String status = response.getString("status");
                                System.out.println("show_error_"+response.toString());
                                if(status.equals("success")){
                                    //navigate to courses list
                                    Intent intent = new Intent(CourseMapActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else if(status.equals("exist")){
                                    callAlert("Course Exist.", "The Course already exist..", "exist");
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
            //Log.e("ProfileActivity", "updateTask: " + e.getMessage());
        }
    }
    private void deleteCourse(String courseId)
    {
        pDialog = ProgressDialog.show(CourseMapActivity.this, "", "Please wait...", true);

        try {
            String link = getString(R.string.base_url).concat("api/deletecourse");//load remote url
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("courseid",courseId);
            postParam.put("userid",userid);
            JsonObjectRequest jsonObjReqq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            pDialog.cancel();//stop loading dialog box
                            try{
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    //navigate to courses list
                                    Intent intent = new Intent(CourseMapActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else if(status.equals("failure")){
                                    callAlert("Login Failed.", "Something went wrong. Please try later.", "failure");
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
            requestQueue.add(jsonObjReqq);
        }
        catch (Exception ex) {
            callAlert("Error Occured.", ex.getLocalizedMessage().toString(), "error");
        }
    }
}
