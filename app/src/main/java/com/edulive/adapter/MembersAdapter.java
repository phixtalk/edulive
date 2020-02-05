package com.edulive.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.edulive.CourseMapActivity;
import com.edulive.MainActivity;
import com.edulive.R;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MembersAdapter extends BaseAdapter implements Filterable {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private List<FeedItem> feedItemsFiltered;
    private ItemFilter mFilter = new ItemFilter();
    private FeedItem item;
    private String APPURL;
    private String viewType, userType, courseName;

    public MembersAdapter(Activity activity, List<FeedItem> feedItems, String viewType, String userType, String courseName) {
        this.activity = activity;
        this.feedItems = feedItems;
        this.feedItemsFiltered = feedItems;
        this.viewType = viewType;
        this.userType = userType;
        this.courseName = courseName;
    }

    @Override
    public int getCount() {
        return feedItemsFiltered.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItemsFiltered.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.course_items, null);

        APPURL = activity.getResources().getString(R.string.base_url);

        TextView name = (TextView) convertView.findViewById(R.id.caption);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        TextView created_by = (TextView) convertView.findViewById(R.id.created_by);
        ImageView is_member = (ImageView) convertView.findViewById(R.id.is_member);

        ImageView btn_approve = (ImageView) convertView.findViewById(R.id.btn_approve);
        ImageView btn_decline = (ImageView) convertView.findViewById(R.id.btn_decline);
        ImageView btn_delete = (ImageView) convertView.findViewById(R.id.btn_delete);

        item = feedItemsFiltered.get(position);
        name.setText(item.getName());
        description.setText(item.getEmail());
        created_by.setText("Date joined: "+item.getDateJoined());
        is_member.setVisibility(View.GONE);

        if(userType.equals("owner")) {
            if (viewType.equals("active")) {
                btn_approve.setVisibility(View.GONE);
                btn_decline.setVisibility(View.GONE);
                btn_delete.setVisibility(View.VISIBLE);
            } else {
                btn_approve.setVisibility(View.VISIBLE);
                btn_decline.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.GONE);
            }
        }else{
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
        }

        //onclick event for approve
        btn_approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction("Approve Request", "This request will be approved. Continue?", "Approve", "Cancel", "approve",
                        feedItemsFiltered.get(position).getId(), feedItemsFiltered.get(position).getUserId());
            }
        });
        //onclick event for decline
        btn_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction("Decline Request", "This request will be declined. Continue?", "Decline", "Cancel", "decline",
                        feedItemsFiltered.get(position).getId(), feedItemsFiltered.get(position).getUserId());
            }
        });
        //onclick event for delete
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction("Delete Request", "This request will be deleted. Continue?", "Delete", "Cancel", "delete",
                        feedItemsFiltered.get(position).getId(), feedItemsFiltered.get(position).getUserId());
            }
        });


        return convertView;
    }

    public void callAction(String title, String message, String btnYes, String btnNo, final String action, final String courseId, final String memberId){
        AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        // set dialog message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnYes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                performAction(action, courseId, memberId);
                            }
                        })
                .setNegativeButton(btnNo,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void performAction(String action, String courseId, String memberId) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Processing...");
        progressDialog.show();

        try {
            String link = activity.getResources().getString(R.string.base_url).concat("api/memberaction");

            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("action",action);
            postParam.put("courseid",courseId);
            postParam.put("memberid",memberId);
            postParam.put("coursename",courseName);

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("show_error"+response.toString());
                            progressDialog.cancel();
                            //navigate to dashboard activity
                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
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
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<FeedItem> list = feedItems;

            int count = list.size();
            final ArrayList<FeedItem> nlist = new ArrayList<FeedItem>(count);

            FeedItem filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }
            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            feedItemsFiltered = (ArrayList<FeedItem>) results.values;
            notifyDataSetChanged();
        }

    }

}
