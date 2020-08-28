package com.gahlot.neverendingservice;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ServiceAdmin {

    private static final String TAG = "ServiceAdmin";

    private static Intent serviceIntent = null;

    public ServiceAdmin() {
    }

    private void setServiceIntent(Context context) {
        if (serviceIntent == null) {
            serviceIntent = new Intent(context, AutoStartService.class);
        }
    }

    public void launchService(Context context) {
        if (context == null) {
            return;
        }
        setServiceIntent(context);

        // depending on the version of Android we either launch the simple service (version<O)
        // or we start a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        Log.d(TAG, "launchService:  Service is starting....");
    }
}
