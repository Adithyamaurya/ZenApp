package com.yourname.zenapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private LinearLayout githubLayout;
    private LinearLayout emailLayout;
    private LinearLayout rateLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        githubLayout = findViewById(R.id.github_layout);
        emailLayout = findViewById(R.id.email_layout);
        rateLayout = findViewById(R.id.rate_layout);

        // Set version name
        TextView versionText = findViewById(R.id.version_text);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("Version " + versionName);
        } catch (Exception e) {
            versionText.setText("Version");
        }
    }

    private void setupClickListeners() {
        githubLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/Adithyamaurya/ZenApp");
            }
        });

        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        rateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApp();
            }
        });
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void sendEmail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:adithyama012.@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "ZenApp Feedback");
            startActivity(intent);
        } catch (Exception e) {
            // Handle error
        }
    }

    private void rateApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to web browser
            openUrl("https://play.google.com/store/apps/details?id=" + getPackageName());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}