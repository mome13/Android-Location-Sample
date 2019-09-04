package ir.nobka.momenium.locationproject;

import android.location.Location;

public interface LocationCallBack {

    void onSuccess(Location location);

    void onError(String error);

    void onLocationDisable();
}
