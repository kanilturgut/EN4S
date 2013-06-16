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
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;

@SuppressLint("NewApi")
public class DetailsActivity extends Activity implements OnClickListener {

	private TextView tvComplaintAdress, tvComplaintTitle, tvComplaintCategory,
			tvReporter, tvReporterDate, tvYouAreNotAllowed;
	private Button bUpVote, bDownVote;

	private ViewPager mViewPager;
	private PhotoView photoView;
	private Bitmap bmp;

	private Utils util = null;

	private GoogleMap myMap;

	private Complaint comp = null;
	private LatLng compPos = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.details_layout);
		getActionBar().hide();

		util = new Utils();

		comp = (Complaint) getIntent().getSerializableExtra("class");

		tvComplaintAdress = (TextView) findViewById(R.id.tvComplaintAdress);
		tvComplaintTitle = (TextView) findViewById(R.id.tvComplaintTitle);
		tvComplaintCategory = (TextView) findViewById(R.id.tvComplaintCategory);

		tvReporter = (TextView) findViewById(R.id.tvReporter);
		tvReporterDate = (TextView) findViewById(R.id.tvReporterDate);
		
		bUpVote = (Button) findViewById(R.id.bUpVote);
		bDownVote = (Button) findViewById(R.id.bDownVote);
		bUpVote.setOnClickListener(this);
		bDownVote.setOnClickListener(this);
		
		compPos = new LatLng(comp.getLatitude(), comp.getLongitude());
		LatLng myPosition = new LatLng(getIntent().getDoubleExtra(
				"latitude", 0), getIntent().getDoubleExtra("longitude", 0));
		if (!util.calculateDistance(myPosition, compPos)) {
			bUpVote.setVisibility(View.GONE);
			bDownVote.setVisibility(View.GONE);
			
			tvYouAreNotAllowed = (TextView) findViewById(R.id.tvYouAreNotAllowed);
			tvYouAreNotAllowed.setVisibility(View.VISIBLE);
			
		}

		mViewPager = new HackyViewPager(this);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.viewPagerLayout);
		linearLayout.addView(mViewPager);
		mViewPager.setAdapter(new SamplePagerAdapter());

		new ImageTask().execute(0);

		myMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mapDetails)).getMap();
		myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		/* Cakma adresi yaratiyorum */

		// Burada AsyncTask ile serverdan sorun bilgisi alinip, butun bilgiler
		// set edilecek

		
		util.addAMarker(myMap, compPos);
		util.centerAndZomm(myMap, compPos, 15);
		tvComplaintAdress.setText(comp.getAddress());
		tvComplaintTitle.setText(comp.getTitle());
		tvComplaintCategory.setText(comp.getCategory());

		tvReporter.setText(comp.getReporter());
		tvReporterDate.setText(comp.getDate());

		/* Cakma adres yaratma biter */

	}

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

			// photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
			// @Override
			// public void onPhotoTap(View view, float x, float y) {
			// //Toast.makeText(container.getContext(), "týklandý",
			// Toast.LENGTH_SHORT).show();
			// //Log.e("image", "tik");
			// Intent intent = new Intent(getApplication(),
			// FullScreenPhotoActivity.class);
			// intent.putExtra("imageId", sDrawables[position]);
			// startActivity(intent);
			// }
			// });

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

	class ImageTask extends AsyncTask<Integer, String, String> {

		@Override
		protected String doInBackground(Integer... params) {

			try {
				Log.d("ImageTask", "istek yapildi1");
				bmp = comp.getImage(params[0]).getBmp();
				Log.d("ImageTask", "istek yapildi");

			} catch (IOException e) {
				Log.e(getClass().getName(), "Image couldn't load", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			photoView.setImageBitmap(bmp);
		}
	}

	// Buttons onClickListener
	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bUpVote) {

			
			

		} else {

		}

	}
}