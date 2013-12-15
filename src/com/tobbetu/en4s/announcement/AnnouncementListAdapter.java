package com.tobbetu.en4s.announcement;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;

public class AnnouncementListAdapter extends ArrayAdapter<Announcement> {

    private final String TAG = "AnnouncementListAdapter";
    private Context ctx;
    private List<Announcement> announcements;

    public AnnouncementListAdapter(Context context, List<Announcement> list) {
        super(context, R.layout.announcement_list_item, list);

        this.ctx = context;
        this.announcements = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.announcement_list_item,
                    parent, false);

            holder = new ViewHolder();
            holder.ivAnnouncementIcon = (ImageView) convertView
                    .findViewById(R.id.ivAnnouncementIcon);
            holder.tvAnnouncementTitle = (TextView) convertView
                    .findViewById(R.id.tvAnnouncementTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String a = announcements.get(position).getIcon();
        Log.e(TAG, a);

        holder.ivAnnouncementIcon.setImageResource(R.drawable.loading);

        announcements.get(position).getImage(
                announcements.get(position).getIcon(), Image.SIZE_ORIG,
                holder.ivAnnouncementIcon);

        holder.tvAnnouncementTitle.setText(announcements.get(position)
                .getTitle());

        return convertView;
    }

    private static class ViewHolder {

        ImageView ivAnnouncementIcon;
        TextView tvAnnouncementTitle;
    }
}
