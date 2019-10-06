package ir.nobka.momenium.locationproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


public class LocationActivity extends AppCompatActivity implements LocationCallBack, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    private boolean shouldUseGPS = true;
    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private ProgressDialog progressDialog;

    private static final String TAG = "LocationActivityTest";

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        Button btnGetLocation = findViewById(R.id.button);


        btnGetLocation.setOnClickListener(v -> getLocationForUser());
        shouldUseGPS = true;
        locationManager = new LocationManager(this);
    }


    private void getLocationForUser() {
        if (checkPermissionForUser()) {
            showProgressDialog();
            locationManager.registerLocationListener(this, shouldUseGPS);
        }
    }


    private boolean checkPermissionForUser() {
        if (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            return true;
        else {
            requestPermissionsSafely(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 6636);
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onSuccess(Location location) {
        hideProgressDialog();
        textView.setText("latitude : " + location.getLatitude() + "  longitude : " + location.getLongitude());
    }


    @Override
    public void onError(String error) {
        hideProgressDialog();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationDisable() {
        OpenSetting();
    }

    public void OpenSetting() {

//        openLocSettingsForUser();

        //*******************                if you dont want to use google play api you can get rid of bellow code and uncomment upper code               ********************************


        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1 * 1000);
            locationRequest.setFastestInterval(1 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocationForUser();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        openLocSettingsForUser();
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        final LocationSettingsStates state = locationSettingsResult.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:

                // All location settings are satisfied. The client can initialize location
                // requests here.
                onConnected(null);

                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                try {
                    Log.d(TAG, "onResult: show resolution");
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            this, 1000);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d(TAG, "onResult: SETTINGS_CHANGE_UNAVAILABLE");
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                break;
        }

    }


    public void openLocSettingsForUser() {
        hideProgressDialog();
        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),1500);
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
//        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        startActivity(viewIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 6636) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocationForUser();
            }
        }
    }


    public void initProgressDialog(String title) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(title);
        progressDialog.show();
    }


    public void showProgressDialog() {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()){
                progressDialog.setMessage("wait please");
                progressDialog.show();
            }
        } else {
            initProgressDialog("wait please");
        }

    }


    public void hideProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000){
            Log.d(TAG, "onActivityResult:1000 " + resultCode);
            if (resultCode != -1){
                googleApiClient = null;
                openLocSettingsForUser();
            }
        }else if (requestCode == 1500){
            Log.d(TAG, "onActivityResult:1500 " + resultCode);
            if (checkPermissionForUser()) {
                showProgressDialog();
                locationManager.registerLocationListener(new LocationCallBack() {
                    @Override
                    public void onSuccess(Location location) {
                        hideProgressDialog();
                        textView.setText("latitude : " + location.getLatitude() + "  longitude : " + location.getLongitude());
                    }

                    @Override
                    public void onError(String error) {
                        hideProgressDialog();
                        locationManager.unRegisterLocaionListener();
                    }

                    @Override
                    public void onLocationDisable() {
                        hideProgressDialog();
                    }
                }, shouldUseGPS);
            }
        }
    }
}
