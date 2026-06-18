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

        // ✅ Get dominant color from ArtActivity
        int dominantColor = getIntent().getIntExtra(
                "dominantColor", Color.GREEN);

        // ✅ Detect emotion from color
        detectEmotion(dominantColor);

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

    private void detectEmotion(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (red > 150 && green < 100 && blue < 100) {
            detectedEmotion = "Stressed / Angry";
            emotionEmoji = "😟";
            emotionDescription = "Your use of red tones suggests " +
                    "stress or strong emotions. Consider taking deep " +
                    "breaths and practicing mindfulness. 🌬️";
        } else if (blue > 150 && red < 100 && green < 100) {
            detectedEmotion = "Calm / Peaceful";
            emotionEmoji = "😌";
            emotionDescription = "Blue tones reflect a calm and " +
                    "peaceful state of mind. You seem to be in a " +
                    "balanced emotional space. 🌊";
        } else if (green > 150 && red < 100 && blue < 100) {
            detectedEmotion = "Hopeful / Healing";
            emotionEmoji = "🌱";
            emotionDescription = "Green tones suggest growth, " +
                    "hope and healing. You are on a positive " +
                    "emotional journey. 🌿";
        } else if (red > 150 && green > 150 && blue < 100) {
            detectedEmotion = "Happy / Joyful";
            emotionEmoji = "😊";
            emotionDescription = "Yellow tones reflect happiness " +
                    "and optimism. You seem to be in a joyful and " +
                    "energetic mood! ☀️";
        } else if (red > 100 && blue > 100 && green < 100) {
            detectedEmotion = "Creative / Imaginative";
            emotionEmoji = "🎨";
            emotionDescription = "Purple tones suggest creativity " +
                    "and deep thinking. You are in a reflective and " +
                    "imaginative state. ✨";
        } else if (red < 50 && green < 50 && blue < 50) {
            detectedEmotion = "Sad / Lonely";
            emotionEmoji = "😔";
            emotionDescription = "Dark tones may reflect sadness " +
                    "or feeling withdrawn. It is okay to feel this way. " +
                    "Reach out for support if needed. 💙";
        } else {
            detectedEmotion = "Balanced / Neutral";
            emotionEmoji = "🙂";
            emotionDescription = "Your mixed colors suggest a " +
                    "balanced emotional state. You are processing " +
                    "multiple feelings in a healthy way. 🌈";
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