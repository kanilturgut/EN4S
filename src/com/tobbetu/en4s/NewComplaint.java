package com.tobbetu.en4s;

import java.util.List;
import java.util.Locale;

import com.example.tumsiniflar.R;

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
import android.widget.TextView;

public class NewComplaint extends Activity implements OnClickListener{

	private Button bTakePic, bPush;
	private EditText etComplaintTitle;

	private TextView tvLat, tvLong;

	private LocationManager lManager = null;
	private double latitude = 0;
	private double longitude = 0;

	private Image img = null;
	private Bitmap bmp = null;

	private Complaint newComplaint = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_complaint);
		
		tvLat = (TextView) findViewById(R.id.tvLatitude);
		tvLong = (TextView) findViewById(R.id.tvLongitude);

		etComplaintTitle = (EditText) findViewById(R.id.etComplaint);

		bTakePic = (Button) findViewById(R.id.bTakePhoto);
		bPush = (Button) findViewById(R.id.bPush);

		bTakePic.setOnClickListener(this);
		bPush.setOnClickListener(this);

		lManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		LocationListener mlocListener = new MyLocationListener();
		lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				mlocListener);

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
			String str = "";

			Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
			//
			try {
				Log.e("deneme", "1");
				List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);

				Log.e("deneme", "2");

				if (addresses.size() > 0)  {

					Log.e("deneme", "3");
					for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++) {

						str += addresses.get(0).getAddressLine(i) + ",";

					}

					Log.e("adres", str);

				}
			} catch(Exception e) {
				Log.e("exception", e.getMessage());
			}

			//simdi butun bilgileri toplayip, server a push edelim.

			//			newComplaint = new Complaint(title, date, address)

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

			Log.e("loc", loc.getLatitude() + "," + loc.getLongitude());
			
			if(loc != null) {

				tvLat.setText("your latitude is " + latitude);
				tvLong.setText("your longitude is " + longitude);
			}

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
