package ir.nobka.momenium.locationproject;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class LocationListener implements android.location.LocationListener {
    Location mLastLocation;
    private static final String TAG = "mommo";

    public LocationListener(String provider) {
        mLastLocation = new Location(provider);
        Log.d(TAG, "LocationListener: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation.set(location);
        Log.d(TAG, "onLocationChanged:  lat:" + location.getLatitude() + "  long: " + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: ");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: ");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + status +" " + provider);
    }
}