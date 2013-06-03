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
package com.example.tumsiniflar;


import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class DetailsActivity extends Activity {

	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.details_layout);
		getActionBar().hide();
		
		mViewPager = new HackyViewPager(this);
		
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.viewPagerLayout);
		linearLayout.addView(mViewPager);
			
		mViewPager.setAdapter(new SamplePagerAdapter());
	}

	class SamplePagerAdapter extends PagerAdapter {

		private int[] sDrawables = { R.drawable.img1, R.drawable.img2, R.drawable.img3 };

		@Override
		public int getCount() {
			return sDrawables.length;
		}

		@Override
		public View instantiateItem(final ViewGroup container, final int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImageResource(sDrawables[position]);
			
			photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
				@Override
				public void onPhotoTap(View view, float x, float y) {
					//Toast.makeText(container.getContext(), "tıklandı", Toast.LENGTH_SHORT).show();
					//Log.e("image", "tik");
					Intent intent = new Intent(getApplication(), FullScreenPhotoActivity.class);
					intent.putExtra("imageId", sDrawables[position]);
					startActivity(intent);
				}
			});
			
//			photoView.setOnTouchListener(new OnTouchListener() {
//				
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					ScrollView scrollView = (ScrollView) findViewById(R.id.scroller);
//					scrollView.setEnabled(false);
//					return false;
//				}
//			});
			
			// Now just add PhotoView to ViewPager and return it
			photoView.setScaleType(ScaleType.FIT_XY);
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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

}