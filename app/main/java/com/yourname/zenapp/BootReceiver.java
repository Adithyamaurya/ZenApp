package com.yourname.zenapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Receiver to restart services after device boot
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            Log.d(TAG, "Device booted, restarting ZenApp services");

            SharedPreferences preferences = context.getSharedPreferences("ZenAppPrefs", Context.MODE_PRIVATE);

            // Only restart services if ZenApp was previously configured (e.g., permissions granted)
            // You should set this preference to true after initial setup is complete.
            if (preferences.getBoolean("permissions_configured", false)) {
                // Start the watchdog service
                Intent watchdogIntent = new Intent(context, ServiceWatchdog.class);
                try {
                    context.startService(watchdogIntent);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Cannot start ServiceWatchdog from BootReceiver: " + e.getMessage());
                }

                // Start the app blocking service
                Intent blockingIntent = new Intent(context, AppBlockingService.class);
                try {
                    context.startService(blockingIntent);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Cannot start AppBlockingService from BootReceiver: " + e.getMessage());
                }

                Log.d(TAG, "ZenApp services restarted after boot");
            } else {
                Log.d(TAG, "ZenApp not configured, services not started after boot.");
            }
        }
    }
}
