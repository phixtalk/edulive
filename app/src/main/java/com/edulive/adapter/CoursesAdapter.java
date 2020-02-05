package com.edulive.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
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

import com.android.volley.toolbox.ImageLoader;
import com.edulive.CourseMapActivity;
import com.edulive.MainActivity;
import com.edulive.R;
import com.edulive.app.AppController;
import com.edulive.data.FeedItem;

import java.util.ArrayList;
import java.util.List;


public class CoursesAdapter extends BaseAdapter implements Filterable {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private List<FeedItem> feedItemsFiltered;
    private ItemFilter mFilter = new ItemFilter();
    private FeedItem item;
    private String APPURL;
    private String userId;

    public CoursesAdapter(Activity activity, List<FeedItem> feedItems, String userId) {
        this.activity = activity;
        this.feedItems = feedItems;
        this.feedItemsFiltered = feedItems;
        this.userId = userId;
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
    public View getView(int position, View convertView, ViewGroup parent) {
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

        item = feedItemsFiltered.get(position);
        name.setText(item.getName());
        description.setText(item.getBodyContent()+" | "+item.getDescription());
        created_by.setText("Created by "+(item.getUserId().equals(userId)?"you":item.getPostTags()));
        if(item.getShowIdentity().equals("null")){//user is a member
            is_member.setVisibility(View.GONE);
        }else{
            is_member.setVisibility(View.VISIBLE);
        }

        /*
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/

        return convertView;
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