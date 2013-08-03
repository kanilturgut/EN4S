package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class BiggerMap extends Activity {

	private GoogleMap myBigMap = null;
	private LatLng markerPosition = null;

	private Utils util = null;

	private Button sendButton = null;
	
	private String complaintTitle;
	private int complaintCategory;
	private byte[] complaintImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bigger_map);
		getActionBar().hide();

		Log.d("BiggerMap", "onCreate");	
		
		util = new Utils();
		
		complaintTitle = getIntent().getStringExtra("complaintTitle");
		complaintCategory = getIntent().getIntExtra("complaintCategory", 0);
		complaintImage = getIntent().getByteArrayExtra("complaintImage");

		markerPosition = new LatLng(
				getIntent().getDoubleExtra("LatLng_Lat", 0), getIntent()
						.getDoubleExtra("LatLng_Lng", 0));

		myBigMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.mapBiggerMap)).getMap();
		myBigMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		util.addAMarker(myBigMap, markerPosition, true);
		util.centerAndZomm(myBigMap, markerPosition, 18);
		
		myBigMap.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDragStart(Marker marker) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMarkerDrag(Marker marker) {

				markerPosition = new LatLng(marker.getPosition().latitude,
						marker.getPosition().longitude);

			}
		});
		
		myBigMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
				
				myBigMap.animateCamera(CameraUpdateFactory.newLatLng(point));
				
			}
		});

		sendButton = (Button) findViewById(R.id.bBiggerMapSend);
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				Intent betterPositionIntent = new Intent(BiggerMap.this, NewComplaint.class);
				betterPositionIntent.putExtra("user_lat", markerPosition.latitude);
				betterPositionIntent.putExtra("user_lng", markerPosition.longitude);
				betterPositionIntent.putExtra("complaintTitle", complaintTitle);
				betterPositionIntent.putExtra("complaintCategory", complaintCategory);
				betterPositionIntent.putExtra("complaintImage", complaintImage);
				startActivity(betterPositionIntent);
				
			}
		});

	}
}