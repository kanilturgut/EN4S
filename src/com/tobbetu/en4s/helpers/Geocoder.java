package com.tobbetu.en4s.helpers;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.tobbetu.en4s.backend.Requests;

public class Geocoder {

    private static final String TAG = "Geocoder";

    public static String getCity(double latitude, double longitude)
            throws IOException, JSONException {
        String url = String
                .format("http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=false",
                        Double.toString(latitude), Double.toString(longitude));

        Log.d(TAG, "url: " + url);
        HttpResponse get = Requests.getWithoutDomain(url);
        String response = Requests.readResponse(get);

        JSONObject obj = new JSONObject(response);
        JSONArray results = obj.getJSONArray("results");
        JSONObject result = results.getJSONObject(0);
        JSONArray components = result.getJSONArray("address_components");

        for (int i = 0; i < components.length(); i++) {
            JSONObject component = components.getJSONObject(i);
            JSONArray types = component.getJSONArray("types");
            for (int j = 0; j < types.length(); j++) {
                if (types.getString(j).equals("administrative_area_level_1")) {
                    return component.getString("long_name").replace(
                            " Province", "");
                }
            }
        }

        return null;
    }
}
