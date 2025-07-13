package com.yourname.zenapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AppBlockingOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private String blockedAppName;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            blockedAppName = intent.getStringExtra("blocked_app");
            if (blockedAppName != null) {
                showBlockingOverlay();
            }
        }
        return START_STICKY; // Restart if killed
    }

    private void showBlockingOverlay() {
        if (overlayView != null) {
            removeOverlay();
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.app_blocking_overlay, null);

        TextView titleText = overlayView.findViewById(R.id.blockingTitle);
        TextView messageText = overlayView.findViewById(R.id.blockingMessage);
        Button unlockButton = overlayView.findViewById(R.id.unlockButton);
        Button homeButton = overlayView.findViewById(R.id.homeButton);

        titleText.setText("🔒 " + blockedAppName + " is Blocked");
        messageText.setText("This app requires a comprehensive essay to unlock.\n\n" +
                "Write 200+ words reflecting on your digital habits.\n" +
                "Minimum score: 75/100");

        unlockButton.setOnClickListener(v -> {
            Intent essayIntent = new Intent(this, EssayUnlockActivity.class);
            essayIntent.putExtra("app_name", blockedAppName);
            essayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(essayIntent);
            removeOverlay();
        });

        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            removeOverlay();
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
                overlayView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlay();
    }
}
