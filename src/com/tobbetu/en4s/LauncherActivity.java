package com.tobbetu.en4s;

import uk.co.senab.photoview.PhotoView;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LauncherActivity extends Activity implements OnClickListener {

	private String TAG = "LauncherActivity";
	private ViewPager mViewPager;
	private LinearLayout infoLayout;
	public static SharedPreferences firstTimeControlPref;
	private AlertDialog alertDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		getActionBar().hide();

		/*
		 * Bu blok, program cihaza yuklendikten sonra sadece 1 kere
		 * calisacak ve yeni sikayet ekleme ekraninda kullanacagimiz,
		 * cihazin hangi boyutta fotograf cekecegini (800x600, 1024x768 ...)
		 * belirleyen bilgileri bulacak.
		 */
		firstTimeControlPref = getSharedPreferences("firstTimeController",
				MODE_PRIVATE);
		if (firstTimeControlPref.getBoolean("isThisFirstTime", true) && 
				!firstTimeControlPref.getBoolean("didLogIn", false)) {

			Log.i(TAG,
					"Bir daha burayi gormeyeceksin. Eger gorursen yanlis birsey var demektir.");

			int[] sizes = Utils.deviceSupportedScreenSize();

			SharedPreferences.Editor firstTimeEditor = firstTimeControlPref
					.edit();
			firstTimeEditor.putBoolean("isThisFirstTime", false);
			firstTimeEditor.putInt("deviceWidth", sizes[0]);
			firstTimeEditor.putInt("deviceHeight", sizes[1]);

			firstTimeEditor.apply();
			
			
			mViewPager = new HackyViewPager(this);
			infoLayout = (LinearLayout) findViewById(R.id.enforceInfoImageLayout);
			infoLayout.addView(mViewPager);
			// setContentView(infoLayout);

			mViewPager.setAdapter(new SamplePagerAdapter());

			findViewById(R.id.bLauncherSignup).setOnClickListener(this);
			findViewById(R.id.bLauncherLogin).setOnClickListener(this);
		} else {
			Intent i = new Intent(this, LoginPageActivity.class);
			startActivity(i);
		}
	}

	@Override
	public void onClick(View v) {
		Intent i = null;

		if (v.getId() == R.id.bLauncherLogin)
			i = new Intent(this, LoginPageActivity.class);
		else
			i = new Intent(this, RegisterPageActivity.class);

		startActivity(i);
	}

	@Override
	public void onBackPressed() {
		createAlert();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (alertDialog != null)
			alertDialog.dismiss();
	}
	
	static class SamplePagerAdapter extends PagerAdapter {

		private static int[] sDrawables = { R.drawable.logo,
				R.drawable.houston, R.drawable.lakers };

		@Override
		public int getCount() {
			return sDrawables.length;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImageResource(sDrawables[position]);

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);

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
	
	private void createAlert() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit ?");
		builder.setMessage("Do you really want to quit ?");
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				try {
					System.exit(0);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				try {
					dialog.dismiss();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		alertDialog = builder.create();
		alertDialog.show();
	}
}