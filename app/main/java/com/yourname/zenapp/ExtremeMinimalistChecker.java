package com.yourname.zenapp;

import android.content.Context;
import android.content.SharedPreferences;

public class ExtremeMinimalistChecker {

    public static boolean isExtremeMinimalistModeActive(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ZenAppPrefs", Context.MODE_PRIVATE);

        long startTime = preferences.getLong("extreme_minimalist_start", 0);
        int durationDays = preferences.getInt("extreme_minimalist_days", 0);

        if (startTime == 0 || durationDays == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long durationMillis = durationDays * 24 * 60 * 60 * 1000L;

        return (currentTime - startTime) < durationMillis;
    }

    public static void checkAndBlockActivity(Context context, String activityName) {
        if (isExtremeMinimalistModeActive(context)) {
            android.widget.Toast.makeText(context,
                    "🧘 " + activityName + " is locked during Extreme Minimalist Mode",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    public static long getRemainingTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("ZenAppPrefs", Context.MODE_PRIVATE);

        long startTime = preferences.getLong("extreme_minimalist_start", 0);
        int durationDays = preferences.getInt("extreme_minimalist_days", 0);

        if (startTime == 0 || durationDays == 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long durationMillis = durationDays * 24 * 60 * 60 * 1000L;
        long remainingTime = (startTime + durationMillis) - currentTime;

        return Math.max(0, remainingTime);
    }
}
