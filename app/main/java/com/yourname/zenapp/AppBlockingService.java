package com.yourname.zenapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

public class AppBlockingService extends Service {

    private Handler handler;
    private Runnable blockingRunnable;
    private SharedPreferences preferences;
    private Set<String> blockedApps;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        handler = new Handler();
        startBlocking();
    }

    private void startBlocking() {
        blockingRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndBlockApps();
                handler.postDelayed(this, 1000); // Check every second
            }
        };
        handler.post(blockingRunnable);
    }

    private void checkAndBlockApps() {
        loadBlockedApps();

        // Get current foreground app
        String currentApp = getCurrentForegroundApp();

        if (currentApp != null && isAppBlocked(currentApp)) {
            if (!isAppTemporarilyUnlocked(currentApp)) {
                // Block the app by bringing ZenApp to foreground
                Intent intent = new Intent(this, EssayUnlockActivity.class);
                intent.putExtra("app_name", currentApp);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                Toast.makeText(this, "App blocked! Complete essay to unlock.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadBlockedApps() {
        blockedApps = preferences.getStringSet("blocked_apps", new HashSet<>());
        if (blockedApps == null) {
            blockedApps = new HashSet<>();
        }
    }

    private String getCurrentForegroundApp() {
        // This would require usage access permission
        // Implementation depends on Android version
        return null; // Simplified for this example
    }

    private boolean isAppBlocked(String appName) {
        return blockedApps.contains(appName);
    }

    private boolean isAppTemporarilyUnlocked(String appName) {
        long unlockTime = preferences.getLong("app_unlock_" + appName, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - unlockTime) < (60 * 60 * 1000); // 1 hour unlock
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && blockingRunnable != null) {
            handler.removeCallbacks(blockingRunnable);
        }
    }
}
