package com.yourname.zenapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private GestureDetectorCompat gestureDetector;
    private TextView timeTextView, dateTextView, quoteTextView;
    private LinearLayout appContainer;
    private Handler timeHandler;
    private Runnable timeRunnable;
    private SharedPreferences preferences;
    private boolean isAnimating = false;
    private Map<String, String> appPackageMap;

    // Daily quotes array
    private String[] dailyQuotes = {
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Life is what happens to you while you're busy making other plans. - John Lennon",
            "The future belongs to those who believe in the beauty of their dreams. - Eleanor Roosevelt",
            "It is during our darkest moments that we must focus to see the light. - Aristotle",
            "The way to get started is to quit talking and begin doing. - Walt Disney",
            "Don't let yesterday take up too much of today. - Will Rogers",
            "You learn more from failure than from success. Don't let it stop you. - Unknown",
            "If you are working on something that you really care about, you don't have to be pushed. - Steve Jobs",
            "Experience is a hard teacher because she gives the test first, the lesson afterward. - Vernon Law",
            "Knowing is not enough; we must apply. Wishing is not enough; we must do. - Johann Wolfgang von Goethe",
            "Whether you think you can or you think you can't, you're right. - Henry Ford",
            "The two most important days in your life are the day you are born and the day you find out why. - Mark Twain",
            "Your limitation—it's only your imagination.",
            "Push yourself, because no one else is going to do it for you.",
            "Great things never come from comfort zones.",
            "Dream it. Wish it. Do it.",
            "Success doesn't just find you. You have to go out and get it.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Dream bigger. Do bigger.",
            "Don't stop when you're tired. Stop when you're done.",
            "Wake up with determination. Go to bed with satisfaction.",
            "Do something today that your future self will thank you for.",
            "Little things make big days.",
            "It's going to be hard, but hard does not mean impossible.",
            "Don't wait for opportunity. Create it.",
            "Sometimes we're tested not to show our weaknesses, but to discover our strengths.",
            "The key to success is to focus on goals, not obstacles.",
            "Dream it. Believe it. Build it.",
            "What we plant in the soil of contemplation, we shall reap in the harvest of action.",
            "Be yourself; everyone else is already taken. - Oscar Wilde",
            "Simplicity is the ultimate sophistication. - Leonardo da Vinci"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize preferences
        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);

        // Initialize gesture detector
        gestureDetector = new GestureDetectorCompat(this, this);

        // Initialize views
        initViews();

        // Build app package mapping
        buildAppPackageMap();

        // Setup time and date updates
        setupTimeUpdates();

        // Setup daily quote
        setupDailyQuote();

        // Setup favorite apps
        setupFavoriteApps();

        // Add entrance animation
        addEntranceAnimation();
    }

    private void buildAppPackageMap() {
        appPackageMap = new HashMap<>();
        PackageManager packageManager = getPackageManager();

        // Get all installed applications
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                String appName = packageManager.getApplicationLabel(app).toString();
                appPackageMap.put(appName, app.packageName);
            }
        }
    }

    private void addEntranceAnimation() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_smooth);
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.startAnimation(fadeIn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
        appContainer = findViewById(R.id.appContainer);

        // Check if views are found
        if (timeTextView == null) {
            android.util.Log.e("MainActivity", "timeTextView not found");
        }
        if (dateTextView == null) {
            android.util.Log.e("MainActivity", "dateTextView not found");
        }
        if (quoteTextView == null) {
            android.util.Log.e("MainActivity", "quoteTextView not found");
        }
        if (appContainer == null) {
            android.util.Log.e("MainActivity", "appContainer not found");
        }
    }

    private void setupTimeUpdates() {
        timeHandler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeAndDate();
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void updateTimeAndDate() {
        try {
            // Check if date/time display is enabled
            boolean showDateTime = preferences.getBoolean("show_date_time", true);

            if (showDateTime) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());

                Date now = new Date();

                if (timeTextView != null) {
                    timeTextView.setText(timeFormat.format(now));
                    timeTextView.setVisibility(View.VISIBLE);
                }
                if (dateTextView != null) {
                    dateTextView.setText(dateFormat.format(now));
                    dateTextView.setVisibility(View.VISIBLE);
                }
            } else {
                if (timeTextView != null) {
                    timeTextView.setVisibility(View.GONE);
                }
                if (dateTextView != null) {
                    dateTextView.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDailyQuote() {
        try {
            boolean showQuote = preferences.getBoolean("show_quote", true);

            if (showQuote && quoteTextView != null) {
                // Get quote based on day of year for daily rotation
                Calendar calendar = Calendar.getInstance();
                int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                String todaysQuote = dailyQuotes[dayOfYear % dailyQuotes.length];

                quoteTextView.setText(todaysQuote);
                quoteTextView.setVisibility(View.VISIBLE);
            } else if (quoteTextView != null) {
                quoteTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFavoriteApps() {
        if (appContainer == null) return;

        try {
            appContainer.removeAllViews();

            // Get saved favorite apps
            Set<String> favoriteAppsSet = preferences.getStringSet("favorite_apps", new HashSet<>());

            // If no favorite apps are selected, show 8 "Apps" items
            if (favoriteAppsSet.isEmpty()) {
                for (int i = 0; i < 8; i++) {
                    TextView appTextView = createAppSelectionTextView();
                    if (appTextView != null) {
                        appContainer.addView(appTextView);

                        // Add staggered entrance animation
                        try {
                            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_smooth);
                            slideIn.setStartOffset(i * 100); // Stagger by 100ms
                            appTextView.startAnimation(slideIn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // Show selected favorite apps
                List<String> favoriteAppsList = new ArrayList<>(favoriteAppsSet);
                for (int i = 0; i < favoriteAppsList.size(); i++) {
                    TextView appTextView = createFavoriteAppTextView(favoriteAppsList.get(i));
                    if (appTextView != null) {
                        appContainer.addView(appTextView);

                        // Add staggered entrance animation
                        try {
                            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_smooth);
                            slideIn.setStartOffset(i * 100); // Stagger by 100ms
                            appTextView.startAnimation(slideIn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextView createAppSelectionTextView() {
        try {
            TextView appTextView = new TextView(this);
            appTextView.setText("Apps");
            appTextView.setTextSize(18);
            appTextView.setTextColor(getResources().getColor(R.color.text_primary));
            appTextView.setPadding(32, 24, 32, 24);
            appTextView.setGravity(android.view.Gravity.CENTER);

            // Set background with null check
            try {
                appTextView.setBackground(getResources().getDrawable(R.drawable.app_list_item_background));
            } catch (Exception e) {
                // Fallback to simple background
                appTextView.setBackgroundColor(getResources().getColor(R.color.background_card));
            }

            appTextView.setClickable(true);
            appTextView.setFocusable(true);

            // Set layout params with margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            appTextView.setLayoutParams(params);

            // Add click listener to open favorite apps selection
            appTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFavoriteAppsSelectionWithAnimation(v);
                }
            });

            return appTextView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TextView createFavoriteAppTextView(String appName) {
        try {
            TextView appTextView = new TextView(this);
            appTextView.setText(appName.toLowerCase());
            appTextView.setTextSize(18);
            appTextView.setTextColor(getResources().getColor(R.color.text_primary));
            appTextView.setPadding(32, 24, 32, 24);
            appTextView.setGravity(android.view.Gravity.CENTER);

            // Set background with null check
            try {
                appTextView.setBackground(getResources().getDrawable(R.drawable.app_list_item_background));
            } catch (Exception e) {
                // Fallback to simple background
                appTextView.setBackgroundColor(getResources().getColor(R.color.background_card));
            }

            appTextView.setClickable(true);
            appTextView.setFocusable(true);

            // Set layout params with margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            appTextView.setLayoutParams(params);

            // Add click listener to launch app with animation
            appTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchAppWithAnimation(appName, v);
                }
            });

            return appTextView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openFavoriteAppsSelectionWithAnimation(View view) {
        try {
            Animation launchAnim = AnimationUtils.loadAnimation(this, R.anim.app_launch);
            view.startAnimation(launchAnim);

            // Delay the actual launch to show animation
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openFavoriteAppsSelection();
                }
            }, 200);
        } catch (Exception e) {
            // Fallback to direct launch
            openFavoriteAppsSelection();
        }
    }

    private void openFavoriteAppsSelection() {
        Intent intent = new Intent(this, FavoriteAppsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void launchAppWithAnimation(String appName, View view) {
        try {
            Animation launchAnim = AnimationUtils.loadAnimation(this, R.anim.app_launch);
            view.startAnimation(launchAnim);

            // Delay the actual launch to show animation
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchApp(appName);
                }
            }, 200);
        } catch (Exception e) {
            // Fallback to direct launch
            launchApp(appName);
        }
    }

    private void launchApp(String appName) {
        try {
            // First try to find the app by exact name match
            String packageName = appPackageMap.get(appName);

            if (packageName != null) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    return;
                }
            }

            // If exact match fails, try partial matching
            for (Map.Entry<String, String> entry : appPackageMap.entrySet()) {
                String installedAppName = entry.getKey().toLowerCase();
                String searchAppName = appName.toLowerCase();

                if (installedAppName.contains(searchAppName) || searchAppName.contains(installedAppName)) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(entry.getValue());
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                        return;
                    }
                }
            }

            // If still no match, try common package names
            launchAppByCommonPackages(appName);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchAppByCommonPackages(String appName) {
        try {
            String appNameLower = appName.toLowerCase();
            String[] possiblePackages = getPossiblePackageNames(appNameLower);

            for (String packageName : possiblePackages) {
                try {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                        return;
                    }
                } catch (Exception e) {
                    // Continue to next package
                }
            }

            // Last resort: try intent actions
            launchAppByIntent(appNameLower);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getPossiblePackageNames(String appName) {
        // Common package name patterns for popular apps
        switch (appName) {
            case "phone":
            case "dialer":
                return new String[]{
                        "com.android.dialer",
                        "com.google.android.dialer",
                        "com.samsung.android.dialer",
                        "com.htc.android.dialer"
                };
            case "messages":
            case "messaging":
            case "sms":
                return new String[]{
                        "com.android.mms",
                        "com.google.android.apps.messaging",
                        "com.samsung.android.messaging",
                        "com.android.messaging"
                };
            case "camera":
                return new String[]{
                        "com.android.camera2",
                        "com.google.android.GoogleCamera",
                        "com.samsung.android.camera",
                        "com.android.camera"
                };
            case "settings":
                return new String[]{
                        "com.android.settings",
                        "com.samsung.android.settings"
                };
            case "gallery":
            case "photos":
                return new String[]{
                        "com.google.android.apps.photos",
                        "com.samsung.android.gallery3d",
                        "com.android.gallery3d"
                };
            case "chrome":
            case "browser":
                return new String[]{
                        "com.android.chrome",
                        "com.google.android.apps.chrome",
                        "com.android.browser"
                };
            case "gmail":
            case "email":
                return new String[]{
                        "com.google.android.gm",
                        "com.android.email",
                        "com.samsung.android.email.provider"
                };
            case "youtube":
                return new String[]{
                        "com.google.android.youtube"
                };
            case "maps":
                return new String[]{
                        "com.google.android.apps.maps"
                };
            case "play store":
            case "playstore":
                return new String[]{
                        "com.android.vending"
                };
            case "calculator":
                return new String[]{
                        "com.google.android.calculator",
                        "com.android.calculator2",
                        "com.samsung.android.calculator"
                };
            case "clock":
            case "alarm":
                return new String[]{
                        "com.google.android.deskclock",
                        "com.android.deskclock",
                        "com.samsung.android.app.clockpackage"
                };
            case "contacts":
                return new String[]{
                        "com.google.android.contacts",
                        "com.android.contacts",
                        "com.samsung.android.contacts"
                };
            case "calendar":
                return new String[]{
                        "com.google.android.calendar",
                        "com.android.calendar",
                        "com.samsung.android.calendar"
                };
            default:
                return new String[]{};
        }
    }

    private void launchAppByIntent(String appName) {
        try {
            Intent intent = new Intent();
            switch (appName) {
                case "phone":
                case "dialer":
                    intent.setAction(Intent.ACTION_DIAL);
                    break;
                case "messages":
                case "messaging":
                case "sms":
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setType("vnd.android-dir/mms-sms");
                    break;
                case "camera":
                    intent.setAction("android.media.action.IMAGE_CAPTURE");
                    break;
                case "settings":
                    intent.setAction(android.provider.Settings.ACTION_SETTINGS);
                    break;
                case "gallery":
                case "photos":
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setType("image/*");
                    break;
                case "browser":
                case "chrome":
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse("http://www.google.com"));
                    break;
                case "email":
                case "gmail":
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                    break;
                case "calculator":
                    intent.setAction("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.APP_CALCULATOR");
                    break;
                case "calendar":
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                    break;
                case "contacts":
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(android.provider.ContactsContract.Contacts.CONTENT_URI);
                    break;
                default:
                    Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
                    return;
            }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No app found to handle " + appName, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

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
    public void onLongPress(MotionEvent e) {
        // Long press functionality removed as requested
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null || isAnimating) return false;

        try {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            // Check if horizontal swipe is more significant than vertical
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    isAnimating = true;

                    if (diffX < 0) {
                        // Swipe right - open settings
                        Intent intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        // Swipe left - open apps list
                        Intent intent = new Intent(this, AppsListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }

                    // Reset animation flag after delay
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isAnimating = false;
                        }
                    }, 500);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAnimating = false;

        // Rebuild app package map in case new apps were installed
        buildAppPackageMap();

        // Apply status bar setting
        try {
            boolean showStatusBar = preferences.getBoolean("show_status_bar", true);
            applyStatusBarVisibility(showStatusBar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Refresh quote and favorite apps in case settings changed
        setupDailyQuote();
        setupFavoriteApps();
    }

    private void applyStatusBarVisibility(boolean showStatusBar) {
        try {
            if (showStatusBar) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
}