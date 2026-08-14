package com.example.healingjourney;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EmotionActivity extends BaseActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String detectedEmotion = "Calm";
    String emotionEmoji = "😌";
    String emotionDescription = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);
        setupBottomNav();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnShare = findViewById(R.id.btnShare);
        Button btnSaveProgress = findViewById(R.id.btnSaveProgress);
        TextView tvDrawAgain = findViewById(R.id.tvDrawAgain);
        TextView tvEmotionResult = findViewById(R.id.tvEmotionResult);
        TextView tvMeaning = findViewById(R.id.tvMeaning);
        TextView tvEmojiFace = findViewById(R.id.tvEmojiFace);

        // ✅ Get colour distribution + canvas coverage from ArtActivity
        int[] topColors = getIntent().getIntArrayExtra("topColors");
        float[] topPercentages = getIntent().getFloatArrayExtra("topPercentages");
        float coverage = getIntent().getFloatExtra("coverage", 0f);

        // ✅ Detect emotion from the combination of colours + coverage
        detectEmotion(topColors, topPercentages, coverage);

        // ✅ Update UI with detected emotion
        tvEmotionResult.setText(
                "Your drawing reflects " + detectedEmotion);
        tvMeaning.setText(emotionDescription);
        tvEmojiFace.setText(emotionEmoji);

        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "My emotion analysis: " + detectedEmotion +
                            " " + emotionEmoji + " #HealingJourney");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // ✅ Save emotion to Firestore
        btnSaveProgress.setOnClickListener(v ->
                saveEmotionToFirestore());

        tvDrawAgain.setOnClickListener(v -> {
            startActivity(new Intent(
                    EmotionActivity.this, ArtActivity.class));
            finish();
        });
    }

    // ✅ Colour category buckets — same thresholds as before, just factored
    // out so they can be applied to each of the top colours used.
    private static final String CAT_RED = "RED";
    private static final String CAT_BLUE = "BLUE";
    private static final String CAT_GREEN = "GREEN";
    private static final String CAT_YELLOW = "YELLOW";
    private static final String CAT_PURPLE = "PURPLE";
    private static final String CAT_DARK = "DARK";
    private static final String CAT_OTHER = "OTHER";

    private String classifyColorCategory(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (red > 150 && green < 100 && blue < 100) return CAT_RED;
        if (blue > 150 && red < 100 && green < 100) return CAT_BLUE;
        if (green > 150 && red < 100 && blue < 100) return CAT_GREEN;
        if (red > 150 && green > 150 && blue < 100) return CAT_YELLOW;
        if (red > 100 && blue > 100 && green < 100) return CAT_PURPLE;
        if (red < 50 && green < 50 && blue < 50) return CAT_DARK;
        return CAT_OTHER;
    }

    // ✅ Applies one of the existing emotion categories — text/emoji unchanged
    private void applyEmotion(String category) {
        switch (category) {
            case CAT_RED:
                detectedEmotion = "Stressed / Angry";
                emotionEmoji = "😟";
                emotionDescription = "Your use of red tones suggests " +
                        "stress or strong emotions. Consider taking deep " +
                        "breaths and practicing mindfulness. 🌬️";
                break;
            case CAT_BLUE:
                detectedEmotion = "Calm / Peaceful";
                emotionEmoji = "😌";
                emotionDescription = "Blue tones reflect a calm and " +
                        "peaceful state of mind. You seem to be in a " +
                        "balanced emotional space. 🌊";
                break;
            case CAT_GREEN:
                detectedEmotion = "Hopeful / Healing";
                emotionEmoji = "🌱";
                emotionDescription = "Green tones suggest growth, " +
                        "hope and healing. You are on a positive " +
                        "emotional journey. 🌿";
                break;
            case CAT_YELLOW:
                detectedEmotion = "Happy / Joyful";
                emotionEmoji = "😊";
                emotionDescription = "Yellow tones reflect happiness " +
                        "and optimism. You seem to be in a joyful and " +
                        "energetic mood! ☀️";
                break;
            case CAT_PURPLE:
                detectedEmotion = "Creative / Imaginative";
                emotionEmoji = "🎨";
                emotionDescription = "Purple tones suggest creativity " +
                        "and deep thinking. You are in a reflective and " +
                        "imaginative state. ✨";
                break;
            case CAT_DARK:
                detectedEmotion = "Sad / Lonely";
                emotionEmoji = "😔";
                emotionDescription = "Dark tones may reflect sadness " +
                        "or feeling withdrawn. It is okay to feel this way. " +
                        "Reach out for support if needed. 💙";
                break;
            default:
                detectedEmotion = "Balanced / Neutral";
                emotionEmoji = "🙂";
                emotionDescription = "Your mixed colors suggest a " +
                        "balanced emotional state. You are processing " +
                        "multiple feelings in a healthy way. 🌈";
        }
    }

    /**
     * ✅ Rule-based detection using the combination of the top colours
     * used (with their share of total usage) and how much of the
     * canvas was covered — instead of a single colour check.
     *
     * - Each top colour is bucketed into a category, and its usage
     *   percentage is added to that category's running score.
     * - A clearly leading category (or a leading category backed by
     *   heavy canvas coverage) is trusted directly.
     * - When no category clearly leads, coverage breaks the tie:
     *   very little of the canvas touched reads as hesitancy/low
     *   engagement, otherwise it's treated as genuinely mixed feelings.
     */
    private void detectEmotion(int[] colors, float[] percentages, float coverage) {
        if (colors == null || colors.length == 0) {
            applyEmotion(CAT_OTHER);
            return;
        }

        Map<String, Float> categoryScores = new HashMap<>();
        for (int i = 0; i < colors.length; i++) {
            String category = classifyColorCategory(colors[i]);
            float share = (percentages != null && i < percentages.length)
                    ? percentages[i] : 0f;
            categoryScores.merge(category, share, Float::sum);
        }

        String topCategory = CAT_OTHER;
        float topScore = -1f;
        for (Map.Entry<String, Float> e : categoryScores.entrySet()) {
            if (e.getValue() > topScore) {
                topScore = e.getValue();
                topCategory = e.getKey();
            }
        }

        float secondScore = 0f;
        for (Map.Entry<String, Float> e : categoryScores.entrySet()) {
            if (!e.getKey().equals(topCategory) && e.getValue() > secondScore) {
                secondScore = e.getValue();
            }
        }

        float margin = topScore - secondScore;
        boolean confidentLead = topScore >= 45f && margin >= 15f;
        boolean strongExpression = coverage >= 60f;
        boolean lowEngagement = coverage <= 15f;

        if (strongExpression && topScore >= 35f) {
            // Plenty of the canvas covered and one colour clearly leads
            applyEmotion(topCategory);
        } else if (confidentLead) {
            // One colour category clearly dominates the palette used
            applyEmotion(topCategory);
        } else if (lowEngagement
                && !topCategory.equals(CAT_YELLOW)
                && !topCategory.equals(CAT_GREEN)) {
            // Very little of the canvas touched — read as hesitancy/withdrawal,
            // unless the little that was drawn still leaned clearly positive
            applyEmotion(CAT_DARK);
        } else {
            // Colours are mixed/competing with no clear winner
            applyEmotion(CAT_OTHER);
        }
    }

    private void saveEmotionToFirestore() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> emotionData = new HashMap<>();
        emotionData.put("emotion", detectedEmotion);
        emotionData.put("emoji", emotionEmoji);
        emotionData.put("timestamp",
                com.google.firebase.Timestamp.now());
        emotionData.put("userId", userId);

        db.collection("emotions")
                .add(emotionData)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this,
                            "Saved to progress! 🎉",
                            Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper())
                            .postDelayed(() -> {
                                startActivity(new Intent(
                                        EmotionActivity.this,
                                        ProgressActivity.class));
                                finish();
                            }, 1000);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}