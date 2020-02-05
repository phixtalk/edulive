package com.edulive;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText mFirstnameView;
    private EditText mLastnameView;
    private EditText mPhoneView;
    private EditText mEmailView;
    private EditText mPasswordView;

    private View mProgressView;
    private View mLoginFormView;
    private TextView message;

    SessionManager session;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().hide();
        setTitle("");

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        mFirstnameView = (EditText) findViewById(R.id.firstname);
        mLastnameView = (EditText) findViewById(R.id.lastname);
        mEmailView = (EditText) findViewById(R.id.email);
        mPhoneView = (EditText) findViewById(R.id.phone);
        mPasswordView = (EditText) findViewById(R.id.password);

        TextView newAccount = (TextView) findViewById(R.id.newAccount);
        newAccount.setPaintFlags(newAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerScreen = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(registerScreen);
                finish();
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        message = (TextView) findViewById(R.id.message);

    }

    //set userinfo class properties
    private String firstname, lastname, email, phone, password;

    private void attemptLogin() {
        // get selected radio button from radioGroup
        // Reset errors.
        mEmailView.setError(null);
        mPhoneView.setError(null);
        mPasswordView.setError(null);
        mFirstnameView.setError(null);
        mLastnameView.setError(null);

        message.setText("");
        message.setVisibility(View.GONE);

        // Store values at the time of the login attempt.
        firstname = mFirstnameView.getText().toString();
        lastname = mLastnameView.getText().toString();
        email = mEmailView.getText().toString();
        phone = mPhoneView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if(!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        //check for a valid username
        if (TextUtils.isEmpty(firstname)) {
            mFirstnameView.setError(getString(R.string.error_field_required));
            focusView = mFirstnameView;
            cancel = true;
        } else if (TextUtils.isEmpty(lastname)) {
            mLastnameView.setError(getString(R.string.error_field_required));
            focusView = mLastnameView;
            cancel = true;
        } else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }else if(!isEmailValid(email)){
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password, usernames);
            //mAuthTask.execute((Void) null);

            //next save users data to relational db
            saveDataToSQL(firstname,lastname,email,phone,password);
        }
    }
    private void messageAlertBox(String title, String message, final String action){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(action.equals("success")){
                                    Intent registerScreen = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(registerScreen);
                                    finish();
                                }else{
                                    dialog.cancel();
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveDataToSQL(final String firstname,final String lastname,final String email,final String phonenumber,final String password){
        try {
            String link = getString(R.string.base_url).concat("api/register");
            String firebase_token = FirebaseInstanceId.getInstance().getToken();//get current firebase token manually

            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("firstname",firstname);
            postParam.put("lastname",lastname);
            postParam.put("email",email);
            postParam.put("phone",phonenumber);
            postParam.put("password",password);
           postParam.put("firebasetoken",firebase_token);

            System.out.println("show_error_got here");

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                showProgress(false);
                                String status = response.getString("status");
                                System.out.println("show_error"+response.toString());
                                if(status.equals("success")){
                                    String userid = response.getString("userid");
                                    session.createSessionData(userid,firstname,lastname,email,phone);//save session outside of creating new record...
                                    //navigate to the main page
                                    Intent MainScreen = new Intent(SignupActivity.this, MainActivity.class);
                                    startActivity(MainScreen);
                                    finish();
                                } else if(status.equals("empty")){
                                    messageAlertBox("Empty Fields.", "Please enter all required fields.", "success");
                                } else if(status.equals("exist")){
                                    messageAlertBox("Account Exist.", "Email has already been used.", "success");
                                    mEmailView.requestFocus();
                                } else if(status.equals("failure")){
                                    messageAlertBox("Registration Failed.", "Something went wrong. Please try later.", "success");
                                    mEmailView.requestFocus();
                                }
                            } catch (Exception e) {
                                showProgress(false);
                                messageAlertBox("Error Occured.", e.getLocalizedMessage().toString(), "success");
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

        } catch (Exception e) {
            System.out.println("show_error "+e.getLocalizedMessage());
        }
    }

    //make back button work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
