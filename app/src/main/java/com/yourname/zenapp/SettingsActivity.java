package com.yourname.zenapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private Switch showQuoteSwitch;
    private Switch showDateTimeSwitch;
    private Switch statusBarSwitch;
    private LinearLayout selectedAppsLayout;
    private LinearLayout aboutLayout;
    private LinearLayout faqsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);

        initViews();
        loadSettings();
        setupClickListeners();
    }

    private void initViews() {
        showQuoteSwitch = findViewById(R.id.show_quote_switch);
        showDateTimeSwitch = findViewById(R.id.show_datetime_switch);
        statusBarSwitch = findViewById(R.id.status_bar_switch);
        selectedAppsLayout = findViewById(R.id.selected_apps);
        aboutLayout = findViewById(R.id.about_layout);
        faqsLayout = findViewById(R.id.faqs_layout);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply current status bar setting
        boolean showStatusBar = preferences.getBoolean("show_status_bar", true);
        applyStatusBarVisibility(showStatusBar);
    }
}