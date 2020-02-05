package com.edulive;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.edulive.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
         {

    SessionManager session;//declare the session manager class
    RequestQueue requestQueue;//declare the volley request queue manager class
    private ProgressDialog pDialog;
    private String usernames, email, userid;//declare string variables to hold users basic info
    //UI references, declare variables for the UI widgets and action buttons
    TextView name_holder;
    TextView email_holder;
    private Button course_list;
    private Button new_course;
    private Button all_courses;
    private Button my_messages;
    private Button profile;
    private Button logout;

    static Button notifCount;
    static int mNotifCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new SessionManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        */

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        usernames = user.get(SessionManager.KEY_LASTNAME).concat(" "+user.get(SessionManager.KEY_FIRSTNAME));
        email = user.get(SessionManager.KEY_EMAIL);
        userid = user.get(SessionManager.KEY_TOKEN);

        //get the view objects into the already declared variables
        name_holder = (TextView) findViewById( R.id.name_holder );
        email_holder = (TextView) findViewById( R.id.email_holder );
        course_list = (Button) findViewById(R.id.btn_courses);
        new_course = (Button) findViewById(R.id.btn_newcourse);
        all_courses = (Button) findViewById(R.id.btn_all_courses);
        my_messages = (Button) findViewById(R.id.btn_messages);
        profile = (Button) findViewById(R.id.btn_profile);
        logout = (Button) findViewById(R.id.btn_logout);

        //display the users info in the view objects variable
        name_holder.setText(usernames);
        email_holder.setText(email);

        my_messages.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent registerScreen = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(registerScreen);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                callLogout();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent registerScreen = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(registerScreen);
            }
        });

        course_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CoursesActivity.class);
                i.putExtra("viewtype", "owner");
                startActivity(i);
            }
        });

        all_courses.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CoursesActivity.class);
                i.putExtra("viewtype", "visitor");
                startActivity(i);
            }
        });

        new_course.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                newCourse(getCurrentFocus());
            }
        });

        //run background function to get unread notifications
        new NotificationTask(userid).execute();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_notification);
        MenuItemCompat.setActionView(item, R.layout.menu_counter);
        notifCount = (Button) MenuItemCompat.getActionView(item);
        notifCount.setText(String.valueOf(mNotifCount));

        //implement onclick functionality
        notifCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerScreen = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(registerScreen);
            }
        });

        //return true;
        return true;
    }

    //method to update notification counter
    private void setNotifCount(int count){
        mNotifCount = count;
        invalidateOptionsMenu();
    }

    private class NotificationTask extends AsyncTask<String, Void, Integer> {
        private String mUid;
        public NotificationTask(String uid) {
            mUid = uid;
        }

        @Override
        protected Integer doInBackground(String... sUrl) {
            //int ret;
            try{
                // making fresh volley request and getting json
                Map<String, String> postParam= new HashMap<String, String>();
                postParam.put("uid",mUid);
                String URL_FEED = getString(R.string.base_url).concat("api/notifications/counter");
                JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED,
                        new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            try {
                                int ret = Integer.parseInt(response.getString("counter"));
                                setNotifCount(ret);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

                // Adding request to volley request queue
                AppController.getInstance().addToRequestQueue(jsonReq);

            } catch (Exception e) {

            }

            return 0;
        }

        @Override
        protected void onProgressUpdate (Void... values) {
            // Getting reference to the TextView tv_counter of the layout activity_main
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer counter) {
            setNotifCount(counter);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            Intent registerScreen = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(registerScreen);
            return true;
        }else if(id==R.id.action_notification || id==R.id.notif_count){
            Intent registerScreen = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(registerScreen);
            return true;
        }else if(id == R.id.action_logout){
            callLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void callLogout() {
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Are you sure you want to leave?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                signOut();
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void signOut(){
        session.logoutUser();//clear all sessions
        //session.setFirstTimeLaunch(false);;
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish();
    }

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    /**
     * Shows Window for adding a new course
     * @param view
     */
    private void newCourse(View view) {

        // Set an EditText view to get user input
        final EditText name = new EditText(MainActivity.this);
        final EditText institution = new EditText(MainActivity.this);
        final EditText department = new EditText(MainActivity.this);

        name.setHint("Name");
        institution.setHint("Institution");
        department.setHint("Department");

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
        final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_add_black_24dp)
                .setTitle("New Course")
                .setView(layout)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        saveCourse(name.getText().toString(),institution.getText().toString(),department.getText().toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                }).show();

        d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

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

    private void saveCourse(String name,String instutition,String department) {
        pDialog = ProgressDialog.show(MainActivity.this, "", "Please wait...", true);

        try {
            String link = getString(R.string.base_url).concat("api/newcourse");//load remote url
            Map<String, String> postParam= new HashMap<String, String>();
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
                                    Intent intent = new Intent(MainActivity.this, CoursesActivity.class);
                                    intent.putExtra("viewtype", "owner");
                                    startActivity(intent);
                                    //finish();
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
    private void callAlert(String title, String message, String  action) {
        UtilityClass.messageAlertBox(title, message, action,this);
    }
}
