package com.example.tumsiniflar;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BugListAdapter extends ArrayAdapter<Complaint> {

	private Context context;
	private ArrayList<Complaint> complaints;

	public BugListAdapter(Context context, ArrayList<Complaint> complaints) {
		//super(c, R.layout.bug_list_item, complaints);
		super(context, R.layout.bug_list_item, complaints);
		
		this.context = context;
		this.complaints = complaints;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater
				.inflate(R.layout.bug_list_item, parent, false);

		ImageView image = (ImageView) rowView.findViewById(R.id.ivImage);
		TextView complaintTitle = (TextView) rowView.findViewById(R.id.tvItem);
		TextView complaintDate = (TextView) rowView.findViewById(R.id.tvVoteState);
		TextView complaintAddress = (TextView) rowView.findViewById(R.id.textView1);
		ImageView imageArrow = (ImageView) rowView.findViewById(R.id.ivArrow);

		image.setImageResource(R.drawable.ic_launcher);
		complaintTitle.setText(complaints.get(position).getTitle());
		complaintDate.setText(complaints.get(position).getDate());
		complaintAddress.setText(complaints.get(position).getAddress());
		imageArrow.setImageResource(R.drawable.right_arrow);
		
		return rowView;

	}

}
