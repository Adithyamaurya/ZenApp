package com.yourname.zenapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Add imports for GestureDetector and MotionEvent
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.core.view.GestureDetectorCompat;

// Make SettingsActivity implement GestureDetector.OnGestureListener
public class SettingsActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    // Add a GestureDetectorCompat and isAnimating flag
    private GestureDetectorCompat gestureDetector;
    private boolean isAnimating = false;

    private SharedPreferences preferences;
    private Switch showQuoteSwitch;
    private Switch showDateTimeSwitch;
    private Switch statusBarSwitch;
    private LinearLayout selectedAppsLayout;
    private LinearLayout blockedAppsLayout;
    private LinearLayout troubleshootBlockingLayout;
    private LinearLayout deviceAdministratorLayout;
    private LinearLayout aboutLayout;
    private LinearLayout faqsLayout;
    private LinearLayout extremeMinimalistLayout;

    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);

        // Initialize device policy manager
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        deviceAdminComponent = new ComponentName(this, ZenAppDeviceAdminReceiver.class);

        // Initialize gesture detector
        gestureDetector = new GestureDetectorCompat(this, this); // Add this line

        initViews();
        loadSettings();
        setupClickListeners();
    }

    // Add onTouchEvent to pass events to GestureDetector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    // Implement GestureDetector.OnGestureListener methods
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null || isAnimating) return false;

        try {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    isAnimating = true;

                    if (diffX < 0) { // Swipe left
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish(); // Finish SettingsActivity to go back to MainActivity
                    }

                    new android.os.Handler().postDelayed(() -> isAnimating = false, 500);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initViews() {
        showQuoteSwitch = findViewById(R.id.show_quote_switch);
        showDateTimeSwitch = findViewById(R.id.show_datetime_switch);
        statusBarSwitch = findViewById(R.id.status_bar_switch);
        selectedAppsLayout = findViewById(R.id.selected_apps);
        blockedAppsLayout = findViewById(R.id.blocked_apps);
        troubleshootBlockingLayout = findViewById(R.id.troubleshoot_blocking);
        deviceAdministratorLayout = findViewById(R.id.device_administrator);
        aboutLayout = findViewById(R.id.about_layout);
        faqsLayout = findViewById(R.id.faqs_layout);
        extremeMinimalistLayout = findViewById(R.id.extreme_minimalist);
    }

    private void loadSettings() {
        // Load saved preferences
        boolean showQuote = preferences.getBoolean("show_quote", true);
        boolean showDateTime = preferences.getBoolean("show_date_time", true);
        boolean showStatusBar = preferences.getBoolean("show_status_bar", true);

        if (showQuoteSwitch != null) {
            showQuoteSwitch.setChecked(showQuote);
        }
        if (showDateTimeSwitch != null) {
            showDateTimeSwitch.setChecked(showDateTime);
        }
        if (statusBarSwitch != null) {
            statusBarSwitch.setChecked(showStatusBar);
        }
    }

    private void setupClickListeners() {
        // Show Quote Switch
        if (showQuoteSwitch != null) {
            showQuoteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferences.edit().putBoolean("show_quote", isChecked).apply();
                }
            });
        }

        // Show Date Time Switch
        if (showDateTimeSwitch != null) {
            showDateTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferences.edit().putBoolean("show_date_time", isChecked).apply();
                }
            });
        }

        // Status Bar Switch
        if (statusBarSwitch != null) {
            statusBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferences.edit().putBoolean("show_status_bar", isChecked).apply();
                    // Apply status bar visibility immediately
                    applyStatusBarVisibility(isChecked);
                }
            });
        }

        // Change default launcher
        findViewById(R.id.change_default_launcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_HOME_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, "Cannot open home settings", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Selected Apps Layout Click
        if (selectedAppsLayout != null) {
            selectedAppsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFavoriteAppsActivity();
                }
            });
        }

        // Blocked Apps Layout Click
        if (blockedAppsLayout != null) {
            blockedAppsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBlockedAppsActivity();
                }
            });
        }

        // NEW: Troubleshoot Blocking Layout Click
        if (troubleshootBlockingLayout != null) {
            troubleshootBlockingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openTroubleshootActivity();
                }
            });
        }

        // NEW: Device Administrator Layout Click
        if (deviceAdministratorLayout != null) {
            deviceAdministratorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestDeviceAdministratorPermission();
                }
            });
        }

        // NEW: Extreme Minimalist Layout Click
        if (extremeMinimalistLayout != null) {
            extremeMinimalistLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExtremeMinimalistModeActive()) {
                        showExtremeMinimalistStatus();
                    } else {
                        showExtremeMinimalistDialog();
                    }
                }
            });
        }

        // About Layout Click
        if (aboutLayout != null) {
            aboutLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutActivity();
                }
            });
        }

        // FAQs Layout Click
        if (faqsLayout != null) {
            faqsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFaqsActivity();
                }
            });
        }
    }

    private void openFavoriteAppsActivity() {
        Intent intent = new Intent(this, FavoriteAppsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openBlockedAppsActivity() {
        Intent intent = new Intent(this, BlockedAppsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openTroubleshootActivity() {
        Intent intent = new Intent(this, PermissionRequestActivity.class);
        intent.putExtra("troubleshooting_mode", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void requestDeviceAdministratorPermission() {
        if (devicePolicyManager.isAdminActive(deviceAdminComponent)) {
            // Already enabled
            showDeviceAdminStatus(true);
        } else {
            // Show warning dialog first
            showDeviceAdminWarningDialog();
        }
    }

    private void showDeviceAdminWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ DEVICE ADMINISTRATOR WARNING")
                .setMessage("You are about to enable Device Administrator privileges for ZenApp.\n\n" +
                        "🛡️ MAXIMUM SECURITY FEATURES:\n" +
                        "• Prevents easy app uninstallation\n" +
                        "• Blocks unauthorized access to settings\n" +
                        "• Enhances app blocking protection\n" +
                        "• Makes bypass attempts extremely difficult\n\n" +
                        "⚠️ WARNING: This makes it very hard to remove ZenApp!\n\n" +
                        "Only enable if you're fully committed to digital wellness.\n\n" +
                        "Do you want to proceed?")
                .setPositiveButton("Enable Maximum Security", (dialog, which) -> {
                    enableDeviceAdministrator();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User cancelled
                })
                .setCancelable(false)
                .show();
    }

    private void enableDeviceAdministrator() {
        try {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "ZenApp needs Device Administrator privileges to provide maximum app blocking protection and prevent easy bypass attempts.");
            startActivityForResult(intent, 1000);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot request device administrator permission", Toast.LENGTH_LONG).show();
        }
    }

    private void showDeviceAdminStatus(boolean isEnabled) {
        String message;
        if (isEnabled) {
            message = "✅ Device Administrator is ENABLED\n\n" +
                    "ZenApp now has maximum security protection:\n" +
                    "• Enhanced app blocking\n" +
                    "• Uninstall protection\n" +
                    "• Bypass prevention\n\n" +
                    "Your digital wellness commitment is secured!";
        } else {
            message = "❌ Device Administrator is DISABLED\n\n" +
                    "Enable it for maximum security:\n" +
                    "• Prevent easy app uninstallation\n" +
                    "• Enhanced blocking protection\n" +
                    "• Stronger bypass prevention";
        }

        new AlertDialog.Builder(this)
                .setTitle("Device Administrator Status")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void applyStatusBarVisibility(boolean showStatusBar) {
        if (showStatusBar) {
            // Show status bar
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            // Hide status bar
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void openAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openFaqsActivity() {
        Intent intent = new Intent(this, FaqsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            // Device admin request result
            boolean isEnabled = devicePolicyManager.isAdminActive(deviceAdminComponent);
            if (isEnabled) {
                preferences.edit().putBoolean("device_admin_enabled", true).apply();
                Toast.makeText(this, "✅ Device Administrator enabled! Maximum security active.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "❌ Device Administrator not enabled. Security features limited.", Toast.LENGTH_LONG).show();
            }
            showDeviceAdminStatus(isEnabled);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAnimating = false; // Reset animation flag on resume
        // Apply current status bar setting
        boolean showStatusBar = preferences.getBoolean("show_status_bar", true);
        applyStatusBarVisibility(showStatusBar);
    }

    private boolean isExtremeMinimalistModeActive() {
        long startTime = preferences.getLong("extreme_minimalist_start", 0);
        int durationDays = preferences.getInt("extreme_minimalist_days", 0);

        if (startTime == 0 || durationDays == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long durationMillis = durationDays * 24 * 60 * 60 * 1000L;

        return (currentTime - startTime) < durationMillis;
    }

    private void showExtremeMinimalistDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🧘 EXTREME MINIMALIST MODE")
                .setMessage("Enter Digital Detox Mode\n\n" +
                        "⚠️ WARNING: This will LOCK you into minimal interface:\n\n" +
                        "🚫 NO ACCESS to Settings\n" +
                        "🚫 NO ACCESS to App Management\n" +
                        "🚫 NO ACCESS to Customization\n" +
                        "✅ ONLY Main Page & App List\n\n" +
                        "📅 You choose the duration (1-365 days)\n" +
                        "🔒 Cannot be disabled until period ends\n\n" +
                        "⚡ Configure ALL settings NOW before enabling!\n\n" +
                        "Ready for digital minimalism commitment?")
                .setPositiveButton("Set Duration", (dialog, which) -> {
                    showDurationSelectionDialog();
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .show();
    }

    private void showDurationSelectionDialog() {
        final String[] durations = {
                "1 Day - Quick Trial",
                "3 Days - Weekend Detox",
                "7 Days - One Week Challenge",
                "14 Days - Two Week Reset",
                "30 Days - Monthly Commitment",
                "60 Days - Deep Detox",
                "90 Days - Quarterly Reset",
                "180 Days - Half Year Challenge",
                "365 Days - Full Year Commitment",
                "Custom Duration..."
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Extreme Minimalist Duration")
                .setItems(durations, (dialog, which) -> {
                    int days;
                    switch (which) {
                        case 0: days = 1; break;
                        case 1: days = 3; break;
                        case 2: days = 7; break;
                        case 3: days = 14; break;
                        case 4: days = 30; break;
                        case 5: days = 60; break;
                        case 6: days = 90; break;
                        case 7: days = 180; break;
                        case 8: days = 365; break;
                        case 9:
                            showCustomDurationDialog();
                            return;
                        default: days = 7; break;
                    }
                    confirmExtremeMinimalistMode(days);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomDurationDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter days (1-365)");
        input.setTextColor(getResources().getColor(R.color.text_primary));
        input.setHintTextColor(getResources().getColor(R.color.text_hint));

        new AlertDialog.Builder(this)
                .setTitle("Custom Duration")
                .setMessage("Enter number of days for Extreme Minimalist Mode:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    try {
                        int days = Integer.parseInt(input.getText().toString().trim());
                        if (days >= 1 && days <= 365) {
                            confirmExtremeMinimalistMode(days);
                        } else {
                            Toast.makeText(this, "Please enter a number between 1 and 365", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmExtremeMinimalistMode(int days) {
        String durationText = days == 1 ? "1 day" : days + " days";

        new AlertDialog.Builder(this)
                .setTitle("⚠️ FINAL CONFIRMATION")
                .setMessage("You are about to enable Extreme Minimalist Mode for " + durationText + ".\n\n" +
                        "🔒 DURING THIS PERIOD:\n" +
                        "• Settings will be completely locked\n" +
                        "• No customization options available\n" +
                        "• Only main page and app list accessible\n" +
                        "• Cannot be disabled until " + durationText + " passes\n\n" +
                        "📱 FINAL CHECK:\n" +
                        "• Are your favorite apps selected?\n" +
                        "• Are your blocked apps configured?\n" +
                        "• Are all settings as you want them?\n\n" +
                        "This is your LAST CHANCE to make changes!\n\n" +
                        "Activate Extreme Minimalist Mode?")
                .setPositiveButton("ACTIVATE EXTREME MODE", (dialog, which) -> {
                    activateExtremeMinimalistMode(days);
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void activateExtremeMinimalistMode(int days) {
        long currentTime = System.currentTimeMillis();

        preferences.edit()
                .putLong("extreme_minimalist_start", currentTime)
                .putInt("extreme_minimalist_days", days)
                .putBoolean("extreme_minimalist_active", true)
                .apply();

        String durationText = days == 1 ? "1 day" : days + " days";

        new AlertDialog.Builder(this)
                .setTitle("🧘 EXTREME MINIMALIST MODE ACTIVATED")
                .setMessage("Digital Detox Mode is now ACTIVE for " + durationText + "!\n\n" +
                        "🔒 Settings and customization are now locked\n" +
                        "📱 Only main page and app list available\n" +
                        "🧘 Focus on digital minimalism\n\n" +
                        "Mode will automatically end in " + durationText + ".\n\n" +
                        "Welcome to your digital detox journey!")
                .setPositiveButton("Begin Minimalist Journey", (dialog, which) -> {
                    // Return to main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showExtremeMinimalistStatus() {
        long startTime = preferences.getLong("extreme_minimalist_start", 0);
        int durationDays = preferences.getInt("extreme_minimalist_days", 0);

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        long remainingTime = (durationDays * 24 * 60 * 60 * 1000L) - elapsedTime;

        int remainingDays = (int) (remainingTime / (24 * 60 * 60 * 1000L));
        int remainingHours = (int) ((remainingTime % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L));

        String remainingText;
        if (remainingDays > 0) {
            remainingText = remainingDays + " days, " + remainingHours + " hours";
        } else {
            remainingText = remainingHours + " hours";
        }

        new AlertDialog.Builder(this)
                .setTitle("🧘 Extreme Minimalist Mode Active")
                .setMessage("Digital Detox Mode is currently ACTIVE\n\n" +
                        "⏰ Time Remaining: " + remainingText + "\n\n" +
                        "🔒 Settings are locked during this period\n" +
                        "📱 Only main page and app list available\n" +
                        "🧘 Stay focused on digital minimalism\n\n" +
                        "Mode will automatically end when time expires.")
                .setPositiveButton("Continue Journey", null)
                .show();
    }
}
