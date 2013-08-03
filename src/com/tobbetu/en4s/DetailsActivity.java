/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.tobbetu.en4s;

import java.io.IOException;

import uk.co.senab.photoview.PhotoView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;

@SuppressLint("NewApi")
public class DetailsActivity extends Activity implements OnClickListener {

	private TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory,
	tvReporter, tvReporterDate, tvYouAreNotAllowed;
	private Button bUpVote, bDownVote, bMoreComment, bShare;
	private LinearLayout viewPagerLayout;

	private ViewPager mViewPager;
	private PhotoView photoView;
	private Bitmap bmp;

	private Utils util = null;

	private GoogleMap myMap;

	private Complaint comp = null;
	private LatLng compPos = null;
	LatLng myPosition = null;
	private Bitmap cropped = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.details_layout);
//		getActionBar().hide();

		util = new Utils();

		comp = (Complaint) getIntent().getSerializableExtra("class");

		viewPagerLayout = (LinearLayout) findViewById(R.id.viewPagerLayout);

		tvComplaintAdress = (TextView) findViewById(R.id.tvComplaintAdress);
		tvComplaintTitle = (TextView) findViewById(R.id.tvComplaintTitle);
		tvComplaintCategory = (TextView) findViewById(R.id.tvComplaintCategory);

		tvReporter = (TextView) findViewById(R.id.tvReporter);
		tvReporterDate = (TextView) findViewById(R.id.tvReporterDate);

		bUpVote = (Button) findViewById(R.id.bUpVote);
		bDownVote = (Button) findViewById(R.id.bDownVote);
		bMoreComment = (Button) findViewById(R.id.bMoreComment);
		bShare = (Button) findViewById(R.id.bShare);
		bUpVote.setOnClickListener(this);
		bDownVote.setOnClickListener(this);
		bMoreComment.setOnClickListener(this);
		bShare.setOnClickListener(this);

		compPos = new LatLng(comp.getLatitude(), comp.getLongitude());
		myPosition = new LatLng(getIntent().getDoubleExtra(
				"latitude", 0), getIntent().getDoubleExtra("longitude", 0));
		if (!util.isNear(myPosition, compPos)) {
			bUpVote.setVisibility(View.GONE);
			bDownVote.setVisibility(View.GONE);

			tvYouAreNotAllowed = (TextView) findViewById(R.id.tvYouAreNotAllowed);
			tvYouAreNotAllowed.setVisibility(View.VISIBLE);

		}

		mViewPager = new HackyViewPager(this);
		viewPagerLayout.addView(mViewPager);
		mViewPager.setAdapter(new SamplePagerAdapter());

		myMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mapDetails)).getMap();
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		util.addAMarker(myMap, compPos, false);
		util.centerAndZomm(myMap, compPos, 15);
		tvComplaintAdress.setText(comp.getAddress());
		tvComplaintTitle.setText(comp.getTitle());
		tvComplaintCategory.setText(comp.getCategory());

		tvReporter.setText(comp.getReporter());
		tvReporterDate.setText(comp.getDateAsString());
		
		String sporDallari[] = {"Basketbol", "Futbol", "Tenis", "Voleybol",
	            "Hentbol", "Yüzme", "Golf"};
		ListView lvComments = (ListView) findViewById(R.id.lvCommentOnDetails);
		lvComments.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,sporDallari));
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		/*Bu sekilde detay sayfasina girilmis olan bir sikayetin, resmi yuklenmeden detay sayfasindan cikinca
		 * arka plan da calisan resmi indirme gorevi iptal ediliyor. Boylece kapanma hatasi almiyoruz.*/
		
	}
	
//	@Override
//	public void onBackPressed() {
//		super.onBackPressed();
//		
//		/**/
//		Intent i = new Intent(this, MainActivity.class);
//		i.putExtra("latitude", myPosition.latitude);
//		i.putExtra("longitude", myPosition.longitude);
//		startActivity(i);
//	}
	
	class SamplePagerAdapter extends PagerAdapter {

		// private int[] sDrawables = { R.drawable.img1, R.drawable.img2,
		// R.drawable.img3 };

		@Override
		public int getCount() {
			return comp.imageCount();
		}

		@Override
		public View instantiateItem(final ViewGroup container,
				final int position) {
			photoView = new PhotoView(container.getContext());

			//			photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
			//				@Override
			//				public void onPhotoTap(View view, float x, float y) {
			//					Toast.makeText(container.getContext(), "tï¿½klandï¿½",
			//							Toast.LENGTH_SHORT).show();
			//					//Log.e("image", "tik");
			//					Intent intent = new Intent(getApplication(),
			//							FullScreenPhotoActivity.class);
			//					
			//					ByteArrayOutputStream stream = new ByteArrayOutputStream();
			//					bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
			//					byte[] byteArray = stream.toByteArray();
			//					intent.putExtra("image", byteArray);
			//					startActivity(intent);
			//				}
			//			});

			// photoView.setOnTouchListener(new OnTouchListener() {
			//
			// @Override
			// public boolean onTouch(View v, MotionEvent event) {
			// ScrollView scrollView = (ScrollView) findViewById(R.id.scroller);
			// scrollView.setEnabled(false);
			// return false;
			// }
			// });

			// Now just add PhotoView to ViewPager and return it
			photoView.setScaleType(ScaleType.FIT_XY);
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			photoView.setImageResource(R.drawable.content_picture);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

//	class ImageTask extends AsyncTask<Integer, String, String> {
//
//		@Override
//		protected String doInBackground(Integer... params) {
//
//			try {
//				Log.d("ImageTask", "istek yapildi1");
//				bmp = comp.getImage(params[0]).getBmp();
//				Log.d("ImageTask", "istek yapildi");
//
//			} catch (IOException e) {
//				Log.e(getClass().getName(), "Image couldn't load", e);
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//
//			Display display = getWindowManager().getDefaultDisplay();
//			Point size = new Point();
//			display.getSize(size);
//			int tmpWidth = size.x;
//			int tmpHeight = viewPagerLayout.getLayoutParams().height;
//
//			if(tmpWidth > 600){
//				tmpWidth = bmp.getWidth();
//				tmpHeight = (int) ((double)(bmp.getWidth() / tmpWidth) * tmpHeight);
//			}
//
//			Log.e(getClass().getName(), "tmp.width:" + tmpWidth + " , tmpheight : " + tmpHeight + "...." + "bmpW: " + bmp.getWidth() + " , bmpH : " + bmp.getHeight());
//			cropped = Bitmap.createBitmap(bmp, 0, 150, tmpWidth, tmpHeight);
//			Log.e("burasi", "croppe width : " + cropped.getWidth() + " cropped height : " + cropped.getHeight());
//			photoView.setImageBitmap(cropped);
//		}
//	}

	// Buttons onClickListener
	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bUpVote) {
			new UpVoteTask().execute();
		} else if (v.getId() == R.id.bDownVote) {
			new DownVoteTask().execute();
		} else if(v.getId() == R.id.bShare) {
//			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//			sharingIntent.setType("text/plain");	
//			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "selam");
//			startActivity(Intent.createChooser(sharingIntent, "Share via"));
			publishFeedDialog(); 
		} else { //bMoreComment
			Intent i = new Intent(this, MoreCommentsActivity.class);
			i.putExtra("class", comp);
			i.putExtra("latitude", comp.getLatitude());
			i.putExtra("longitude", comp.getLongitude());
			// ayrica burada liste icerigini de intent icine yerlestirmeliyiz yada baska bir yontem
			startActivity(i);
		}

	}
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", "Enforce");
	    params.putString("caption", "Let's Create Better Cities");
	    params.putString("description", "Enforce is a mobile app for creating better cities.");
	    params.putString("link", "http://enforceapp.com");
	    params.putString("picture", "http://enforceapp.com/static/img/icon.png");

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(DetailsActivity.this,
	                            "Posted story, id: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getApplicationContext(), 
	                        "Publish cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	

	}
	
	class UpVoteTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			
			try {
				Log.d(getClass().getName(), "In UpVoteTask doInbackground");
				comp.upvote(Utils.locationToJSON(myPosition.latitude, myPosition.longitude));
			} catch (IOException e) {
				Log.e(getClass().getName(), "UpVoteTask failed", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.d(getClass().getName(), "In UpVoteTask onPostExecute");
			Toast.makeText(getApplicationContext(), "Thanks for your upvote", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	class DownVoteTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			
			try {
				Log.d(getClass().getName(), "In DownVoteTask doInbackground");
				comp.downvote(Utils.locationToJSON(myPosition.latitude, myPosition.longitude));
			} catch (IOException e) {
				Log.e(getClass().getName(), "DownVoteTask failed", e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.d(getClass().getName(), "In DownVoteTask onPostExecute");
			Toast.makeText(getApplicationContext(), "Thanks for your downvote", Toast.LENGTH_SHORT).show();
		}
		
	}	
}