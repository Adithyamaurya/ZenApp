package com.yourname.zenapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

public class AppBlockingAccessibilityService extends AccessibilityService {

    private SharedPreferences preferences;
    private Set<String> blockedApps;
    private String lastBlockedApp = "";
    private long lastBlockTime = 0;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        loadBlockedApps();

        // Configure the service
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);

        Toast.makeText(this, "🔒 Maximum App Blocking Protection Activated", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String packageName = event.getPackageName().toString();
                String appName = getAppNameFromPackage(packageName);

                if (appName != null && isAppBlocked(appName)) {
                    if (!isAppTemporarilyUnlocked(appName)) {
                        blockAppAccess(appName, packageName);
                    }
                }
            }
        }
    }

    private void loadBlockedApps() {
        blockedApps = preferences.getStringSet("blocked_apps", new HashSet<>());
        if (blockedApps == null) {
            blockedApps = new HashSet<>();
        }
    }

    private String getAppNameFromPackage(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAppBlocked(String appName) {
        loadBlockedApps(); // Reload to get latest blocked apps
        return blockedApps.contains(appName);
    }

    private boolean isAppTemporarilyUnlocked(String appName) {
        long unlockTime = preferences.getLong("app_unlock_" + appName, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - unlockTime) < (60 * 60 * 1000); // 1 hour unlock
    }

    private void blockAppAccess(String appName, String packageName) {
        // Prevent rapid blocking of same app
        long currentTime = System.currentTimeMillis();
        if (appName.equals(lastBlockedApp) && (currentTime - lastBlockTime) < 2000) {
            return;
        }

        lastBlockedApp = appName;
        lastBlockTime = currentTime;

        // Force close the blocked app by bringing ZenApp to foreground
        Intent intent = new Intent(this, EssayUnlockActivity.class);
        intent.putExtra("app_name", appName);
        intent.putExtra("package_name", packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        // Also start overlay to prevent access
        Intent overlayIntent = new Intent(this, AppBlockingOverlayService.class);
        overlayIntent.putExtra("blocked_app", appName);
        startService(overlayIntent);

        // Force kill the blocked app process (requires root or system permissions)
        try {
            Runtime.getRuntime().exec("am force-stop " + packageName);
        } catch (Exception e) {
            // Ignore if no permission
        }
    }

    @Override
    public void onInterrupt() {
        // Service interrupted
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Restart the service if it's destroyed
        Intent restartIntent = new Intent(this, AppBlockingAccessibilityService.class);
        startService(restartIntent);
    }
}
