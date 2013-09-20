package com.tobbetu.en4s;

import java.io.ByteArrayOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.helpers.CategoryI18n;
import com.tobbetu.en4s.helpers.Preview;
import com.tobbetu.en4s.service.EnforceService;

public class NewComplaint extends Activity implements OnClickListener {

	private Button bPush, bImproveLocation;
	private ImageView ivTakenPhoto;
	private EditText etComplaintTitle;
	private Spinner categoriesSpinner;
	private ProgressDialog progressDialog = null;

	private LinearLayout photoButtonLL;
	
	private GoogleMap myMap;
	private LatLng position = null;

	private Image img = null;
	private Bitmap bmp = null;
	private byte[] bitmapdata = null;

	private Complaint newComplaint = null;
	private int selectedCategoryIndex = 0;

	private final String TAG = "NewComplaint";

	private Preview preview;
	ProgressDialog pg = null;

	private static double latitude = 0;
	private static double longitude = 0;

	private OrientationEventListener mOrientationListener = null;
	private int deviceOrientation, photoOrientation;
	private int frameWidth;
	private byte[] bytearrays;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_complaint);
		getActionBar().hide();
		
		photoButtonLL = (LinearLayout) findViewById(R.id.photoButtonLL);
		ivTakenPhoto = new ImageView(getBaseContext());

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = width; // Kare olacaðý için height = width
        
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width-30, height-30, Gravity.CENTER);
        photoButtonLL.addView(ivTakenPhoto, layoutParams);
		
		// klavye kendi kendine acilmayacak...Oh beeee :D
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		String savedComplainTitle = getIntent()
				.getStringExtra("complaintTitle");
		selectedCategoryIndex = getIntent().getIntExtra("complaintCategory", 0);

		final TextView tvCount = (TextView) findViewById(R.id.tvWordCount);
		etComplaintTitle = (EditText) findViewById(R.id.etNewComplaint);
		etComplaintTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				tvCount.setText(100 - etComplaintTitle.getText().length() + "");
			}
		});

		
		bytearrays = getIntent().getByteArrayExtra("taken_photo");
		if (bytearrays.length > 0) {
			 bmp = BitmapFactory.decodeByteArray(bytearrays, 0, bytearrays.length);
			 ivTakenPhoto.setImageBitmap(bmp);
			 
			 img = new Image();
			 img.setBmp(bmp);
		}
		
		bPush = (Button) findViewById(R.id.bPush);
		bImproveLocation = (Button) findViewById(R.id.bImproveLoc);

		bPush.setOnClickListener(this);
		bImproveLocation.setOnClickListener(this);

		byte[] savedImage = getIntent().getByteArrayExtra("complaintImage");
		Bitmap savedBitmap = null;
		if (savedImage != null) {
			savedBitmap = BitmapFactory.decodeByteArray(savedImage, 0,
					savedImage.length);

			img = new Image();
			img.setBmp(savedBitmap);

			// resim cekildikten sonra ikinci kez haritadan konum secilirse
			// mevcut foograf korunmali.
			bitmapdata = savedImage;

			ivTakenPhoto.setVisibility(ImageView.VISIBLE);
			
			ivTakenPhoto.setImageBitmap(savedBitmap);

			savedBitmap = null;
		}

		myMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mapNewComplaint)).getMap();
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		position = new LatLng(EnforceService.getLocation().getLatitude(),
				EnforceService.getLocation().getLongitude());

		// user konumu begenmeyip, biggermap aktivitesini acarak konuma set
		// ederse, bu kod parcasi calisacak ve konumu user in elle sectigi konum
		// olarak set edecek
		if (!Double.isNaN(getIntent().getDoubleExtra("user_lat", Double.NaN)))
			position = new LatLng(getIntent().getDoubleExtra("user_lat",
					Double.NaN), getIntent().getDoubleExtra("user_lng",
							Double.NaN));

		Utils.addAMarker(myMap, position, false);
		Utils.centerAndZomm(myMap, position, 15);

		categoriesSpinner = (Spinner) findViewById(R.id.spinnerNewComplaintCategory);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.categories,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(spinnerAdapter);
		categoriesSpinner
		.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedCategoryIndex = arg2;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				selectedCategoryIndex = 0;

			}
		});

		etComplaintTitle.setText(savedComplainTitle);
		categoriesSpinner.setSelection(selectedCategoryIndex);

		mOrientationListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				deviceOrientation = orientation;
			}
		};
		if (mOrientationListener.canDetectOrientation())
			mOrientationListener.enable();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mOrientationListener.disable();

		finish();
		Log.d(TAG, "in onStop");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_complaint, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bPush) {

			if (etComplaintTitle.getText().toString().equals(""))
				Toast.makeText(NewComplaint.this,
						getResources().getString(R.string.nc_missing_title),
						Toast.LENGTH_LONG).show();
			else if (selectedCategoryIndex == 0) {
				Toast.makeText(NewComplaint.this,
						getResources().getString(R.string.nc_cat_missing_msg),
						Toast.LENGTH_LONG).show();
			} else if (img == null) {
				Toast.makeText(NewComplaint.this,
						getResources().getString(R.string.nc_missing_photo),
						Toast.LENGTH_LONG).show();
			} else {
				progressDialog = ProgressDialog.show(NewComplaint.this,
						getString(R.string.nc_cat_loading),
						getString(R.string.nc_cat_sending));

				newComplaint = new Complaint();
				newComplaint.setTitle(etComplaintTitle.getText().toString());

				newComplaint.setAddress("");

				newComplaint.setCity("");
				newComplaint.setCategory(CategoryI18n
						.getEnglishName(selectedCategoryIndex));

				newComplaint.setLatitude(latitude);
				newComplaint.setLongitude(longitude);

				if (!Double.isNaN(getIntent().getDoubleExtra("user_lat",
						Double.NaN))) {
					newComplaint.setLatitude(getIntent().getDoubleExtra(
							"user_lat", 0));
					newComplaint.setLongitude(getIntent().getDoubleExtra(
							"user_lng", 0));

				}

				Log.d("title", newComplaint.getTitle());
				Log.d("category", newComplaint.getCategory());
				Log.d("address", newComplaint.getAddress());
				Log.d("city", newComplaint.getCity());
				Log.d("location", newComplaint.getLatitude() + ","
						+ newComplaint.getLongitude());

				new SaveTask().execute();
			}
		} else { // bImroveLocation
			Intent biggerMapIntent = new Intent(NewComplaint.this,
					BiggerMap.class);

			biggerMapIntent.putExtra("complaintTitle", etComplaintTitle
					.getText().toString());
			biggerMapIntent
			.putExtra("complaintCategory", selectedCategoryIndex);
			biggerMapIntent.putExtra("complaintImage", bitmapdata);
			biggerMapIntent.putExtra("taken_photo", bytearrays);
			
			Log.d(TAG, "onMapClick intent started");
			startActivity(biggerMapIntent);
		}

	}

	private class SaveTask extends BetterAsyncTask<Void, Complaint> {

		@Override
		protected Complaint task(Void... arg0) throws Exception {
			Complaint savedComplaint = newComplaint.save();
			String url = img.upload(savedComplaint.getId());
			savedComplaint.addJustUploadedImage(url);
			return savedComplaint;
		}

		@Override
		protected void onSuccess(Complaint result) {
			progressDialog.dismiss();

			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.nc_accepted),
					Toast.LENGTH_SHORT).show();

			Intent anIntent = new Intent(NewComplaint.this,
					DetailsActivity.class);
			anIntent.putExtra("class", result);
			startActivity(anIntent);
		}

		@Override
		protected void onFailure(Exception error) {
			Log.e(TAG, "SaveTask Failed", error);
			Toast.makeText(NewComplaint.this,
					getResources().getString(R.string.nc_compalint_rejected),
					Toast.LENGTH_LONG).show();
		}
	}
}
