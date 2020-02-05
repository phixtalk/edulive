package com.edulive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

public class PollVoteActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SessionManager session;
    private ProgressDialog pDialog;

    TextView refresh;
    String userid, courseid, pollid,question,option1,option2,option3;

    TextView questionView;
    RadioGroup radioGroup;
    private RadioButton radioTypeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_vote);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Session Manager
        session = new SessionManager(getApplicationContext());
        //mStatusText = (EditText) findViewById(R.id.status_title);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //get session values
        HashMap<String, String> userz = session.getUserDetails();
        userid = userz.get(SessionManager.KEY_TOKEN);
        //get values passed on from previous page
        Bundle recdData = getIntent().getExtras();
        courseid = recdData.getString("courseid");
        pollid = recdData.getString("pollid");
        question = recdData.getString("question");
        option1 = recdData.getString("option1");
        option2 = recdData.getString("option2");
        option3 = recdData.getString("option3");
        String[] optionsOpt = {option1, option2, option3};

        //set question value
        questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);
        //set options
        radioGroup = (RadioGroup)findViewById(R.id.options);
        for (int i = 0; i < radioGroup .getChildCount(); i++) {
            ((RadioButton) radioGroup.getChildAt(i)).setText(String.valueOf(optionsOpt[i]));
        }
        //set submit button implementation
        Button submit = (Button) findViewById(R.id.btn_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSubmit();
            }
        });

    }

    private void callSubmit() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioTypeButton = (RadioButton) findViewById(selectedId);

        boolean cancel = false;
        View focusView = null;

        if(selectedId == -1){
            radioGroup.setFocusable(true);
            focusView = radioGroup;
            cancel = true;
            callAlert("Select An Option.", "Please select an option.", "failure");
        }else{
            String selectOption = radioTypeButton.getText().toString();
            //connect to database and save voted option

            pDialog = ProgressDialog.show(PollVoteActivity.this, "", "Please wait...", true);
            try {
                String link = getString(R.string.base_url).concat("api/pollaction");//load remote url
                Map<String, String> postParam = new HashMap<String, String>();
                postParam.put("pollid", pollid);
                postParam.put("userid", userid);
                postParam.put("vote",selectOption);
                postParam.put("action","pollvote");
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                pDialog.cancel();//stop loading dialog box
                                try {
                                    String status = response.getString("status");
                                    if (status.equals("success")) {
                                        Toast.makeText(getApplicationContext(), "Vote was recorded successfully.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else if (status.equals("failure")) {
                                        callAlert("Action Failed.", "Something went wrong. Please try later.", "failure");
                                    }else if(status.equals("exist")){
                                        callAlert("Alread Voted.", "You have already voted in this poll.", "failure");
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
    }

    private void callAlert(String title, String message, String  action) {
        UtilityClass.messageAlertBox(title, message, action,this);
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
