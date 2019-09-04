package ir.nobka.momenium.locationproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

public class LocationManager implements LocationListener, Runnable {

    private LocationCallBack locationCallBack;
    private Context mContext;

    private Handler handler;
    private int tries;
    private static final String TAG = "totto";


    public LocationManager(Context mContext) {
        this.mContext = mContext;
        handler = new Handler();
        tries = 0;
    }


    public void registerLocationListener(final LocationCallBack locationCallBack, boolean GPS) {
        tries = 0;

        this.locationCallBack = locationCallBack;

        android.location.LocationManager mLocationManager = (android.location.LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);


        if (mLocationManager != null) {
            long CHECK_LOCATION_UPDATE_SEC = 1000;
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission
                    .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }else {

                if (GPS) {
                    mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER,
                            CHECK_LOCATION_UPDATE_SEC, 0, this);

                }else {

                    mLocationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER,
                            CHECK_LOCATION_UPDATE_SEC, 0, this);

                }
            }
        } else
            locationCallBack.onError("not find provider");

        //set a timeout for location
        handler.postDelayed(this, 60 * 1000);
    }

    public void unRegisterLocaionListener() {

        if (mContext == null)
            return;

        android.location.LocationManager mLocationManager = (android.location.LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        handler.removeCallbacks(this);

    }

    @Override
    public void onLocationChanged(Location location) {
        // waits for more accurate location
        if (++tries > 4) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                if (!isMockSettingsON(mContext)) {
                    locationCallBack.onSuccess(location);
                }
            } else {
                if (!location.isFromMockProvider()) {
                    locationCallBack.onSuccess(location);
                }
            }
            unRegisterLocaionListener();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        unRegisterLocaionListener();
        locationCallBack.onLocationDisable();
    }

    @Override
    public void run() {
        unRegisterLocaionListener();
        if (locationCallBack != null) {
            locationCallBack.onError("Location not available.");
        }
    }

    public static boolean isMockSettingsON(Context context) {
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }
}
