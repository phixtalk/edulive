package com.edulive;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.edulive.app.AppController;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ADMIN on 12/22/2018.
 */

public class UtilityClass {
    public static void updateFirebaseTokenMethod(String userbind, String token, Context context){
        try{
            /*
            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(userbind);
            Map<String, Object> updateField = new HashMap<String, Object>();
            updateField.put("firebasetoken", token);
            current_user_db.updateChildren(updateField);
            */
            new updateFirebaseSQLTask(userbind, token, context).execute("");

        }catch (Exception ex){

        }
    }

    public static CharSequence converteTimestamp(String mileSegundos){
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos),System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    public static void sendPushNotification(final String firebasetoken){

        String URL_FEED = "http://www.phixsoft.com.ng/chatlang/sendpush.php";
        try{
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();

            postParam.put("firebasetoken",firebasetoken);
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED, new JSONObject(postParam), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response != null) {
                            System.out.println("mytag response:"+response.toString());
                        }
                    }catch (Exception e){
                        System.out.println("mytag:"+e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("mytag:"+error.getMessage());
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);

        } catch (Exception e) {
            System.out.println("show_error"+e.getMessage());
        }
    }
    public static void sendChatNotification(final Context c, final String senduserid, final String sendusername, final String receiveruserid, final String firebasetoken,
                                            final String ticker,final String title,final String message){

        String URL_FEED = c.getResources().getString(R.string.base_url).concat("chatnotification");
        try{
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            System.out.println("mytag: "+firebasetoken);
            postParam.put("ticker",ticker);
            postParam.put("title",title);
            postParam.put("message",message);
            postParam.put("senduserid",senduserid);
            postParam.put("sendusername",sendusername);
            postParam.put("receiveruserid",receiveruserid);
            postParam.put("firebasetoken",firebasetoken);
            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED, new JSONObject(postParam), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response != null) {
                            System.out.println("mytag response:"+response.toString());
                        }
                    }catch (Exception e){
                        System.out.println("mytag:"+e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("mytag:"+error.getMessage());
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);

        } catch (Exception e) {
            System.out.println("show_error"+e.getMessage());
        }
    }
    public static void sendGroupChatNotification(final Context c, final String senduserid, final String sendusername, final String groupmemberids,
                                            final String ticker,final String title,final String message,final String groupid,final String groupname,final String createdon,
                                            final String firebasetoken){

        String URL_FEED = c.getResources().getString(R.string.base_url).concat("sendgrouppush");
        try{
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("ticker",ticker);
            postParam.put("title",title);
            postParam.put("message",message);
            postParam.put("senduserid",senduserid);
            postParam.put("sendusername",sendusername);
            postParam.put("memberids",groupmemberids);

            postParam.put("firebasetoken",firebasetoken);

            postParam.put("groupid",groupid);
            postParam.put("groupname",groupname);
            postParam.put("createdon",createdon);

            System.out.println("groupmemberids:"+groupmemberids);

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED, new JSONObject(postParam), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response != null) {
                            System.out.println("mytag response:"+response.toString());
                        }
                    }catch (Exception e){
                        System.out.println("mytag:"+e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("mytag:"+error.getMessage());
                    //VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);

        } catch (Exception e) {
            System.out.println("show_error"+e.getMessage());
        }
    }
    public static void initToast(Context c, String message){
        Toast.makeText(c,message, Toast.LENGTH_SHORT).show();
    }
    public static void inviteFriend(final Context c) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hello,\n" +
                "\n" +
                "I just downloaded ChatLang App on my Android.\n" +
                "\n" +
                "ChatLang is a powerful chatting app for android users. Meet new interesting people, and have great conversations everyday.\n" +
                "\n" +
                "Get it now. https://play.google.com/store/apps/details?id=com.chatlang";

        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "VanityApp... the new cool");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        c.startActivity(Intent.createChooser(sharingIntent, "Invite Your Friends"));
    }
    public  static boolean verificaConexao(Context context) {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    public static class updateFirebaseSQLTask extends AsyncTask<String, String, Void> {

        private final String mUserId;
        private final String mToken;
        private final Context mContext;

        updateFirebaseSQLTask(String userid, String token, final Context context) {
            mUserId = userid;
            mToken = token;
            mContext = context;
        }
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            final String URL_FEED = mContext.getResources().getString(R.string.base_url).concat("firebasetoken.php");
            try{
                // making fresh volley request and getting json
                Map<String, String> postParam= new HashMap<String, String>();
                postParam.put("userid",mUserId);
                postParam.put("token",mToken);

                JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, URL_FEED, new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response != null) {
                                //System.out.println("show_error:"+response.toString());
                                //System.out.println("show_error__CHECKING");
                            }
                        }catch (Exception e){
                            //System.out.println("show_error"+e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //System.out.println("show_error"+error.getMessage());
                        //VolleyLog.d(TAG, "Error: " + error.getMessage());
                    }
                });

                // Adding request to volley request queue
                AppController.getInstance().addToRequestQueue(jsonReq);

            } catch (Exception e) {
                //System.out.println("show_error"+e.getMessage());
            }

            return null;
        }
    }
    public static String local(String latitudeFinal,String longitudeFinal){
        String displayLocation = "https://maps.googleapis.com/maps/api/staticmap?key=AIzaSyDIJ9XX2ZvRKCJcFRrl-lRanEtFUow4piM" +
                "&center="+latitudeFinal+","+longitudeFinal+"&zoom=18&size=280x280&markers=color:red|"+latitudeFinal+","+longitudeFinal;
        System.out.println("displayLocation"+displayLocation);
        return displayLocation;
    }
    public static void messageAlertBox(String title, String message, final String action, Context context){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(action.equals("success")){
                                }else{
                                    dialog.cancel();
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
