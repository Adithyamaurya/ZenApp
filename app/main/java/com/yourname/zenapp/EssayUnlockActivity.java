package com.yourname.zenapp;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EssayUnlockActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView essayPromptTextView;
    private TextView wordCountTextView;
    private TextView progressTextView;
    private TextView evaluationTextView;
    private EditText essayEditText;
    private Button submitButton;
    private Button cancelButton;
    private ProgressBar evaluationProgressBar;

    private String appName;
    private String requiredEssay;
    private SharedPreferences preferences;
    private int requiredWordCount = 200;
    private int currentWordCount = 0;
    private long lastKeyTime = 0;
    private int keystrokeCount = 0;
    private boolean isTypingNaturally = true;
    private EssayEvaluator essayEvaluator;

    // Essay prompts for different apps
    private Map<String, String> essayPrompts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_essay_unlock);

        preferences = getSharedPreferences("ZenAppPrefs", MODE_PRIVATE);
        appName = getIntent().getStringExtra("app_name");
        essayEvaluator = new EssayEvaluator();

        initViews();
        setupEssayPrompts();
        setupEssayPrompt();
        setupTextWatcher();
        setupClickListeners();

        disableClipboard();
        preventScreenCapture();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.titleTextView);
        essayPromptTextView = findViewById(R.id.essayPromptTextView);
        wordCountTextView = findViewById(R.id.wordCountTextView);
        progressTextView = findViewById(R.id.progressTextView);
        evaluationTextView = findViewById(R.id.evaluationTextView);
        essayEditText = findViewById(R.id.essayEditText);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);
        evaluationProgressBar = findViewById(R.id.evaluationProgressBar);
    }

    private void setupEssayPrompts() {
        essayPrompts = new HashMap<>();

        essayPrompts.put("social", "Reflect deeply on how social media platforms influence your daily productivity, mental well-being, and real-world relationships. Analyze the psychological mechanisms these apps use to capture attention and discuss three specific strategies you can implement to create healthier boundaries with social media. Consider the long-term impact of constant connectivity on your ability to focus, be present, and engage in meaningful offline activities.");

        essayPrompts.put("entertainment", "Examine your relationship with entertainment applications and their impact on your personal growth and time management. Discuss the difference between passive consumption and active engagement with media. Analyze how entertainment apps affect your motivation to pursue challenging, skill-building activities. Propose three alternative activities that could provide similar satisfaction while contributing to your long-term goals and personal development.");

        essayPrompts.put("gaming", "Analyze the psychological appeal of gaming and its effects on your brain's reward system, time perception, and goal-setting abilities. Compare the achievements and skills developed in games versus real-world accomplishments. Discuss how gaming might be affecting your patience for slower, more gradual forms of progress in life. Outline three ways you could channel the same energy and focus you bring to gaming toward developing real-world skills or relationships.");

        essayPrompts.put("shopping", "Explore the psychology behind impulse buying and how shopping applications exploit cognitive biases to encourage consumption. Reflect on the difference between needs and wants, and analyze how marketing techniques influence your purchasing decisions. Discuss the environmental and financial implications of frequent online shopping. Develop three questions you will ask yourself before any purchase to ensure it aligns with your values and long-term financial goals.");

        essayPrompts.put("news", "Examine the impact of constant news consumption on your mental health, worldview, and ability to focus on actionable problems in your immediate environment. Analyze how the 24/7 news cycle affects your anxiety levels and sense of agency. Discuss the difference between being informed and being overwhelmed by information. Propose a structured approach to staying informed while protecting your mental well-being and maintaining focus on areas where you can make a meaningful impact.");

        essayPrompts.put("default", "Conduct a thorough self-assessment of your relationship with this specific application and technology in general. Analyze how this app fits into your daily routine and whether it supports or detracts from your core values and long-term objectives. Examine the opportunity cost of time spent on this app - what meaningful activities, relationships, or personal development could you pursue instead? Develop a personal technology use philosophy that prioritizes intentional engagement over mindless consumption.");
    }

    private void setupEssayPrompt() {
        if (appName != null) {
            titleTextView.setText("Unlock " + appName);

            String essayType = getEssayType(appName.toLowerCase());
            requiredEssay = essayPrompts.get(essayType);

            essayPromptTextView.setText(requiredEssay);
            updateWordCount();
        }
    }

    private String getEssayType(String appName) {
        if (appName.contains("facebook") || appName.contains("instagram") ||
                appName.contains("twitter") || appName.contains("snapchat") ||
                appName.contains("tiktok") || appName.contains("whatsapp") ||
                appName.contains("telegram") || appName.contains("discord")) {
            return "social";
        } else if (appName.contains("youtube") || appName.contains("netflix") ||
                appName.contains("spotify") || appName.contains("music") ||
                appName.contains("video") || appName.contains("stream")) {
            return "entertainment";
        } else if (appName.contains("game") || appName.contains("play") ||
                appName.contains("clash") || appName.contains("candy")) {
            return "gaming";
        } else if (appName.contains("shop") || appName.contains("amazon") ||
                appName.contains("buy") || appName.contains("store") ||
                appName.contains("cart") || appName.contains("purchase")) {
            return "shopping";
        } else if (appName.contains("news") || appName.contains("reddit") ||
                appName.contains("cnn") || appName.contains("bbc")) {
            return "news";
        } else {
            return "default";
        }
    }

    private void setupTextWatcher() {
        essayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long currentTime = System.currentTimeMillis();

                // Detect potential paste operations
                if (count > 20 && count > before * 5) {
                    isTypingNaturally = false;
                    Toast.makeText(EssayUnlockActivity.this,
                            "Large text insertion detected! Please type manually.",
                            Toast.LENGTH_LONG).show();

                    // Remove the pasted text
                    essayEditText.setText(s.subSequence(0, start));
                    essayEditText.setSelection(start);
                    return;
                }

                // Track typing speed
                if (lastKeyTime > 0) {
                    long timeDiff = currentTime - lastKeyTime;
                    if (timeDiff < 50 && count > 5) { // Very fast typing
                        isTypingNaturally = false;
                        Toast.makeText(EssayUnlockActivity.this,
                                "Typing too fast! Please slow down and type naturally.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                lastKeyTime = currentTime;
                keystrokeCount++;

                updateWordCount();
                evaluateEssayRealTime(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateWordCount() {
        String text = essayEditText.getText().toString().trim();
        String[] words = text.isEmpty() ? new String[0] : text.split("\\s+");
        currentWordCount = words.length;

        wordCountTextView.setText("Words: " + currentWordCount + "/" + requiredWordCount);

        int progress = Math.min(100, (currentWordCount * 100) / requiredWordCount);
        progressTextView.setText("Progress: " + progress + "%");

        if (currentWordCount >= requiredWordCount) {
            progressTextView.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            progressTextView.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void evaluateEssayRealTime(String text) {
        if (text.length() > 100) {
            EssayEvaluation evaluation = essayEvaluator.evaluateEssay(text, requiredEssay);

            StringBuilder feedback = new StringBuilder();
            feedback.append("Quality Score: ").append(evaluation.overallScore).append("/100\n");

            if (evaluation.originalityScore < 70) {
                feedback.append("⚠️ Content appears generic. Be more specific and personal.\n");
            }
            if (evaluation.relevanceScore < 70) {
                feedback.append("⚠️ Stay focused on the essay prompt.\n");
            }
            if (evaluation.depthScore < 70) {
                feedback.append("⚠️ Provide deeper analysis and examples.\n");
            }

            evaluationTextView.setText(feedback.toString());
            evaluationTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWordCount >= requiredWordCount && isTypingNaturally) {
                    evaluateAndUnlock();
                } else if (!isTypingNaturally) {
                    Toast.makeText(EssayUnlockActivity.this,
                            "Please write your essay manually without copying text.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EssayUnlockActivity.this,
                            "Please complete the essay with at least " + requiredWordCount + " words.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void evaluateAndUnlock() {
        evaluationProgressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Simulate evaluation time
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String essayText = essayEditText.getText().toString();
                EssayEvaluation evaluation = essayEvaluator.evaluateEssay(essayText, requiredEssay);

                evaluationProgressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);

                if (evaluation.overallScore >= 75) {
                    unlockApp(evaluation);
                } else {
                    showEvaluationFeedback(evaluation);
                }
            }
        }, 3000);
    }

    private void showEvaluationFeedback(EssayEvaluation evaluation) {
        StringBuilder feedback = new StringBuilder();
        feedback.append("Essay Evaluation Results:\n\n");
        feedback.append("Overall Score: ").append(evaluation.overallScore).append("/100\n");
        feedback.append("Word Count: ").append(evaluation.wordCount).append("/").append(requiredWordCount).append("\n");
        feedback.append("Originality: ").append(evaluation.originalityScore).append("/100\n");
        feedback.append("Relevance: ").append(evaluation.relevanceScore).append("/100\n");
        feedback.append("Depth: ").append(evaluation.depthScore).append("/100\n\n");

        if (evaluation.overallScore < 75) {
            feedback.append("❌ Essay needs improvement. Minimum score required: 75/100\n\n");
            feedback.append("Suggestions:\n");
            if (evaluation.originalityScore < 70) {
                feedback.append("• Add more personal insights and specific examples\n");
            }
            if (evaluation.relevanceScore < 70) {
                feedback.append("• Address the essay prompt more directly\n");
            }
            if (evaluation.depthScore < 70) {
                feedback.append("• Provide deeper analysis and reflection\n");
            }
        }

        evaluationTextView.setText(feedback.toString());
        evaluationTextView.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Essay score: " + evaluation.overallScore + "/100. Minimum required: 75",
                Toast.LENGTH_LONG).show();
    }

    private void unlockApp(EssayEvaluation evaluation) {
        // Store unlock timestamp
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("app_unlock_" + appName, System.currentTimeMillis());
        editor.putInt("last_essay_score_" + appName, evaluation.overallScore);
        editor.apply();

        Toast.makeText(this, appName + " unlocked! Essay score: " + evaluation.overallScore + "/100",
                Toast.LENGTH_LONG).show();

        launchApp();
        finish();
    }

    private void launchApp() {
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getAppPackageName(appName));
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                launchAppByIntent(appName.toLowerCase());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getAppPackageName(String appName) {
        switch (appName.toLowerCase()) {
            case "phone": return "com.android.dialer";
            case "messages": return "com.android.mms";
            case "camera": return "com.android.camera2";
            case "settings": return "com.android.settings";
            default: return "";
        }
    }

    private void launchAppByIntent(String appName) {
        try {
            Intent intent = new Intent();
            switch (appName) {
                case "phone":
                    intent.setAction(Intent.ACTION_DIAL);
                    break;
                case "messages":
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
                default:
                    Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
                    return;
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open " + appName, Toast.LENGTH_SHORT).show();
        }
    }

    private void disableClipboard() {
        essayEditText.setLongClickable(false);
        essayEditText.setTextIsSelectable(false);

        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""));
            }
        } catch (Exception e) {
            // Ignore clipboard access errors
        }
    }

    private void preventScreenCapture() {
        try {
            getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
            );
        } catch (Exception e) {
            // Ignore if not supported
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button during essay writing
        Toast.makeText(this, "Complete the essay or tap Cancel to exit", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTypingNaturally = true;
        disableClipboard();
    }

    // Essay Evaluation Classes
    private static class EssayEvaluation {
        int overallScore;
        int wordCount;
        int originalityScore;
        int relevanceScore;
        int depthScore;
    }

    private static class EssayEvaluator {
        private Set<String> commonWords;
        private Set<String> genericPhrases;

        public EssayEvaluator() {
            initializeCommonWords();
            initializeGenericPhrases();
        }

        private void initializeCommonWords() {
            commonWords = new HashSet<>(Arrays.asList(
                    "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
                    "from", "up", "about", "into", "through", "during", "before", "after", "above",
                    "below", "between", "among", "under", "over", "is", "are", "was", "were", "be",
                    "been", "being", "have", "has", "had", "do", "does", "did", "will", "would",
                    "could", "should", "may", "might", "must", "can", "shall", "this", "that",
                    "these", "those", "i", "you", "he", "she", "it", "we", "they", "me", "him",
                    "her", "us", "them", "my", "your", "his", "her", "its", "our", "their"
            ));
        }

        private void initializeGenericPhrases() {
            genericPhrases = new HashSet<>(Arrays.asList(
                    "in my opinion", "i think that", "it is important", "in conclusion",
                    "to sum up", "in summary", "first of all", "on the other hand",
                    "in addition", "furthermore", "moreover", "however", "therefore",
                    "as a result", "for example", "for instance", "such as", "in other words"
            ));
        }

        public EssayEvaluation evaluateEssay(String essay, String prompt) {
            EssayEvaluation evaluation = new EssayEvaluation();

            String[] words = essay.toLowerCase().trim().split("\\s+");
            evaluation.wordCount = words.length;

            evaluation.originalityScore = evaluateOriginality(essay);
            evaluation.relevanceScore = evaluateRelevance(essay, prompt);
            evaluation.depthScore = evaluateDepth(essay);

            // Calculate overall score
            evaluation.overallScore = (int) (
                    (evaluation.originalityScore * 0.4) +
                            (evaluation.relevanceScore * 0.3) +
                            (evaluation.depthScore * 0.3)
            );

            return evaluation;
        }

        private int evaluateOriginality(String essay) {
            String[] words = essay.toLowerCase().split("\\s+");
            int uniqueWords = 0;
            int totalWords = words.length;

            Set<String> uniqueSet = new HashSet<>();
            for (String word : words) {
                if (!commonWords.contains(word)) {
                    uniqueSet.add(word);
                }
            }
            uniqueWords = uniqueSet.size();

            // Check for generic phrases
            String lowerEssay = essay.toLowerCase();
            int genericPhraseCount = 0;
            for (String phrase : genericPhrases) {
                if (lowerEssay.contains(phrase)) {
                    genericPhraseCount++;
                }
            }

            // Calculate originality score
            double uniqueRatio = (double) uniqueWords / totalWords;
            int baseScore = (int) (uniqueRatio * 100);
            int penalty = genericPhraseCount * 5;

            return Math.max(0, Math.min(100, baseScore - penalty));
        }

        private int evaluateRelevance(String essay, String prompt) {
            String[] promptWords = extractKeywords(prompt);
            String lowerEssay = essay.toLowerCase();

            int relevantWords = 0;
            for (String keyword : promptWords) {
                if (lowerEssay.contains(keyword.toLowerCase())) {
                    relevantWords++;
                }
            }

            double relevanceRatio = (double) relevantWords / promptWords.length;
            return (int) (relevanceRatio * 100);
        }

        private int evaluateDepth(String essay) {
            // Check for depth indicators
            String lowerEssay = essay.toLowerCase();
            int depthScore = 0;

            // Look for analysis words
            String[] analysisWords = {
                    "analyze", "examine", "consider", "reflect", "evaluate", "assess",
                    "compare", "contrast", "explore", "investigate", "because", "since",
                    "therefore", "consequently", "as a result", "leads to", "causes",
                    "effects", "impact", "influence", "relationship", "connection"
            };

            for (String word : analysisWords) {
                if (lowerEssay.contains(word)) {
                    depthScore += 5;
                }
            }

            // Check for personal examples and specific details
            if (lowerEssay.contains("for example") || lowerEssay.contains("for instance")) {
                depthScore += 10;
            }

            // Check for questions (shows reflection)
            if (essay.contains("?")) {
                depthScore += 5;
            }

            // Check sentence variety (complex sentences indicate deeper thinking)
            String[] sentences = essay.split("[.!?]+");
            int complexSentences = 0;
            for (String sentence : sentences) {
                if (sentence.contains(",") && sentence.split("\\s+").length > 15) {
                    complexSentences++;
                }
            }
            depthScore += complexSentences * 3;

            return Math.min(100, depthScore);
        }

        private String[] extractKeywords(String prompt) {
            // Extract meaningful words from the prompt
            String[] words = prompt.toLowerCase().split("\\s+");
            Set<String> keywords = new HashSet<>();

            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() > 4 && !commonWords.contains(word)) {
                    keywords.add(word);
                }
            }

            return keywords.toArray(new String[0]);
        }
    }
}
