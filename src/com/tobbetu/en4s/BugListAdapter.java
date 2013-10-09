package com.tobbetu.en4s;

import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.service.EnforceService;

public class BugListAdapter extends ArrayAdapter<Complaint> {

    private final String TAG = "BugListAdapter";
    private final Context context;
    private final List<Complaint> complaints;
    private final int tabPosition;
    private Complaint complaint;
    private final User user = Login.getMe();
    private ImageView ivUp;

    public BugListAdapter(Context context, List<Complaint> complaints, int pos) {

        super(context, R.layout.bug_list_item, complaints);

        this.context = context;
        this.complaints = complaints;
        this.tabPosition = pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bug_list_item, parent,
                    false);

            holder = new ViewHolder();
            holder.problemImage = (ImageView) convertView
                    .findViewById(R.id.ivProblemImage);
            holder.complaintTitle = (TextView) convertView
                    .findViewById(R.id.tvItem);
            holder.tvAdditionalInfo = (TextView) convertView
                    .findViewById(R.id.tvDate);
            holder.tvUpVoteCount = (TextView) convertView
                    .findViewById(R.id.tvUpVoteCount);
            holder.tvCommentCount = (TextView) convertView
                    .findViewById(R.id.tvCommentCount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        complaint = complaints.get(position);

        holder.problemImage.setImageResource(R.drawable.loading);
        complaint.getImage(0, Image.SIZE_512, holder.problemImage);

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int tmpWidth = size.x;

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                tmpWidth, tmpWidth);
        convertView.findViewById(R.id.complaintItemInfoLayout).setLayoutParams(
                llParams);
        holder.problemImage.setLayoutParams(llParams);

        holder.complaintTitle.setText(complaint.getTitle().substring(0, 1)
                .toUpperCase()
                + complaint.getTitle().substring(1).trim());
        holder.tvUpVoteCount.setText("" + complaint.getUpVote());
        holder.tvCommentCount.setText("" + complaint.getCommentsCount());

        ivUp = (ImageView) convertView.findViewById(R.id.ivUp);

        if (complaint.alreadyVoted(user))
            ivUp.setImageResource(R.drawable.up_voted);

        // position
        if (tabPosition == 0) {
            holder.tvAdditionalInfo.setText(R.string.bla_hot);
        } else if (tabPosition == 1) {
            holder.tvAdditionalInfo.setText(complaint
                    .getDateAsString(this.context));
        } else if (tabPosition == 2) {
            holder.tvAdditionalInfo.setText(complaint.getDistance(this.context,
                    EnforceService.getLocation().getLatitude(), EnforceService
                            .getLocation().getLongitude()));
        } else {
            holder.tvAdditionalInfo
                    .setText(String.format(context.getString(R.string.bla_top),
                            complaint.getUpVote()));
        }

        return convertView;
    }

    static class ViewHolder {

        ImageView problemImage;
        TextView complaintTitle;
        TextView tvAdditionalInfo;
        TextView tvUpVoteCount;
        TextView tvCommentCount;

    }
}
