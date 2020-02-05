package com.edulive;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class ProfileActivity extends AppCompatActivity {

    SessionManager session;//declare the session manager class
    RequestQueue requestQueue;//declare the volley request queue manager class
    private String usernames, email;//declare string variables to hold users basic info
    //UI references, declare variables for the UI widgets and edit buttons
    TextView name_holder;
    TextView email_holder;
    ImageView edit_name;
    ImageView edit_password;
    private ProgressDialog pDialog;//declare progress dialog object

    // Flags for change password
    private boolean FLAG_OLDPASS;
    private boolean FLAG_PASS;
    private boolean FLAG_RETYPEPASS;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private updateTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        usernames = user.get(SessionManager.KEY_LASTNAME).concat(" "+user.get(SessionManager.KEY_FIRSTNAME));
        email = user.get(SessionManager.KEY_EMAIL);

        //get the view objects into the already declared variables
        name_holder = (TextView) findViewById( R.id.name_holder );
        email_holder = (TextView) findViewById( R.id.email_holder );
        edit_name = (ImageView) findViewById(R.id.edit_name);
        edit_password = (ImageView) findViewById(R.id.edit_password);

        //display the users info in the view objects variable
        name_holder.setText(usernames);
        email_holder.setText(email);

        //next, set onclick event listeners to perform actions when user clicks on the edit icon
        /*
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        */

        edit_name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Set an EditText view to get user input
                final EditText input = new EditText(ProfileActivity.this);
                input.setHint("First Name");

                final EditText inputLastname = new EditText(ProfileActivity.this);
                inputLastname.setHint("Last Name");

                // Set edit texts to be one line
                input.setLines(1);
                inputLastname.setLines(1);

                // Create the label
                Context context = view.getContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(input);
                layout.addView(inputLastname);

                // Create the dialog
                final AlertDialog d = new AlertDialog.Builder(ProfileActivity.this)
                        .setIcon(R.drawable.ic_action_edit)
                        .setTitle("Update Name")
                        .setCancelable(false)
                        .setView(layout)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //perform action here

                                mAuthTask = new updateTask(input.getText().toString(), inputLastname.getText().toString(), null, 1);
                                mAuthTask.execute((Void) null);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).show();

                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

                // Text listener for enabled dialog positive button
                input.addTextChangedListener(new TextWatcher(){
                    public void afterTextChanged(Editable s) {
                        if (!s.toString().isEmpty() && !inputLastname.getText().toString().isEmpty()) {
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
                //text listener for lastname
                inputLastname.addTextChangedListener(new TextWatcher(){
                    public void afterTextChanged(Editable s) {
                        if (!s.toString().isEmpty() && !input.getText().toString().isEmpty()) {
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
        });

        // Edit button password
        edit_password.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Set an EditText view to get user input
                final EditText inputOldPassword = new EditText(ProfileActivity.this);
                final EditText inputPassword = new EditText(ProfileActivity.this);
                final EditText inputRetypePassword = new EditText(ProfileActivity.this);
                final TextView message = new TextView(ProfileActivity.this);

                // Set the color of message text view
                message.setTextColor(Color.WHITE);

                // Center it
                message.setGravity(Gravity.CENTER);

                // Set text hints
                inputOldPassword.setHint("Old Password");
                inputPassword.setHint("New Password");
                inputRetypePassword.setHint("Re-type New Password");

                // Set edit texts to be one line
                inputOldPassword.setLines(1);
                inputPassword.setLines(1);
                inputRetypePassword.setLines(1);

                // Transform them to passwords
                inputOldPassword.setTransformationMethod(new PasswordTransformationMethod());
                inputPassword.setTransformationMethod(new PasswordTransformationMethod());
                inputRetypePassword.setTransformationMethod(new PasswordTransformationMethod());

                // Create the label
                Context context = view.getContext();
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(inputOldPassword);
                layout.addView(inputPassword);
                layout.addView(inputRetypePassword);
                layout.addView(message);

                // Create the dialog
                final AlertDialog d = new AlertDialog.Builder(ProfileActivity.this)
                        .setIcon(R.drawable.ic_action_edit)
                        .setTitle("Update Password")
                        .setView(layout)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                mAuthTask = new updateTask("", "", inputPassword.getText().toString(), 2);
                                mAuthTask.execute((Void) null);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }).show();

                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

                // Text listeners for enabled dialog positive button
                inputOldPassword.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (s.toString().length() < 4 && !s.toString().isEmpty()) {
                            message.setText("Old password is too short");
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            message.setText("");
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!s.toString().isEmpty()) {
                            FLAG_OLDPASS = true;
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        } else {
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                            FLAG_OLDPASS = false;
                        }
                    }
                });

                inputPassword.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (s.toString().length() < 4 && !s.toString().isEmpty()) {
                            message.setText("New password is too short");
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            message.setText("");
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!s.toString().isEmpty()) {
                            FLAG_PASS = true;
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        } else {
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                            FLAG_PASS = false;
                        }
                    }
                });

                inputRetypePassword.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (!inputPassword.getText().toString().equals(inputRetypePassword.getText().toString())) {
                            message.setText(R.string.error_not_equal_password);
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            message.setText("");
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }							}
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!s.toString().isEmpty()) {
                            FLAG_RETYPEPASS = true;
                            if (FLAG_OLDPASS && FLAG_PASS && FLAG_RETYPEPASS) {
                                d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        } else {
                            d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                            FLAG_RETYPEPASS = false;
                        }
                    }
                });
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
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Represents an asynchronous update task used to authenticate the user
     */
    public class updateTask extends AsyncTask<Void, Void, Boolean> {
        private int type;
        private String name;
        private String lastname;
        private String newPass;

        /**
         * Constructor
         * @param type
         */
		public updateTask(String name, String lastname, String newPass, int type) {
            this.name = name;
            this.lastname = lastname;
            this.newPass = newPass;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.base_url).concat("api/profileupdate");
                Map<String, String> postParam= new HashMap<String, String>();
                postParam.put("firstname",name);
                postParam.put("lastname",lastname);
                postParam.put("email",email);
                postParam.put("new_password",newPass);
                if (type == 1) {
                    postParam.put("tag","update_name");
                } else if (type == 2) {
                    postParam.put("tag","update_password");
                }
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String status = response.getString("status");
                                    System.out.println("show_error_"+type+"__"+response.toString());
                                    if(status.equals("success")){

                                    } else if(status.equals("failure")){
                                        //messageAlertBox("Login Failed.", "Something went wrong. Please try later.", "failure");
                                        //mEmailView.requestFocus();
                                    }
                                } catch (Exception e) {
                                    //showProgress(false);
                                    //messageAlertBox("Error Occured.", e.getLocalizedMessage().toString(), "success");
                                    System.out.println("show_error_"+e.getLocalizedMessage()+"__");
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
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pDialog.dismiss();

            if (!success) {
            }
            if (type == 1) {//on success, update session variables
                session.updateSessionField("lastname",lastname);
                session.updateSessionField("firstname",name);
                name_holder.setText(lastname.toString()+" "+name.toString()); //it needs to be updated after the pdialog dismiss
            }else{
                finish();
            }
        }
    }
}