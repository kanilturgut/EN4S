package com.tobbetu.en4s;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Utils {

	private Marker place = null;

	/**
	 * Parametre olarak aldigi harita uzerindeki, LatLng ile gosterilen konuma
	 * marker ekler.
	 * 
	 */
	public void addAMarker(GoogleMap map, LatLng position) {

		if (place != null)
			place.remove();

		place = map.addMarker(new MarkerOptions().position(position));

		// map.addMarker(new MarkerOptions()
		// .position(myPos)
		// .title("Yeriniz")
		// .snippet("Þu an buradasýnýz.")
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.ic_launcher)));
	}

	/**
	 * 
	 * Harita uzerinde belirtilen konumu ekranda ortalayip, zoom yapar.
	 */
	public void centerAndZomm(GoogleMap map, LatLng position, int zoom) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
	}

	/**
	 * Parametre olarak aldigi konumun acik adresini doner.
	 */
	public String getAddress(Context context, LatLng position) {

		String address = "";

		Geocoder gcd = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = gcd.getFromLocation(position.latitude,
					position.longitude, 1);

			if (addresses.size() > 0) {
				for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
					address += addresses.get(0).getAddressLine(i) + ",";
				}
			}
		} catch (Exception e) {
			Log.e("exception", e.getMessage());
		}
		return address;

	}

}
