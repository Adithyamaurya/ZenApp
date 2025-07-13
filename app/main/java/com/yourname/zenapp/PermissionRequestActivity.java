package com.yourname.zenapp;

import android.accessibilityservice.AccessibilityService;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionRequestActivity extends AppCompatActivity {

    private LinearLayout permissionContainer;
    private Button continueButton;
    private SharedPreferences preferences;

    private CheckBox overlayPermissionCheck;
    private CheckBox accessibilityPermissionCheck;
    private CheckBox batteryOptimizationCheck;
    private CheckBox usageStatsCheck;
    private CheckBox autoStartCheck;
    private CheckBox deviceAdminCheck;

    private Button overlayPermissionButton;
    private Button accessibilityPermissionButton;
    private Button batteryOptimizationButton;
    private Button usageStatsButton;
    private Button autoStartButton;
    private Button deviceAdminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);

        initViews();
        setupPermissionChecks();
        setupClickListeners();

        // Check if in troubleshooting mode
        boolean troubleshootingMode = getIntent().getBooleanExtra("troubleshooting_mode", false);

        if (troubleshootingMode) {
            showTroubleshootingDialog();
        } else {
            showWarningDialog();
        }
    }

    private void initViews() {
        permissionContainer = findViewById(R.id.permissionContainer);
        continueButton = findViewById(R.id.continueButton);

        overlayPermissionCheck = findViewById(R.id.overlayPermissionCheck);
        accessibilityPermissionCheck = findViewById(R.id.accessibilityPermissionCheck);
        batteryOptimizationCheck = findViewById(R.id.batteryOptimizationCheck);
        usageStatsCheck = findViewById(R.id.usageStatsCheck);
        autoStartCheck = findViewById(R.id.autoStartCheck);
        deviceAdminCheck = findViewById(R.id.deviceAdminCheck);

        overlayPermissionButton = findViewById(R.id.overlayPermissionButton);
        accessibilityPermissionButton = findViewById(R.id.accessibilityPermissionButton);
        batteryOptimizationButton = findViewById(R.id.batteryOptimizationButton);
        usageStatsButton = findViewById(R.id.usageStatsButton);
        autoStartButton = findViewById(R.id.autoStartButton);
        deviceAdminButton = findViewById(R.id.deviceAdminButton);
    }

    private void showWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ CRITICAL SECURITY NOTICE")
                .setMessage("You are about to enable MAXIMUM APP BLOCKING protection.\n\n" +
                        "⚠️ WARNING: Once apps are blocked with these permissions:\n" +
                        "• Apps become COMPLETELY inaccessible\n" +
                        "• Blocking cannot be easily removed\n" +
                        "• System-level protection prevents bypass\n" +
                        "• Only high-quality essays (75+ score) can unlock\n\n" +
                        "This is designed for serious digital wellness commitment.\n\n" +
                        "Do you understand and accept these restrictions?")
                .setPositiveButton("I Understand & Accept", (dialog, which) -> {
                    // User accepted, continue
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showTroubleshootingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🔧 Troubleshoot Blocking System")
                .setMessage("This page helps you diagnose and fix app blocking issues.\n\n" +
                        "Check each permission below and grant any missing ones.\n\n" +
                        "If apps are not being blocked properly:\n" +
                        "• Ensure all permissions are granted\n" +
                        "• Restart your device\n" +
                        "• Check manufacturer-specific settings\n" +
                        "• Verify accessibility service is running")
                .setPositiveButton("Check Permissions", (dialog, which) -> {
                    // Continue to permission checks
                })
                .setNegativeButton("Back", (dialog, which) -> {
                    finish();
                })
                .show();
    }

    private void setupPermissionChecks() {
        checkAllPermissions();
    }

    private void checkAllPermissions() {
        // Check overlay permission
        overlayPermissionCheck.setChecked(canDrawOverlays());

        // Check accessibility permission
        accessibilityPermissionCheck.setChecked(isAccessibilityServiceEnabled());

        // Check battery optimization
        batteryOptimizationCheck.setChecked(isBatteryOptimizationDisabled());

        // Check usage stats permission
        usageStatsCheck.setChecked(hasUsageStatsPermission());

        // Auto-start is manufacturer specific, assume user needs to enable manually
        autoStartCheck.setChecked(preferences.getBoolean("auto_start_enabled", false));

        // Device admin check
        deviceAdminCheck.setChecked(preferences.getBoolean("device_admin_enabled", false));

        updateContinueButton();
    }

    private void setupClickListeners() {
        overlayPermissionButton.setOnClickListener(v -> requestOverlayPermission());
        accessibilityPermissionButton.setOnClickListener(v -> requestAccessibilityPermission());
        batteryOptimizationButton.setOnClickListener(v -> requestBatteryOptimization());
        usageStatsButton.setOnClickListener(v -> requestUsageStatsPermission());
        autoStartButton.setOnClickListener(v -> requestAutoStartPermission());
        deviceAdminButton.setOnClickListener(v -> requestDeviceAdminPermission());

        continueButton.setOnClickListener(v -> {
            if (allPermissionsGranted()) {
                // Mark permissions as configured
                preferences.edit().putBoolean("permissions_configured", true).apply();

                // Start the blocking service
                startBlockingServices();

                // Go to blocked apps selection
                Intent intent = new Intent(this, BlockedAppsActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "All permissions are required for maximum protection", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1001);
            }
        }
    }

    private void requestAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Enable 'ZenApp Blocking Service' in Accessibility settings", Toast.LENGTH_LONG).show();
    }

    private void requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Enable 'ZenApp' in Usage Access settings", Toast.LENGTH_LONG).show();
    }

    private void requestAutoStartPermission() {
        // Show instructions for different manufacturers
        showAutoStartInstructions();
    }

    private void requestDeviceAdminPermission() {
        // For maximum security, we can request device admin rights
        showDeviceAdminInstructions();
    }

    private void showAutoStartInstructions() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String instructions = getAutoStartInstructions(manufacturer);

        new AlertDialog.Builder(this)
                .setTitle("Enable Auto-Start")
                .setMessage(instructions)
                .setPositiveButton("I've Enabled It", (dialog, which) -> {
                    preferences.edit().putBoolean("auto_start_enabled", true).apply();
                    autoStartCheck.setChecked(true);
                    updateContinueButton();
                })
                .setNegativeButton("Show Me How", (dialog, which) -> {
                    // Try to open auto-start settings
                    openAutoStartSettings();
                })
                .show();
    }

    private String getAutoStartInstructions(String manufacturer) {
        switch (manufacturer) {
            case "xiaomi":
                return "1. Go to Settings > Apps > Manage Apps\n2. Find ZenApp\n3. Enable 'Autostart'\n4. Enable 'Background Activity'";
            case "huawei":
            case "honor":
                return "1. Go to Settings > Apps\n2. Find ZenApp\n3. Enable 'AutoLaunch'\n4. Go to Battery > App Launch\n5. Set ZenApp to 'Manage Manually'";
            case "oppo":
            case "realme":
                return "1. Go to Settings > Apps\n2. Find ZenApp\n3. Enable 'Auto Launch'\n4. Go to Battery > Power Saving\n5. Add ZenApp to whitelist";
            case "vivo":
                return "1. Go to Settings > More Settings > Applications\n2. Find ZenApp\n3. Enable 'Auto-start'\n4. Enable 'Background App Refresh'";
            case "samsung":
                return "1. Go to Settings > Apps\n2. Find ZenApp\n3. Battery > Optimize Battery Usage\n4. Turn OFF optimization for ZenApp";
            default:
                return "1. Go to Settings > Apps\n2. Find ZenApp\n3. Look for 'Auto-start' or 'Background Activity'\n4. Enable all permissions for ZenApp";
        }
    }

    private void openAutoStartSettings() {
        try {
            String manufacturer = Build.MANUFACTURER.toLowerCase();
            Intent intent = new Intent();

            switch (manufacturer) {
                case "xiaomi":
                    intent.setComponent(new ComponentName("com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    break;
                case "huawei":
                case "honor":
                    intent.setComponent(new ComponentName("com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
                    break;
                case "oppo":
                case "realme":
                    intent.setComponent(new ComponentName("com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.FakeActivity"));
                    break;
                case "vivo":
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                    break;
                default:
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    break;
            }

            startActivity(intent);
        } catch (Exception e) {
            // Fallback to app settings
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void showDeviceAdminInstructions() {
        new AlertDialog.Builder(this)
                .setTitle("Maximum Security Mode")
                .setMessage("For ultimate app blocking protection, ZenApp can request Device Administrator privileges.\n\n" +
                        "⚠️ WARNING: This makes it extremely difficult to uninstall ZenApp or disable blocking.\n\n" +
                        "Only enable if you're committed to digital wellness.")
                .setPositiveButton("Enable Maximum Security", (dialog, which) -> {
                    preferences.edit().putBoolean("device_admin_enabled", true).apply();
                    deviceAdminCheck.setChecked(true);
                    updateContinueButton();
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    // Skip device admin
                })
                .show();
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private boolean isAccessibilityServiceEnabled() {
        String enabledServices = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServices != null) {
            return enabledServices.contains(getPackageName() + "/" + AppBlockingAccessibilityService.class.getName());
        }
        return false;
    }

    private boolean isBatteryOptimizationDisabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        return true;
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean allPermissionsGranted() {
        return canDrawOverlays() &&
                isAccessibilityServiceEnabled() &&
                isBatteryOptimizationDisabled() &&
                hasUsageStatsPermission() &&
                preferences.getBoolean("auto_start_enabled", false);
    }

    private void updateContinueButton() {
        continueButton.setEnabled(allPermissionsGranted());
        if (allPermissionsGranted()) {
            continueButton.setText("Continue to App Blocking Setup");
            continueButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        } else {
            continueButton.setText("Grant All Permissions First");
            continueButton.setBackgroundColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void startBlockingServices() {
        // Start the overlay service
        Intent overlayIntent = new Intent(this, AppBlockingOverlayService.class);
        startService(overlayIntent);

        // Start the monitoring service
        Intent monitorIntent = new Intent(this, AppBlockingService.class);
        startService(monitorIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAllPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            checkAllPermissions();
        }
    }
}
