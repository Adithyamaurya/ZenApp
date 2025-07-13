package com.yourname.zenapp;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockedAppsActivity extends AppCompatActivity {

    private ListView appsListView;
    private TextView instructionTextView;
    private TextView limitTextView;
    private List<AppInfo> installedApps;
    private BlockedAppsAdapter adapter;
    private SharedPreferences preferences;
    private Set<String> blockedApps;
    private Set<String> permanentlyBlockedApps;
    private static final int MAX_BLOCKED_APPS = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_apps);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        loadBlockedApps();

        initViews();
        loadInstalledApps();
        setupListAdapter();
        updateLimitText();

        showPermanentBlockingWarning();
    }

    private void showPermanentBlockingWarning() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ PERMANENT BLOCKING WARNING")
                .setMessage("CRITICAL NOTICE:\n\n" +
                        "🔒 Apps selected for blocking become PERMANENTLY BLOCKED\n" +
                        "🚫 Cannot be easily removed from blocking list\n" +
                        "📝 Only high-quality essays (75+ score) can provide temporary access\n" +
                        "🔄 Blocking resets after 1 hour of access\n" +
                        "⚡ System-level protection prevents bypass\n\n" +
                        "This is designed for serious digital wellness commitment.\n\n" +
                        "Are you absolutely sure you want to proceed?")
                .setPositiveButton("I Accept Permanent Blocking", (dialog, which) -> {
                    // User accepted permanent blocking
                    preferences.edit().putBoolean("permanent_blocking_accepted", true).apply();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void loadBlockedApps() {
        blockedApps = preferences.getStringSet("blocked_apps", new HashSet<>());
        if (blockedApps == null) {
            blockedApps = new HashSet<>();
        } else {
            blockedApps = new HashSet<>(blockedApps);
        }

        // Load permanently blocked apps (cannot be unblocked easily)
        permanentlyBlockedApps = preferences.getStringSet("permanently_blocked_apps", new HashSet<>());
        if (permanentlyBlockedApps == null) {
            permanentlyBlockedApps = new HashSet<>();
        } else {
            permanentlyBlockedApps = new HashSet<>(permanentlyBlockedApps);
        }
    }

    private void saveBlockedApps() {
        preferences.edit()
                .putStringSet("blocked_apps", blockedApps)
                .putStringSet("permanently_blocked_apps", permanentlyBlockedApps)
                .apply();
    }

    private void initViews() {
        appsListView = findViewById(R.id.appsListView);
        instructionTextView = findViewById(R.id.instructionText);
        limitTextView = findViewById(R.id.limitText);
    }

    private void updateLimitText() {
        int currentCount = blockedApps.size();
        int permanentCount = permanentlyBlockedApps.size();
        limitTextView.setText("Blocked: " + currentCount + "/" + MAX_BLOCKED_APPS +
                " | Permanent: " + permanentCount);

        if (currentCount >= MAX_BLOCKED_APPS) {
            limitTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            limitTextView.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void loadInstalledApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        installedApps = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                String appName = packageManager.getApplicationLabel(app).toString();
                Drawable appIcon = packageManager.getApplicationIcon(app);
                installedApps.add(new AppInfo(appName, app.packageName, appIcon));
            }
        }

        Collections.sort(installedApps, (a, b) -> a.name.compareToIgnoreCase(b.name));
    }

    private void setupListAdapter() {
        adapter = new BlockedAppsAdapter();
        appsListView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class BlockedAppsAdapter extends ArrayAdapter<AppInfo> {

        public BlockedAppsAdapter() {
            super(BlockedAppsActivity.this, R.layout.blocked_app_selection_item, installedApps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.blocked_app_selection_item, parent, false);
            }

            AppInfo app = installedApps.get(position);

            TextView appName = view.findViewById(R.id.appName);
            CheckBox checkBox = view.findViewById(R.id.blockedCheckBox);

            boolean isPermanentlyBlocked = permanentlyBlockedApps.contains(app.name);

            if (isPermanentlyBlocked) {
                appName.setText(app.name + " 🔒 PERMANENT");
                appName.setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                appName.setText(app.name);
                appName.setTextColor(getResources().getColor(R.color.text_primary));
            }

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(blockedApps.contains(app.name));
            checkBox.setEnabled(!isPermanentlyBlocked); // Disable checkbox for permanent apps

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isPermanentlyBlocked) {
                        checkBox.setChecked(true); // Force checked for permanent apps
                        Toast.makeText(BlockedAppsActivity.this,
                                app.name + " is permanently blocked and cannot be removed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (isChecked) {
                        if (blockedApps.size() >= MAX_BLOCKED_APPS) {
                            checkBox.setChecked(false);
                            Toast.makeText(BlockedAppsActivity.this,
                                    "Maximum " + MAX_BLOCKED_APPS + " apps can be blocked",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Show permanent blocking confirmation
                        showPermanentBlockingConfirmation(app.name, () -> {
                            blockedApps.add(app.name);
                            permanentlyBlockedApps.add(app.name);
                            saveBlockedApps();
                            updateLimitText();
                            adapter.notifyDataSetChanged();
                        }, () -> {
                            checkBox.setChecked(false);
                        });
                    } else {
                        // This should not happen for permanently blocked apps
                        blockedApps.remove(app.name);
                        saveBlockedApps();
                        updateLimitText();
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPermanentlyBlocked) {
                        Toast.makeText(BlockedAppsActivity.this,
                                app.name + " is permanently blocked and cannot be removed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!checkBox.isChecked() && blockedApps.size() >= MAX_BLOCKED_APPS) {
                        Toast.makeText(BlockedAppsActivity.this,
                                "Maximum " + MAX_BLOCKED_APPS + " apps can be blocked",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });

            return view;
        }
    }

    private void showPermanentBlockingConfirmation(String appName, Runnable onConfirm, Runnable onCancel) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ PERMANENT BLOCKING CONFIRMATION")
                .setMessage("You are about to PERMANENTLY BLOCK: " + appName + "\n\n" +
                        "🔒 This app will become completely inaccessible\n" +
                        "📝 Only high-quality essays (75+ score) provide 1-hour access\n" +
                        "🚫 Cannot be easily removed from blocking\n" +
                        "⚡ System-level protection prevents bypass\n\n" +
                        "This action cannot be easily undone!\n\n" +
                        "Are you absolutely certain?")
                .setPositiveButton("YES - PERMANENTLY BLOCK", (dialog, which) -> {
                    onConfirm.run();
                    Toast.makeText(this, appName + " is now PERMANENTLY BLOCKED", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    onCancel.run();
                })
                .setCancelable(false)
                .show();
    }

    private static class AppInfo {
        String name;
        String packageName;
        Drawable icon;

        AppInfo(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}
