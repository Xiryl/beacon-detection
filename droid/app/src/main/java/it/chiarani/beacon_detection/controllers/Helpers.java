package it.chiarani.beacon_detection.controllers;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

/**
 * Helper class used on fragments and activities for get the Service status without creating it wtice
 */
public final class Helpers {

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
