package com.tobbetu.en4s;

import java.util.List;

import com.tobbetu.en4s.backend.Complaint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BugListAdapter extends ArrayAdapter<Complaint> {

	private Context context;
	private List<Complaint> complaints;
	private int tabPosition;

	public BugListAdapter(Context context, List<Complaint> complaints, int pos) {
		//super(c, R.layout.bug_list_item, complaints);
		super(context, R.layout.bug_list_item, complaints);
		
		this.context = context;
		this.complaints = complaints;
		this.tabPosition = pos;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater
				.inflate(R.layout.bug_list_item, parent, false);

		ImageView image = (ImageView) rowView.findViewById(R.id.ivImage);
		TextView complaintTitle = (TextView) rowView.findViewById(R.id.tvItem);
		//TextView complaintVote = (TextView) rowView.findViewById(R.id.tvVoteState);
		
		TextView complaintAddress = (TextView) rowView.findViewById(R.id.tvComplaintAddress);
		TextView complaintUpVote = (TextView) rowView.findViewById(R.id.upVoteTW);
		TextView complaintDownVote = (TextView) rowView.findViewById(R.id.downVoteTW);
		
		TextView tvAdditionalInfo = (TextView) rowView.findViewById(R.id.tvAdditionalInfo);
		
		//ImageView imageArrow = (ImageView) rowView.findViewById(R.id.ivArrow);

		Log.e("Type -> ", complaints.get(position).getCategory());
		if(complaints.get(position).getCategory().equals("Traffic")){
			image.setImageResource(R.drawable.type1);
		}
		else if(complaints.get(position).getCategory().equals("Infrastructure")){
			image.setImageResource(R.drawable.type2);
		}
		else if(complaints.get(position).getCategory().equals("Environment")){
			image.setImageResource(R.drawable.type3);
		}
		else if(complaints.get(position).getCategory().equals("Disable")){
			image.setImageResource(R.drawable.type4);
		}
		else{
			image.setImageResource(R.drawable.type5);
		}
		
		//image.setImageResource(R.drawable.ic_launcher);
		if(complaints.get(position).getTitle().length() > 20){
			complaintTitle.setText(complaints.get(position).getTitle().trim().substring(0, 20) + "...");
		}
		else{
			complaintTitle.setText(complaints.get(position).getTitle().trim());
		}
		complaintAddress.setText("at " + complaints.get(position).getCity().toUpperCase());
		complaintUpVote.setText("10");
		complaintDownVote.setText("12");
		
		
		//position
		if(tabPosition == 0) {
			tvAdditionalInfo.setText("Hot ile ilgili bilgi");
		} else if (tabPosition == 1) {
			tvAdditionalInfo.setText("New ile ilgili bilgi");
		} else if (tabPosition == 2) {
			tvAdditionalInfo.setText("Near ile ilgili bilgi");
		} else {
			tvAdditionalInfo.setText("Top ile ilgili bilgi");
		}
		
		
		//complaintVote.setText(complaints.get(position).getDate());
		//complaintAddress.setText(complaints.get(position).getAddress());
		//imageArrow.setImageResource(R.drawable.right_arrow);
		
		return rowView;

	}

}
