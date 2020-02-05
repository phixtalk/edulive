package com.edulive;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.edulive.adapter.FilesAdapter;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;
import com.edulive.helpers.FilePath;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FilesActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SessionManager session;
    private ProgressDialog pDialog;
    private ListView listView;
    private FilesAdapter listAdapter;
    private List<FeedItem> feedItems;
    private View mProgressView;
    private MenuItem item_add_announcement;
    FeedItem item;
    // Search EditText
    EditText inputSearch;
    TextView refresh;
    String userid, viewtype, courseid;
    private String selectedFilePath;
    ProgressDialog dialog;

    private static final int PICK_FILE_REQUEST = 1;

    private String mediapath;
    private String mediadir;
    private boolean file_downloaded = false;
    String APPURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

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

        //check for permissions very early
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestForSpecificPermission();
        }

        APPURL = getString(R.string.base_url);

        //setup the list view
        listView = (ListView) findViewById(R.id.list);

        feedItems = new ArrayList<FeedItem>();
        listAdapter = new FilesAdapter(this, feedItems, viewtype);
        listView.setAdapter(listAdapter);

        //Download activator
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                item = feedItems.get(position);
                if(!viewtype.equals("owner")){//downloads for members only
                    askDownloadFile(item.getImage());
                }
            }
        });

        refresh.setVisibility(View.GONE);
        loadAllFiles();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh.setVisibility(View.GONE);
                loadAllFiles();
            }
        });

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        inputSearch.setHint("Search Files");
        // Enabling Search Filter
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the text
                FilesActivity.this.listAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
            @Override
            public void afterTextChanged(Editable arg0) {}
        });
    }

    //PERMISSIONS METHODS
//    private boolean checkIfAlreadyhavePermission() {
//        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
//        if (result == PackageManager.PERMISSION_GRANTED ) {
//            return true;
//        } else {
//            return false;
//        }
//    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                , android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //--PERMISSIONS METHODS


    private void loadAllFiles() {
        try{
            showProgress(true);
            // making fresh volley request and getting json
            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("userid",userid);
            postParam.put("courseid",courseid);
            postParam.put("viewtype",viewtype);

            String URL_FEED = getString(R.string.base_url).concat("api/coursefiles");
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
                                        item.setDescription(feedObj.getString("description"));
                                        item.setImage(feedObj.getString("filedir"));
                                        item.setFiletype(feedObj.getString("filetype"));
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

    public void askDownloadFile(final String url){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("File Download")
                .setCancelable(false)
                .setMessage("Download this file?")
                .setPositiveButton("Download",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                executeDownload(url);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void executeDownload(String url) {
        String medianame = url.substring(url.lastIndexOf('/')+1);
        mediadir = Environment.getExternalStorageDirectory() + "/" +getString(R.string.app_name) + "/Downloads/";
        mediapath = mediadir.concat(medianame);
        //make sure directory exists
        File mediaDirs = new File(mediadir);
        if (!mediaDirs.exists()) {
            if (!mediaDirs.mkdirs()) {
            }
        }
        //next check if file exist
        File mediaStorageDir = new File(mediapath);
        if (mediaStorageDir.exists()) {
            file_downloaded = true;
            //new BackgroundAsyncTask().execute(mediapath);
            Toast.makeText(getApplicationContext(),"This file has already been download to your device.",Toast.LENGTH_LONG).show();
        } else {
//                        final DownloadTask downloadTask = new DownloadTask(FilesActivity.this);
//                        downloadTask.execute(pictureurl);
            String downloadUrl = APPURL.concat(url);
            new DownloadTask(FilesActivity.this).execute(downloadUrl);
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

    private AlertDialog d;
    private TextView tvFileName;
    private String fileTile="", fileDescribe="";
    private void addFile(View view) {
        // Set an EditText view to get user input
        final EditText name = new EditText(FilesActivity.this);
        final EditText describe = new EditText(FilesActivity.this);
        final ImageView upload = new ImageView(FilesActivity.this);
        tvFileName = new TextView(FilesActivity.this);
        name.setHint("Title");
        name.setLines(1);

        describe.setHint("File Description");
        describe.setLines(2);

        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        upload.setImageResource(R.drawable.ic_camera_black);
        //upload.setLayoutParams(params);

        // Create the label
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(describe);
        layout.addView(tvFileName);
        layout.addView(upload);

        // Create the dialog
        d = new AlertDialog.Builder(FilesActivity.this)
                .setIcon(R.drawable.ic_add)
                .setTitle("Upload File")
                .setView(layout)
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedFilePath != null){
                            fileTile = name.getText().toString();
                            fileDescribe = describe.getText().toString();
                            uploadFile(selectedFilePath);
                        }else{
                            Toast.makeText(FilesActivity.this,"Please choose a File First",Toast.LENGTH_SHORT).show();
                        }
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
                if (!s.toString().isEmpty() && selectedFilePath!=null)
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

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
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
            case R.id.action_add:
                addFile(getCurrentFocus());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        //sets the select file to all types of files
        intent.setType("*/*");
        //allows to select data and return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //starts new activity to select file and return data
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == PICK_FILE_REQUEST){
                if(data == null){
                    //no data present
                    return;
                }
                Uri selectedFileUri = data.getData();
                try {
                    selectedFilePath = getPathFromUri(FilesActivity.this,selectedFileUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    tvFileName.setText(selectedFilePath);
                    if(!fileTile.equals("")){
                        d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }else{
                    d.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //android upload file to server

    public void uploadFile(final String filePath){
        if (filePath != null) {
            // Displaying the image or video on the screen
            //next check filepaht type and size
            File chkfile = new File(filePath);
            long length = chkfile.length() / 1024;
            String mysize = Long.toString(length);
            String ftype = getExtension(chkfile.getName());
            new UploadFileToServer(ftype).execute();
        } else {
            Toast.makeText(getApplicationContext(),"No media attached.", Toast.LENGTH_LONG).show();
        }
    }

    public static String getExtension(String fileName) {
        String encoded;
        try { encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"); }
        catch(UnsupportedEncodingException e) { encoded = fileName; }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }

    long totalSize = 0;

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        private final String mFilePath;
        private final String mFileTitle;
        private final String mfileDescribe;
        private final String mCourseId;
        private final String mFileType;

        UploadFileToServer(String filetype) {
            mFilePath = selectedFilePath;
            mFileTitle = fileTile;
            mfileDescribe = fileDescribe;
            mCourseId = courseid;
            mFileType = filetype;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(FilesActivity.this,"","Uploading File...",true);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(getString(R.string.base_url).concat("api/uploadfiles"));

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) 1000) * 100));
                            }
                        });

                // Extra parameters if you want to pass to server
                File sourceFile = new File(mFilePath);
                entity.addPart("userid", new StringBody(userid));
                entity.addPart("userfile", new FileBody(sourceFile));
                entity.addPart("ftitle",new StringBody(mFileTitle));
                entity.addPart("filedescribe", new StringBody(mfileDescribe));
                entity.addPart("ftype", new StringBody(mFileType));
                entity.addPart("courseid", new StringBody(mCourseId));
                entity.addPart("base_url", new StringBody(getString(R.string.base_url)));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "+ statusCode;
                    System.out.println("mytagzz1: "+responseString);
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
                System.out.println("mytagzz2: "+responseString);
            } catch (IOException e) {
                responseString = e.toString();
                System.out.println("mytagzz3: "+responseString);
                //txt.setText(R.string.no_connection);//no feedback from server ==> server down
            } catch (Exception g){
                System.out.println("mytagzz4:"+g.getMessage() + mFileType);
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jObject  = new JSONObject(result); // json
                String status = jObject.getString("status");
                if(status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Upload was successful.", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),status, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                finish();
            } catch (Exception ex) {
                System.out.println("mytagzz5:"+ex.getMessage());
            }
        }

    }

    //working for all android version.This is tested code.This support all devices
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    //working for all android version.This is tested code.This support all devices


    //Download files implementation
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(mediapath);

                byte data[] = new byte[4096];
                //Log.d("Orignalvalu1",String.valueOf(new byte[4096]));
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    //Log.d("Orignalvalu1",String.valueOf(input.read(data)));
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    //Log.d("Orignalvalu2",String.valueOf(total));
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            dialog = ProgressDialog.show(FilesActivity.this,"","Downloading File...",true);
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            dialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"Downloaded to Storage/" +getString(R.string.app_name) + "/Downloads/", Toast.LENGTH_SHORT).show();
        }
    }
}
