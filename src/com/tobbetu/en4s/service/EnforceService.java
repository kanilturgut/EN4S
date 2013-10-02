package com.tobbetu.en4s.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.tasks.SaveComplaintTask;

public class EnforceService extends Service {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private LocationManager locManager = null;
    private LocationListener locListener = null;

    private static Location myBestLoc = null;

    private static boolean isGPSEnabled = false;

    @Override
    public IBinder onBind(Intent intent) {
        // Belki birgun ne oldugu anlasilir ve icerisi doldurulur :)
        return null;
    }

    @Override
    public void onCreate() {

        Log.d("EnforceService", "onCreate of service");

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locListener = new EnforceLocationListener();

        myBestLoc = locManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

    }

    @Override
    public void onStart(Intent intent, int startId) {

        Log.d("EnforceService", "onStart of service");

        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                1000, 1, locListener);

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                1, locListener);

        isGPSEnabled = locManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onDestroy() {

        Log.d("EnforceService", "onDestroy of service");

        if (locManager != null) {
            locManager.removeUpdates(locListener);

            locManager = null;
            locListener = null;

        }
    }

    class EnforceLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            Log.d("EnforceService", location.toString());

            if (isBetterLocation(location, myBestLoc)) {
                myBestLoc = location;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                isGPSEnabled = false;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000, 1, locListener);

                isGPSEnabled = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    }

    protected boolean isBetterLocation(Location location,
            Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static Location getLocation() {
        return myBestLoc;
    }

    public static boolean getGPSStatus() {
        return isGPSEnabled;
    }

    public static void startSaveComplaintTask(Context c,
            Complaint newComplaint2, Image img) {
        SaveComplaintTask newSaveComplaint = new SaveComplaintTask(c,
                newComplaint2, img);
        newSaveComplaint.execute();
    }
}
