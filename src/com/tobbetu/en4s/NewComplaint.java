package com.tobbetu.en4s;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;

@SuppressLint("NewApi")
public class NewComplaint extends Activity implements OnClickListener {

	private Button bPush;
	private ImageButton bTakePic;
	private ImageView ivTakenPhoto;
	private EditText etComplaintTitle;
	private TextView tvNewComplaintAdress;
	private Spinner categoriesSpinner;
	private LinearLayout photoButtonLL;
	private ProgressDialog progressDialog = null;
	
	private Utils util = null;

	private GoogleMap myMap;
	// private Marker place = null;
	private LocationManager lManager = null;
	private LatLng position = null;
	private LatLng temp = null;
	private double latitude = 0;
	private double longitude = 0;

	private Image img = null;
	private Bitmap bmp = null;

	private Complaint newComplaint = null;
	private String category;

	private String TAG = "NewComplaint";
	
	private Uri mImageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_complaint);
		getActionBar().hide();

		util = new Utils();

		photoButtonLL = (LinearLayout) findViewById(R.id.photoButtonLL);
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
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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

	private File createTemporaryFile(String part, String ext) throws Exception {
		File tempDir= Environment.getExternalStorageDirectory();
		tempDir=new File(tempDir.getAbsolutePath() + "/.temp/");
		if(!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}
	
	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.bTakePhoto) {
			Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
			File photo = null;
			try {
				photo = this.createTemporaryFile("picture", ".jpg");
				photo.delete();
			}
			catch(Exception e) {
				Log.v(TAG, "Can't create file to take picture!");
				//		        Toast.makeText(activity, "Please check SD card! Image shot is impossible!", 10000);
				//		        return false;
			}
			mImageUri = Uri.fromFile(photo);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
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

	public Bitmap grabImage(){
	    this.getContentResolver().notifyChange(mImageUri, null);
	    ContentResolver cr = this.getContentResolver();
	    Bitmap bitmap;
	    try{
	        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
	        Log.e("Failed to load", bitmap.getHeight() + "");
	        return bitmap;
	    }
	    catch (Exception e)
	    {
	        //Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
	        Log.d(TAG, "Failed to load", e);
	    }
	    return null;
	}
	
	@Override
	// fotograf
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
//			Bundle extras = data.getExtras();
			bmp = (Bitmap) grabImage(); // Resimin orjinal hali
		
			img = new Image();
//			img.setBmp(bmp);

			int height = bmp.getHeight();
			int width = bmp.getWidth();

			Log.e(TAG, "height : " + height + ", width : " + width);

			bTakePic.setVisibility(View.GONE);
			ivTakenPhoto.setVisibility(View.VISIBLE);
			//ivTakenPhoto.getLayoutParams().height = height;
			//ivTakenPhoto.getLayoutParams().width = width;
	
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int tmpWidth = size.x;
			int tmpHeight = photoButtonLL.getLayoutParams().height;
			
			if(tmpWidth > 600){
				tmpWidth = bmp.getWidth();
				tmpHeight = (int) ((double)(bmp.getWidth() / tmpWidth) * tmpHeight);
			}
			
			Bitmap resized = Bitmap.createScaledBitmap(bmp, 600, 800, true); // 600 x 800 olarak resize edilmiþ resim
			Bitmap cropped = Bitmap.createBitmap(resized, 0, 150, tmpWidth, tmpHeight); // Resized resim üzerinden crop edilmiþ resim
			ivTakenPhoto.setImageBitmap(cropped);
			
			img.setBmp(resized);
			
			/*Kullanici fotograf cektiken sonra, send butonuna basmayi unutup aradan biraz zaman gectikten sonra
			 * farkederse konum bilgisi degismis oluyor. Bize yollayinca da yanlis konum almis oluyoruz. Bu sebeple
			 * fotograf cekildigi siradaki konumu kaydetmeli ve send butonuna basilinca o konumu yollamaliyiz*/
			position = temp;

		}

	}

	public class MyLocationListener implements LocationListener { // location

		@Override
		public void onLocationChanged(Location loc) {

			latitude = loc.getLatitude();
			longitude = loc.getLongitude();

			temp = new LatLng(loc.getLatitude(), loc.getLongitude());

			util.addAMarker(myMap, temp);
			util.centerAndZomm(myMap, temp, 15);
			
			tvNewComplaintAdress.setText(util.getAddress(getBaseContext(),
					temp));

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
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(NewComplaint.this, "Loading", "Your complaint is sending. Thank you for your patience");
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				newComplaint.save();
				String url = img.upload(newComplaint.getId());
				newComplaint.addJustUploadedImage(url);
			} catch (IOException e) {
				// TODO: handle exception
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			progressDialog.dismiss();
			
			Toast.makeText(getApplicationContext(),
					"Your complaint is saved succesfully", Toast.LENGTH_SHORT)
					.show();

			Intent anIntent = new Intent(NewComplaint.this,
					DetailsActivity.class);
			anIntent.putExtra("latitude", latitude);
			anIntent.putExtra("longitude", longitude);
			anIntent.putExtra("class", newComplaint);
			startActivity(anIntent);
		}

	}

}
