package com.yourname.zenapp;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FaqsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);

        setupFaqItems();
    }

    private void setupFaqItems() {
        LinearLayout faqContainer = findViewById(R.id.faq_container);

        // FAQ items data
        String[][] faqs = {
                {"How do I set ZenApp as my default launcher?",
                        "Go to Settings > Apps > Default Apps > Home App, then select ZenApp."},

                {"How do I hide apps?",
                        "Long press on any app in the app list and select 'Hide' from the context menu."},

                {"How do I access hidden apps?",
                        "Tap on the 'ZenApp' title in the app list to view and manage hidden apps."},

                {"How do I add apps to favorites?",
                        "Enable 'Show Favorite Apps' in Settings > Appearance, then long press apps to add them to favorites."},

                {"How do I rename apps?",
                        "Long press on any app and select 'Rename' from the context menu."},

                {"Can I change the theme?",
                        "Currently you can't change the theme so wait for the future update to switch between light and dark themes."},

                {"How do I show/hide the status bar?",
                        "Go to Settings > Appearance and toggle 'Status Bar on Top' to show or hide the system status bar."},

                {"How do I reset all settings?",
                        "Currently, you need to clear the app data from Android Settings > Apps > ZenApp > Storage > Clear Data."},

                {"Why can't I uninstall some apps?",
                        "System apps cannot be uninstalled, only disabled. You can hide them instead."},

                {"How do I report bugs or request features?",
                        "Go to Settings > About > Contact Developer to send feedback via email."},

                {"How do I uninstall ZenApp?",
                        "Go to Settings > Apps > ZenApp > Uninstall."},

                {"How do I turn off Administrator Mode?",
                        "Go to Settings > Security > Administrator Mode > Disable"},

                {"Why blocking is not working?",
                        "Go to ZenApp Settings > Troubleshoot Blocking > Grant Permissions > Allow"}
        };

        // Create FAQ items dynamically
        for (String[] faq : faqs) {
            View faqItem = createFaqItem(faq[0], faq[1]);
            faqContainer.addView(faqItem);
        }
    }

    private View createFaqItem(String question, String answer) {
        LinearLayout faqLayout = new LinearLayout(this);
        faqLayout.setOrientation(LinearLayout.VERTICAL);
        faqLayout.setBackground(getDrawable(R.drawable.settings_item_background));
        faqLayout.setPadding(48, 48, 48, 48);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        faqLayout.setLayoutParams(params);

        // Question
        TextView questionView = new TextView(this);
        questionView.setText(question);
        questionView.setTextColor(getColor(R.color.text_primary));
        questionView.setTextSize(16);
        questionView.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams questionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        questionParams.setMargins(0, 0, 0, 24);
        questionView.setLayoutParams(questionParams);

        // Answer
        TextView answerView = new TextView(this);
        answerView.setText(answer);
        answerView.setTextColor(getColor(R.color.text_secondary));
        answerView.setTextSize(14);
        answerView.setLineSpacing(12, 1.0f);

        faqLayout.addView(questionView);
        faqLayout.addView(answerView);

        return faqLayout;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}