package com.tobbetu.en4s;

import java.util.List;

import android.annotation.SuppressLint;
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

public class BugListAdapter extends ArrayAdapter<Complaint> {

	private Context context;
	private List<Complaint> complaints;
	private int tabPosition;
	private double latitude;
	private double longitude;

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

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.bug_list_item, parent, false);

		ImageView problemImage = (ImageView) rowView.findViewById(R.id.ivProblemImage);
		TextView complaintTitle = (TextView) rowView.findViewById(R.id.tvItem);
		TextView tvAdditionalInfo = (TextView) rowView.findViewById(R.id.tvDate);
		TextView tvUpVoteCount = (TextView) rowView.findViewById(R.id.tvUpVoteCount);
//		TextView tvDownVoteCount = (TextView) rowView.findViewById(R.id.tvDownVoteCount);
		
		
//		TextView complaintAddress = (TextView) rowView
//				.findViewById(R.id.tvComplaintAddress);
//		TextView complaintUpVote = (TextView) rowView
//				.findViewById(R.id.tvUpVoteCount);
//		TextView complaintDownVote = (TextView) rowView
//				.findViewById(R.id.tvDownVoteCount);
//		TextView tvAdditionalInfo = (TextView) rowView
//				.findViewById(R.id.tvAdditionalInfo);

		Complaint complaint = complaints.get(position);
		Log.e("Type -> ", complaint.getCategory());
//		if (complaint.getCategory().equals("Traffic")) {
//			problemImage.setImageResource(R.drawable.type1);
//		} else if (complaint.getCategory().equals("Infrastructure")) {
//			problemImage.setImageResource(R.drawable.type2);
//		} else if (complaint.getCategory().equals("Environment")) {
//			problemImage.setImageResource(R.drawable.type3);
//		} else if (complaint.getCategory().equals("Disability Rights")) {
//			problemImage.setImageResource(R.drawable.type4);
//		} else {
//			problemImage.setImageResource(R.drawable.type5);
//		}
		
		problemImage.setImageResource(R.drawable.loading);
		complaint.getImage(0, Image.SIZE_512, problemImage);
//		new ImageTask().execute(complaint, problemImage);


		complaintTitle.setText(complaint.getTitle().trim());
		tvUpVoteCount.setText("" + complaint.getUpVote());
//		tvDownVoteCount.setText("" + complaint.getDownVote());

//		complaintAddress.setText("at " + complaint.getCity().toUpperCase());
//		complaintUpVote.setText(complaint.getUpVote());
//		complaintDownVote.setText(complaint.getDownVote());

		// position
		if (tabPosition == 0) {
			tvAdditionalInfo.setText("Hot ile ilgili bilgi");
		} else if (tabPosition == 1) {
			tvAdditionalInfo.setText(complaint.getDateAsString());
		} else if (tabPosition == 2) {
			tvAdditionalInfo
					.setText(complaint.getDistance(latitude, longitude));
		} else {
			tvAdditionalInfo.setText(complaint.getUpVote() + " kez beðenildi");
		}

		// complaintVote.setText(complaints.get(position).getDate());
		// complaintAddress.setText(complaints.get(position).getAddress());
		// imageArrow.setImageResource(R.drawable.right_arrow);

		return rowView;

	}

}
