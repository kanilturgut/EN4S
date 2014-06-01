package com.tobbetu.en4s;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Author   : kanilturgut
 * Date     : 01/06/14
 * Time     : 10:11
 */
public class MapActivity extends Activity {

    final String TAG = "MapActivity";
    Context context = null;

    double latitude, longitude;
    GoogleMap googleMap = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        this.context = this;

        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);


        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapActivity));
        if (mapFragment != null)
            googleMap = mapFragment.getMap();


        if (googleMap != null) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            LatLng latLng = new LatLng(latitude, longitude);
            Utils.addAMarker(googleMap, latLng, true);
            Utils.centerAndZomm(googleMap, latLng, 18);


            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));
                }
            });
        }

    }
}