package com.tobbetu.en4s;

import java.io.IOException;
import java.util.List;

import com.tobbetu.en4s.backend.Complaint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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

//		TextView complaintAddress = (TextView) rowView
//				.findViewById(R.id.tvComplaintAddress);
//		TextView complaintUpVote = (TextView) rowView
//				.findViewById(R.id.upVoteTW);
//		TextView complaintDownVote = (TextView) rowView
//				.findViewById(R.id.downVoteTW);
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
		
		problemImage.setImageResource(R.drawable.resim1);
//		new ImageTask().execute(complaint, problemImage);


		complaintTitle.setText(complaint.getTitle().trim());

//		complaintAddress.setText("at " + complaint.getCity().toUpperCase());
//		complaintUpVote.setText("" + complaint.getUpVote());
//		complaintDownVote.setText("" + complaint.getDownVote());

		// position
//		if (tabPosition == 0) {
//			tvAdditionalInfo.setText("Hot ile ilgili bilgi");
//		} else if (tabPosition == 1) {
//			tvAdditionalInfo.setText(complaint.getDateAsString());
//		} else if (tabPosition == 2) {
//			tvAdditionalInfo
//					.setText(complaint.getDistance(latitude, longitude));
//		} else {
//			tvAdditionalInfo.setText("Top ile ilgili bilgi");
//		}

		// complaintVote.setText(complaints.get(position).getDate());
		// complaintAddress.setText(complaints.get(position).getAddress());
		// imageArrow.setImageResource(R.drawable.right_arrow);

		return rowView;

	}
	
	private class ImageTask extends AsyncTask<Object, Bitmap, Bitmap> {
		
		private ImageView v;
		
		@Override
		protected Bitmap doInBackground(Object... arg0) {
			Complaint complaint = (Complaint) arg0[0];
			v = (ImageView) arg0[1];
			Bitmap tmp;
			try {
				tmp =  complaint.getImage(0).getBmp();
			} catch (IOException e) {
				tmp = null;
			}
			// TODO Auto-generated method stub
			return tmp;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			v.setImageBitmap(result);
		}
		
	}

}
