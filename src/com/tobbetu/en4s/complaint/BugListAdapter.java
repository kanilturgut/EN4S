package com.tobbetu.en4s.complaint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.bugsense.trace.BugSenseHandler;
import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.backend.User;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.login.Login;
import com.tobbetu.en4s.service.EnforceService;

import java.util.List;

public class BugListAdapter extends ArrayAdapter<Complaint> {

    private final String TAG = "BugListAdapter";
    private final Context context;
    private final List<Complaint> complaints;
    private final int tabPosition;
    private Complaint complaint;
    private final User user = Login.getMe();

    AQuery aQuery = null;

    public BugListAdapter(Context context, List<Complaint> complaints, int pos) {

        super(context, R.layout.bug_list_item, complaints);

        this.context = context;
        this.complaints = complaints;
        this.tabPosition = pos;

        aQuery = new AQuery(context);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (position == complaints.size() - 1) {
            loadMore();
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
            holder.tvDownVoteCount = (TextView) convertView
                    .findViewById(R.id.tvDownVoteCount);
            holder.tvCommentCount = (TextView) convertView
                    .findViewById(R.id.tvCommentCount);
            holder.upvoteImage = (ImageView) convertView
                    .findViewById(R.id.ivUp);
            holder.downvoteImage = (ImageView) convertView
                    .findViewById(R.id.ivDown);
            holder.tvComplaintCity = (TextView) convertView
                    .findViewById(R.id.tvComplaintCity);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        complaint = complaints.get(position);

        holder.problemImage.setImageResource(R.drawable.loading);
        complaint.getImage(0, Image.SIZE_512, holder.problemImage, context);

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int tmpWidth = size.x;

        RelativeLayout.LayoutParams llParams = new RelativeLayout.LayoutParams(
                tmpWidth, tmpWidth);
        convertView.findViewById(R.id.complaintItemInfoLayout1).setLayoutParams(
                llParams);
        holder.problemImage.setLayoutParams(llParams);

        holder.complaintTitle.setText(complaint.getTitle().substring(0, 1)
                .toUpperCase()
                + complaint.getTitle().substring(1).trim());
        holder.tvUpVoteCount.setText(String.valueOf(complaint.getUpVote()));
        holder.tvDownVoteCount.setText(String.valueOf(complaint.getDownVote()));
        holder.tvCommentCount.setText(String.valueOf(complaint.getCommentsCount()));

        if (complaint.alreadyUpVoted(user))
            holder.upvoteImage.setImageResource(R.drawable.upvote_green);
        else
            holder.upvoteImage.setImageResource(R.drawable.upvote);

        if (complaint.alreadyDownVoted(user))
            holder.downvoteImage.setImageResource(R.drawable.downvote_red);
        else
            holder.downvoteImage.setImageResource(R.drawable.downvote);

        holder.tvComplaintCity.setText(complaint.getCity());

        try {
            holder.tvAdditionalInfo.setText(complaint
                    .getDateAsString(this.context) + " / " + complaint.getDistance(this.context,
                    EnforceService.getLocation().getLatitude(), EnforceService
                            .getLocation().getLongitude()));
        } catch (Exception e) {
            Log.e(TAG, "ERROR occured for additionalInfo", e);
            BugSenseHandler.sendException(e);
        }
        return convertView;
    }

    private void loadMore() {
        new ComplaintListTask().execute();
    }

    private class ComplaintListTask extends
            BetterAsyncTask<Void, List<Complaint>> {

        @Override
        protected List<Complaint> task(Void... arg0) throws Exception {
            String sinceId = complaints.get(complaints.size() - 1).getId();
            Log.d(TAG, "Loading more -> " + sinceId);

            switch (tabPosition) {
                case 0: // Hot
                    return Complaint.getHotList(sinceId);
                case 1: // New
                    return Complaint.getNewList(sinceId);
                case 2: // Near
                    return Complaint.getNearList(sinceId, EnforceService
                            .getLocation().getLatitude(), EnforceService
                            .getLocation().getLongitude());
                case 3: // Top
                    return Complaint.getTopList(sinceId);
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        protected void onSuccess(List<Complaint> result) {
            complaints.addAll(result);
            BugListAdapter.this.notifyDataSetChanged();
        }

        @Override
        protected void onFailure(Exception error) {
            // TODO Auto-generated method stub
        }
    }

    static class ViewHolder {

        ImageView problemImage;
        ImageView upvoteImage;
        ImageView downvoteImage;
        TextView complaintTitle;
        TextView tvAdditionalInfo;
        TextView tvUpVoteCount;
        TextView tvDownVoteCount;
        TextView tvCommentCount;
        TextView tvComplaintCity;
    }
}
