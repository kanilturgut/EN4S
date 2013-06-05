package com.tobbetu.en4s;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.tumsiniflar.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NewComplaint extends Activity implements OnClickListener{

	private Button bPush;
	private ImageButton bTakePic;
	private EditText etComplaintTitle;

	private GoogleMap myMap;
	private LocationManager lManager = null;
	private double latitude = 0;
	private double longitude = 0;
	private String fullAddress = "";

	private Image img = null;
	private Bitmap bmp = null;

	private Complaint newComplaint = null;
	
	private String TAG = "NewComplaint";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_complaint);

	//	etComplaintTitle = (EditText) findViewById(R.id.etComplaint);

		bTakePic = (ImageButton) findViewById(R.id.bTakePhoto);
		bPush = (Button) findViewById(R.id.bPush);

		bTakePic.setOnClickListener(this);
		bPush.setOnClickListener(this);

		lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();
		lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				mlocListener);
		
		myMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapNewComplaint)).getMap();		
		myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

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
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, 0);
		} else {

			/*Once kullanicinin adresini alalim*/
			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

			try {
				List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);

				if (addresses.size() > 0)  {

					Log.e("deneme", "3");
					for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++) {

						fullAddress += addresses.get(0).getAddressLine(i) + ",";
					}
				}
			} catch(Exception e) {
				Log.e("exception", e.getMessage());
			}

			//simdi butun bilgileri toplayip, server a push edelim.

			newComplaint = new Complaint(etComplaintTitle.getText().toString(),
					"today", 
					fullAddress);
			
			String reporter = LoginPageActivity.loginPreferences.getString("username", "unknown");
			Log.e(TAG, "reporter : " + reporter);
			
			newComplaint.setCategory("Kategori");
			newComplaint.setLatitude(latitude);
			newComplaint.setLongitude(longitude);

		}

	}

	@Override //fotograf
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			bmp = (Bitmap) extras.get("data");

			img = new Image();

			byte[] buffer = img.bitmapToByteArray(bmp);

			img.setImageByteArray(buffer);
			img.setBmp(bmp);
		}

	}

	public class MyLocationListener implements LocationListener { //location

		@Override
		public void onLocationChanged(Location loc) {

			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			
			LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());
			
			myMap.addMarker(new MarkerOptions().position(position));
			myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
			
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

}
