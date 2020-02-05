package com.edulive.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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
import com.edulive.PollChartActivity;
import com.edulive.PollResultsActivity;
import com.edulive.PollVoteActivity;
import com.edulive.PollsActivity;
import com.edulive.R;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PollsAdapter extends BaseAdapter implements Filterable {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private List<FeedItem> feedItemsFiltered;
    private ItemFilter mFilter = new ItemFilter();
    private FeedItem item;
    private String APPURL;
    private String viewType;

    public PollsAdapter(Activity activity, List<FeedItem> feedItems, String viewType) {
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
        ImageView btn_results= (ImageView) convertView.findViewById(R.id.btn_results);
        ImageView btn_chart= (ImageView) convertView.findViewById(R.id.btn_chart);


        item = feedItemsFiltered.get(position);
        name.setText(item.getName());
        created_by.setText("Posted: "+item.getDateJoined());
        is_member.setVisibility(View.GONE);
        if(viewType.equals("owner")) {
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.VISIBLE);
            btn_results.setVisibility(View.VISIBLE);
            btn_chart.setVisibility(View.VISIBLE);
            description.setVisibility(View.GONE);
        }else if(viewType.equals("results")){
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
            btn_results.setVisibility(View.GONE);
            btn_chart.setVisibility(View.GONE);
            description.setVisibility(View.VISIBLE);
            description.setText("Voted: "+item.getDescription());
        }else{
            btn_approve.setVisibility(View.GONE);
            btn_decline.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
            btn_results.setVisibility(View.VISIBLE);
            btn_chart.setVisibility(View.VISIBLE);
            description.setVisibility(View.GONE);
        }
        //onclick event for view polls results
        btn_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, PollResultsActivity.class);
                intent.putExtra("pollid", feedItemsFiltered.get(position).getId());
                intent.putExtra("question", feedItemsFiltered.get(position).getName());
                intent.putExtra("option1", feedItemsFiltered.get(position).getDescription());
                intent.putExtra("option2", feedItemsFiltered.get(position).getPostTags());
                intent.putExtra("option3", feedItemsFiltered.get(position).getBodyContent());

                intent.putExtra("count_a",feedItemsFiltered.get(position).getCountHype());
                intent.putExtra("count_b",feedItemsFiltered.get(position).getCountCast());
                intent.putExtra("count_c",feedItemsFiltered.get(position).getCountComments());

                intent.putExtra("viewtype","results");

                activity.startActivity(intent);
            }
        });
        btn_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, PollChartActivity.class);
                intent.putExtra("pollid", feedItemsFiltered.get(position).getId());
                intent.putExtra("question", feedItemsFiltered.get(position).getName());
                intent.putExtra("option1", feedItemsFiltered.get(position).getDescription());
                intent.putExtra("option2", feedItemsFiltered.get(position).getPostTags());
                intent.putExtra("option3", feedItemsFiltered.get(position).getBodyContent());

                intent.putExtra("count_a",feedItemsFiltered.get(position).getCountHype());
                intent.putExtra("count_b",feedItemsFiltered.get(position).getCountCast());
                intent.putExtra("count_c",feedItemsFiltered.get(position).getCountComments());

                intent.putExtra("viewtype","results");

                activity.startActivity(intent);
            }
        });
        //onclick event for delete
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction("Delete Poll", "This poll will be deleted. Continue?", "Delete", "Cancel", "delete", feedItemsFiltered.get(position).getId());
            }
        });

        return convertView;
    }

    public void callAction(String title, String message, String btnYes, String btnNo, final String action, final String pollId){
        AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(activity);
        // set dialog message
        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnYes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                performAction(pollId);
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

    private void performAction(String pollId) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Deleting...");
        progressDialog.show();

        try {
            String link = activity.getResources().getString(R.string.base_url).concat("api/pollaction");

            Map<String, String> postParam= new HashMap<String, String>();

            postParam.put("pollid", pollId);
            postParam.put("action","deletepoll");
            postParam.put("userid", "");

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("show_error2"+response.toString());
                            progressDialog.cancel();
                            activity.finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //System.out.println("show_error3 "+error.getLocalizedMessage());
                    progressDialog.cancel();
                }
            });

            // Adding request to volley request queue
            AppController.getInstance().addToRequestQueue(jsonReq);

        } catch (Exception e) {
            progressDialog.cancel();
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