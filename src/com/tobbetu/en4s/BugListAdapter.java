package com.tobbetu.en4s;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.Login;
import com.tobbetu.en4s.backend.User;

public class BugListAdapter extends ArrayAdapter<Complaint> {

    private final String TAG = "BugListAdapter";
    private final Context context;
    private final List<Complaint> complaints;
    private final int tabPosition;
    private final double latitude;
    private final double longitude;
    private Complaint complaint;
    private final User user = Login.getMe();
    private ImageView ivUp;

    public BugListAdapter(Context context, List<Complaint> complaints, int pos,
            double lat, double lon) {
        // super(c, R.layout.bug_list_item, complaints);
        super(context, R.layout.bug_list_item, complaints);

        this.context = context;
        this.complaints = complaints;
        this.tabPosition = pos;
        this.latitude = lat;
        this.longitude = lon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.bug_list_item, parent, false);

        ImageView problemImage = (ImageView) rowView
                .findViewById(R.id.ivProblemImage);
        TextView complaintTitle = (TextView) rowView.findViewById(R.id.tvItem);
        TextView tvAdditionalInfo = (TextView) rowView
                .findViewById(R.id.tvDate);
        TextView tvUpVoteCount = (TextView) rowView
                .findViewById(R.id.tvUpVoteCount);
        TextView tvCommentCount = (TextView) rowView
                .findViewById(R.id.tvCommentCount);

        complaint = complaints.get(position);

        problemImage.setImageResource(R.drawable.loading);
        complaint.getImage(0, Image.SIZE_512, problemImage);

        complaintTitle.setText(complaint.getTitle().trim());
        tvUpVoteCount.setText("" + complaint.getUpVote());
        tvCommentCount.setText("" + complaint.getCommentsCount());

        ivUp = (ImageView) rowView.findViewById(R.id.ivUp);

        try {
            if (complaint.alreadyUpVoted(user))
                ivUp.setImageResource(R.drawable.up_voted);
        } catch (Exception e) {
            Log.e(TAG, "alreadyUpVote calismadi");
        }

        // position
        if (tabPosition == 0) {
            tvAdditionalInfo.setText(R.string.bla_hot);
        } else if (tabPosition == 1) {
            tvAdditionalInfo.setText(complaint.getDateAsString(this.context));
        } else if (tabPosition == 2) {
            tvAdditionalInfo.setText(complaint.getDistance(this.context,
                    latitude, longitude));
        } else {
            tvAdditionalInfo
                    .setText(String.format(context.getString(R.string.bla_top),
                            complaint.getUpVote()));
        }

        return rowView;
    }
}
