package com.edulive.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.edulive.R;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FilesAdapter extends BaseAdapter implements Filterable {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private List<FeedItem> feedItemsFiltered;
    private ItemFilter mFilter = new ItemFilter();
    private FeedItem item;
    private String APPURL;
    private String viewType;

    public FilesAdapter(Activity activity, List<FeedItem> feedItems, String viewType) {
        this.activity = activity;
        this.feedItems = feedItems;
        this.feedItemsFiltered = feedItems;
        this.viewType = viewType;
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
        ImageView btn_download = (ImageView) convertView.findViewById(R.id.btn_download);

        item = feedItemsFiltered.get(position);
        name.setText(item.getName());

        if(TextUtils.isEmpty(item.getDescription())||item.getDescription().equals("")||item.getDescription()==null||item.getDescription().equals("null")){
            description.setVisibility(View.GONE);
        }else{
            description.setVisibility(View.VISIBLE);
            description.setText(item.getDescription());
        }

        created_by.setText("Type: "+item.getFiletype()+", Posted: "+item.getDateJoined());
        is_member.setVisibility(View.GONE);

        if(viewType.equals("owner")){
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.VISIBLE);
            btn_download.setVisibility(View.GONE);
        }else{
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
            btn_download.setVisibility(View.VISIBLE);
        }
        //onclick event for delete
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction("Delete Request", "This file will be deleted. Continue?", "Delete", "Cancel",
                        "delete", feedItemsFiltered.get(position).getId(), feedItemsFiltered.get(position).getUserId(), feedItemsFiltered.get(position).getImage());
            }
        });
        return convertView;
    }

    public void callAction(String title, String message, String btnYes, String btnNo, final String action, final String courseId, final String memberId, final String image){
        AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        // set dialog message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnYes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                performAction(courseId, image);
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

    private void performAction(String fileId, String image) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Deleting...");
        progressDialog.show();

        try {
            String link = activity.getResources().getString(R.string.base_url).concat("api/fileaction");

            Map<String, String> postParam= new HashMap<String, String>();

            postParam.put("fileid", fileId);
            postParam.put("filepath", image);
            postParam.put("action","deletefile");

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("show_error"+response.toString());
                            progressDialog.cancel();
                            activity.finish();
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