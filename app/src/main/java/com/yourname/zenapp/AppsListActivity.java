package com.yourname.zenapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppsListActivity extends AppCompatActivity {

    private ListView appsListView;
    private TextView titleTextView;
    private List<AppInfo> installedApps;
    private List<AppInfo> visibleApps;
    private AppsAdapter adapter;
    private SharedPreferences preferences;
    private Set<String> hiddenApps;
    private Map<String, String> appRenames;
    private View contextMenuView;
    private AppInfo selectedApp;
    private FrameLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        loadHiddenApps();
        loadAppRenames();

        initViews();
        loadInstalledApps();
        setupListAdapter();
        setupClickListeners();
    }

    private void loadHiddenApps() {
        hiddenApps = preferences.getStringSet("hidden_apps", new HashSet<>());
        if (hiddenApps == null) {
            hiddenApps = new HashSet<>();
        } else {
            hiddenApps = new HashSet<>(hiddenApps); // Make it mutable
        }
    }

    private void loadAppRenames() {
        appRenames = new HashMap<>();
        Set<String> renameEntries = preferences.getStringSet("app_renames", new HashSet<>());
        for (String entry : renameEntries) {
            String[] parts = entry.split("\\|");
            if (parts.length == 2) {
                appRenames.put(parts[0], parts[1]);
            }
        }
    }

    private void saveHiddenApps() {
        preferences.edit().putStringSet("hidden_apps", hiddenApps).apply();
    }

    private void saveAppRenames() {
        Set<String> renameEntries = new HashSet<>();
        for (Map.Entry<String, String> entry : appRenames.entrySet()) {
            renameEntries.add(entry.getKey() + "|" + entry.getValue());
        }
        preferences.edit().putStringSet("app_renames", renameEntries).apply();
    }

    private void initViews() {
        appsListView = findViewById(R.id.appsListView);
        titleTextView = findViewById(R.id.app_title);
        mainContainer = findViewById(R.id.main_container);
    }

    private void loadInstalledApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        installedApps = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            // Only include apps that can be launched
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                String appName = packageManager.getApplicationLabel(app).toString();
                Drawable appIcon = packageManager.getApplicationIcon(app);
                installedApps.add(new AppInfo(appName, app.packageName, appIcon));
            }
        }

        // Sort apps alphabetically
        Collections.sort(installedApps, (a, b) -> a.name.compareToIgnoreCase(b.name));

        // Filter out hidden apps
        filterVisibleApps();
    }

    private void filterVisibleApps() {
        visibleApps = new ArrayList<>();
        for (AppInfo app : installedApps) {
            if (!hiddenApps.contains(app.packageName)) {
                visibleApps.add(app);
            }
        }
    }

    private void setupListAdapter() {
        adapter = new AppsAdapter();
        appsListView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Regular app click
        appsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (contextMenuView != null && contextMenuView.getVisibility() == View.VISIBLE) {
                    hideContextMenu();
                    return;
                }

                AppInfo selectedApp = visibleApps.get(position);
                launchApp(selectedApp.packageName);
            }
        });

        // Long press for context menu
        appsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedApp = visibleApps.get(position);
                showContextMenu(view, position);
                return true;
            }
        });

        // Title click for hidden apps management
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHiddenAppsActivity();
            }
        });

        // Click outside to hide context menu
        mainContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideContextMenu();
            }
        });
    }

    private void showContextMenu(View anchorView, int position) {
        hideContextMenu(); // Hide any existing menu

        contextMenuView = LayoutInflater.from(this).inflate(R.layout.context_menu_vertical, null);


        // Get the position of the ListView item relative to the main container
        int[] listViewLocation = new int[2];
        int[] anchorLocation = new int[2];
        int[] containerLocation = new int[2];

        appsListView.getLocationInWindow(listViewLocation);
        anchorView.getLocationInWindow(anchorLocation);
        mainContainer.getLocationInWindow(containerLocation);

        // Calculate relative position within the main container
        int relativeX = anchorLocation[0] - containerLocation[0];
        int relativeY = anchorLocation[1] - containerLocation[1];

        // Create layout parameters for the context menu
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        // Position the menu to the right of the selected item
        int menuWidth = 160; // Approximate width of context menu
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int containerPadding = 16; // Account for container padding

        // Check if there's enough space on the right
        if (relativeX + anchorView.getWidth() + menuWidth + containerPadding < screenWidth) {
            // Show on the right side
            params.leftMargin = relativeX + anchorView.getWidth() + 16;
        } else {
            // Show on the left side
            params.leftMargin = relativeX - menuWidth - 16;
            if (params.leftMargin < containerPadding) {
                params.leftMargin = containerPadding;
            }
        }

        // Vertical positioning - center the menu with the selected item
        params.topMargin = relativeY + (anchorView.getHeight() / 2) - 100; // Adjust to center

        // Ensure menu doesn't go off screen vertically
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int menuHeight = 300; // Approximate height of context menu

        if (params.topMargin + menuHeight > screenHeight - 100) {
            params.topMargin = screenHeight - menuHeight - 100;
        }
        if (params.topMargin < 100) {
            params.topMargin = 100;
        }

        contextMenuView.setLayoutParams(params);
        mainContainer.addView(contextMenuView);
        contextMenuView.setVisibility(View.VISIBLE);

        // Setup context menu buttons
        setupContextMenuButtons();
    }

    private void setupContextMenuButtons() {
        // Delete button
        View deleteButton = contextMenuView.findViewById(R.id.btn_delete);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmation();
                }
            });
        }

        // Rename button
        View renameButton = contextMenuView.findViewById(R.id.btn_rename);
        if (renameButton != null) {
            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRenameDialog();
                }
            });
        }

        // Hide button
        View hideButton = contextMenuView.findViewById(R.id.btn_hide);
        if (hideButton != null) {
            hideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideApp();
                }
            });
        }

        // Info button
        View infoButton = contextMenuView.findViewById(R.id.btn_info);
        if (infoButton != null) {
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAppInfo();
                }
            });
        }

        // Close button
        View closeButton = contextMenuView.findViewById(R.id.btn_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideContextMenu();
                }
            });
        }
    }

    private void hideContextMenu() {
        if (contextMenuView != null) {
            mainContainer.removeView(contextMenuView);
            contextMenuView = null;
        }
    }

    private void openHiddenAppsActivity() {
        Intent intent = new Intent(this, HiddenAppsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete App")
                .setMessage("Are you sure you want to uninstall " + getDisplayName(selectedApp) + "?")
                .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        uninstallApp();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        hideContextMenu();
    }

    private void showRenameDialog() {
        EditText editText = new EditText(this);
        editText.setText(getDisplayName(selectedApp));
        editText.selectAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename App")
                .setView(editText)
                .setPositiveButton("Rename", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String newName = editText.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            renameApp(newName);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        hideContextMenu();
    }

    private void hideApp() {
        hiddenApps.add(selectedApp.packageName);
        saveHiddenApps();
        filterVisibleApps();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, getDisplayName(selectedApp) + " hidden", Toast.LENGTH_SHORT).show();
        hideContextMenu();
    }

    private void showAppInfo() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + selectedApp.packageName));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open app info", Toast.LENGTH_SHORT).show();
        }
        hideContextMenu();
    }

    private void uninstallApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + selectedApp.packageName));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot uninstall this app", Toast.LENGTH_SHORT).show();
        }
    }

    private void renameApp(String newName) {
        appRenames.put(selectedApp.packageName, newName);
        saveAppRenames();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "App renamed to " + newName, Toast.LENGTH_SHORT).show();
    }

    private String getDisplayName(AppInfo app) {
        return appRenames.getOrDefault(app.packageName, app.name);
    }

    private void launchApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (contextMenuView != null && contextMenuView.getVisibility() == View.VISIBLE) {
            hideContextMenu();
            return;
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list in case apps were uninstalled or unhidden
        loadHiddenApps();
        loadAppRenames();
        loadInstalledApps();
        adapter.notifyDataSetChanged();
    }

    // Custom adapter for apps list
    private class AppsAdapter extends ArrayAdapter<AppInfo> {

        public AppsAdapter() {
            super(AppsListActivity.this, R.layout.app_list_item, visibleApps);
        }

        @Override
        public int getCount() {
            return visibleApps.size();
        }

        @Override
        public AppInfo getItem(int position) {
            return visibleApps.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.app_list_item, parent, false);
            }

            AppInfo app = visibleApps.get(position);

            TextView appName = view.findViewById(R.id.appName);
            appName.setText(getDisplayName(app).toLowerCase());

            return view;
        }
    }

    // Helper class to store app information
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