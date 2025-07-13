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
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteAppsActivity extends AppCompatActivity {

    private ListView appsListView;
    private TextView warningTextView;
    private List<AppInfo> installedApps;
    private FavoriteAppsAdapter adapter;
    private SharedPreferences preferences;
    private Set<String> favoriteApps;
    private static final int MAX_FAVORITE_APPS = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_apps);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        loadFavoriteApps();

        initViews();
        loadInstalledApps();
        setupListAdapter();
        updateWarningText();
    }

    private void loadFavoriteApps() {
        favoriteApps = preferences.getStringSet("favorite_apps", new HashSet<>());
        if (favoriteApps == null) {
            favoriteApps = new HashSet<>();
        } else {
            favoriteApps = new HashSet<>(favoriteApps); // Make it mutable
        }
    }

    private void saveFavoriteApps() {
        preferences.edit().putStringSet("favorite_apps", favoriteApps).apply();
    }

    private void initViews() {
        appsListView = findViewById(R.id.appsListView);
        warningTextView = findViewById(R.id.warningText);
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
    }

    private void setupListAdapter() {
        adapter = new FavoriteAppsAdapter();
        appsListView.setAdapter(adapter);
    }

    private void updateWarningText() {
        int selectedCount = favoriteApps.size();
        int remaining = MAX_FAVORITE_APPS - selectedCount;

        if (warningTextView != null) {
            if (selectedCount == 0) {
                warningTextView.setText("Select up to 8 apps to display on home screen");
                warningTextView.setTextColor(getResources().getColor(R.color.text_secondary));
            } else if (selectedCount < MAX_FAVORITE_APPS) {
                warningTextView.setText(selectedCount + " selected • " + remaining + " remaining");
                warningTextView.setTextColor(getResources().getColor(R.color.text_secondary));
            } else if (selectedCount == MAX_FAVORITE_APPS) {
                warningTextView.setText("Maximum 8 apps selected");
                warningTextView.setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                warningTextView.setText("Too many apps selected! Please uncheck some apps.");
                warningTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // Custom adapter for favorite apps selection
    private class FavoriteAppsAdapter extends ArrayAdapter<AppInfo> {

        public FavoriteAppsAdapter() {
            super(FavoriteAppsActivity.this, R.layout.favorite_app_selection_item, installedApps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.favorite_app_selection_item, parent, false);
            }

            AppInfo app = installedApps.get(position);

            TextView appName = view.findViewById(R.id.appName);
            CheckBox checkBox = view.findViewById(R.id.favoriteCheckBox);

            appName.setText(app.name);

            // Remove any existing listener to prevent unwanted triggers
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(favoriteApps.contains(app.name));

            // Set listener for checkbox
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Check if we can add more apps
                        if (favoriteApps.size() >= MAX_FAVORITE_APPS) {
                            // Prevent checking and show warning
                            checkBox.setChecked(false);
                            Toast.makeText(FavoriteAppsActivity.this,
                                    "Maximum 8 apps allowed! Please uncheck some apps first.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        favoriteApps.add(app.name);
                    } else {
                        favoriteApps.remove(app.name);
                    }
                    saveFavoriteApps();
                    updateWarningText();
                }
            });

            // Make the whole item clickable
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if we're trying to check when at limit
                    if (!checkBox.isChecked() && favoriteApps.size() >= MAX_FAVORITE_APPS) {
                        Toast.makeText(FavoriteAppsActivity.this,
                                "Maximum 8 apps allowed! Please uncheck some apps first.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });

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