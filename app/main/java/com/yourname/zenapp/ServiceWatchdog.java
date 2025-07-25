package com.yourname.zenapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.Nullable;
import android.app.ActivityManager; // Import ActivityManager

/**
 * Watchdog service to monitor and restart critical services
 */
public class ServiceWatchdog extends Service {
    private static final String TAG = "ServiceWatchdog";
    private static final long CHECK_INTERVAL = 30000; // 30 seconds

    private Handler handler = new Handler();
    private Runnable watchdogRunnable;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        startWatchdog();
        Log.d(TAG, "ServiceWatchdog started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Restart if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startWatchdog() {
        watchdogRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndRestartServices();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(watchdogRunnable);
    }

    private void checkAndRestartServices() {
        // Check if accessibility service is enabled
        if (!isAccessibilityServiceEnabled()) {
            Log.w(TAG, "Accessibility service is disabled");
            // Notify user to re-enable (e.g., via a persistent notification or a flag for MainActivity)
            notifyUserToEnableAccessibility();
        }

        // Check if app blocking service is running
        if (!isServiceRunning(AppBlockingService.class)) {
            Log.w(TAG, "AppBlockingService is not running, restarting...");
            Intent serviceIntent = new Intent(this, AppBlockingService.class);
            try {
                startService(serviceIntent);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Cannot start AppBlockingService in background: " + e.getMessage());
                // This might happen on Android O+ if app is in background and no notification is shown
                // Consider using startForegroundService if it's critical and needs to run immediately
            }
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        ComponentName cn = new ComponentName(this, AppBlockingAccessibilityService.class);
        String flat = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return flat != null && flat.contains(cn.flattenToString());
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void notifyUserToEnableAccessibility() {
        // Store flag to show notification/dialog in main activity when it's active
        preferences.edit()
                .putBoolean("accessibility_disabled_warning", true)
                .apply();
        // You might also want to show a persistent notification here if the app is in foreground
        // or if you have permission to show notifications.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && watchdogRunnable != null) {
            handler.removeCallbacks(watchdogRunnable);
        }

        // Attempt to restart the watchdog itself to ensure continuous monitoring
        // This is a common pattern for persistent services, but Android might still kill it
        // if system resources are low or battery optimizations are aggressive.
        Intent restartIntent = new Intent(this, ServiceWatchdog.class);
        try {
            startService(restartIntent);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot restart ServiceWatchdog in background: " + e.getMessage());
        }
    }
}
