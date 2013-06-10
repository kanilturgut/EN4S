package com.tobbetu.en4s;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;

public class NewComplaint extends Activity implements OnClickListener {

	private Button bPush;
	private ImageButton bTakePic;
	private ImageView ivTakenPhoto;
	private EditText etComplaintTitle;
	private TextView tvNewComplaintAdress;
	private Spinner categoriesSpinner;

	private Utils util = null;

	private GoogleMap myMap;
	// private Marker place = null;
	private LocationManager lManager = null;
	private LatLng position = null;
	private double latitude = 0;
	private double longitude = 0;

	private Image img = null;
	private Bitmap bmp = null;

	private Complaint newComplaint = null;
	private String category;

	private String TAG = "NewComplaint";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_complaint);
		getActionBar().hide();

		util = new Utils();

		etComplaintTitle = (EditText) findViewById(R.id.etNewComplaint);
		tvNewComplaintAdress = (TextView) findViewById(R.id.tvNewComplaintAdress);
		ivTakenPhoto = (ImageView) findViewById(R.id.ivTakenPhoto);
		bTakePic = (ImageButton) findViewById(R.id.bTakePhoto);
		bPush = (Button) findViewById(R.id.bPush);

		bTakePic.setOnClickListener(this);
		bPush.setOnClickListener(this);

		lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();
		lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				mlocListener);

		myMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mapNewComplaint)).getMap();
		myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

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
						category = arg0.getItemAtPosition(arg2).toString();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						category = "All"; // default category
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_complaint, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bTakePhoto) {
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, 0);
		} else {

			if (etComplaintTitle.getText().toString().equals(""))
				Toast.makeText(getApplicationContext(),
						"You have to fill title!", Toast.LENGTH_SHORT).show();
			else {

				newComplaint = new Complaint();
				newComplaint.setTitle(etComplaintTitle.getText().toString());
				newComplaint.setAddress(util.getAddress(getBaseContext(),
						position));
				newComplaint.setCity(util.getCity(getBaseContext(), position));
				newComplaint.setCategory(category);
				newComplaint.setLatitude(latitude);
				newComplaint.setLongitude(longitude);

				Log.d("title", newComplaint.getTitle());
				Log.d("category", newComplaint.getCategory());
				Log.d("address", newComplaint.getAddress());
				Log.d("city", newComplaint.getCity());
				Log.d("location", newComplaint.getLatitude() + ","
						+ newComplaint.getLongitude());

				new SaveTask().execute();
			}
		}

	}

	@Override
	// fotograf
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			bmp = (Bitmap) extras.get("data");

			img = new Image();

			img.setBmp(bmp);

			int height = bmp.getHeight();
			int width = bmp.getWidth();

			Log.e(TAG, "height : " + height + ", width : " + width);

			bTakePic.setVisibility(View.GONE);
			ivTakenPhoto.setVisibility(View.VISIBLE);
			ivTakenPhoto.getLayoutParams().height = height * 3;
			ivTakenPhoto.getLayoutParams().width = width * 3;
			ivTakenPhoto.setImageBitmap(bmp);

		}

	}

	public class MyLocationListener implements LocationListener { // location

		@Override
		public void onLocationChanged(Location loc) {

			latitude = loc.getLatitude();
			longitude = loc.getLongitude();

			position = new LatLng(loc.getLatitude(), loc.getLongitude());

			// if (place != null) //surekli yeni marker eklememek icin..
			// place.remove();
			//
			// place = myMap.addMarker(new MarkerOptions().position(position));
			// //konuma marker koyar
			util.addAMarker(myMap, position);

			// myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,
			// 15)); //konumu haritada ortalayip, zoom yapar
			util.centerAndZomm(myMap, position, 15);

			// Geocoder gcd = new Geocoder(getBaseContext(),
			// Locale.getDefault());
			// try {
			// List<Address> addresses = gcd.getFromLocation(latitude,
			// longitude, 1);
			//
			// if (addresses.size() > 0) {
			// for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++) {
			// fullAddress += addresses.get(0).getAddressLine(i) + ",";
			// }
			//
			// tvNewComplaintAdress.setText(fullAddress);
			// fullAddress = ""; //yeni bilgi geldiginde adresler arka arkaya
			// eklenmemeli.
			// }
			// } catch(Exception e) {
			// Log.e("exception", e.getMessage());
			// }
			tvNewComplaintAdress.setText(util.getAddress(getBaseContext(),
					position));

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	private class SaveTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				newComplaint.save();
			} catch (IOException e) {
				// TODO: handle exception
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Toast.makeText(getApplicationContext(),
					"Your complaint is saved succesfully", Toast.LENGTH_SHORT)
					.show();
			
			Intent anIntent = new Intent(NewComplaint.this,
                    DetailsActivity.class);
            anIntent.putExtra("class", newComplaint);
            startActivity(anIntent);
		}

	}

}
