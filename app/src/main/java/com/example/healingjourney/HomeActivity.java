package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeActivity extends BaseActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView tvUsername, tvHomeEmotions,
            tvHomeArtworks, tvHomeStreak,
            tvLatestEmotion, tvLatestEmoji,
            tvHomeSessionCount;
    TextView[] moodViews;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNav();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUsername = findViewById(R.id.tvUsername);
        tvHomeEmotions = findViewById(R.id.tvHomeEmotions);
        tvHomeArtworks = findViewById(R.id.tvHomeArtworks);
        tvHomeStreak = findViewById(R.id.tvHomeStreak);
        tvLatestEmotion = findViewById(R.id.tvLatestEmotion);
        tvLatestEmoji = findViewById(R.id.tvLatestEmoji);
        tvHomeSessionCount =
                findViewById(R.id.tvHomeSessionCount);

        Button btnStartDrawing =
                findViewById(R.id.btnStartDrawing);
        Button btnChatAI =
                findViewById(R.id.btnChatAI);
        Button btnViewProgress =
                findViewById(R.id.btnViewProgress);
        ImageView ivProfile = findViewById(R.id.ivProfile);

        moodViews = new TextView[]{
                findViewById(R.id.mood1),
                findViewById(R.id.mood2),
                findViewById(R.id.mood3),
                findViewById(R.id.mood4),
                findViewById(R.id.mood5)
        };
        for (TextView mood : moodViews) {
            mood.setOnClickListener(v -> selectMood((TextView) v));
        }

        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            loadRealStats();
            loadLatestEmotion();
        }

        ivProfile.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ProfileActivity.class)));

        btnStartDrawing.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        MandalaActivity.class)));

        btnChatAI.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ChatActivity.class)));

        btnViewProgress.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ProgressActivity.class)));
    }

    private void selectMood(TextView selected) {
        for (TextView mood : moodViews) {
            mood.setSelected(mood == selected);
        }
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String name =
                                doc.getString("fullName");
                        if (name != null && !name.isEmpty())
                            tvUsername.setText(name + " 🌿");
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void loadRealStats() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("emotions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    int count = snap.size();
                    tvHomeEmotions.setText(
                            String.valueOf(count));
                    tvHomeSessionCount.setText(
                            String.valueOf(count));
                    tvHomeStreak.setText(
                            Math.min(count, 30) + "🔥");
                });

        db.collection("artworks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap ->
                        tvHomeArtworks.setText(
                                String.valueOf(snap.size())));
    }

    @SuppressLint("SetTextI18n")
    private void loadLatestEmotion() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("emotions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        QueryDocumentSnapshot doc =
                                (QueryDocumentSnapshot)
                                        snap.getDocuments().get(0);
                        String emotion =
                                doc.getString("emotion");
                        String emoji =
                                doc.getString("emoji");
                        if (emotion != null)
                            tvLatestEmotion.setText(emotion);
                        if (emoji != null)
                            tvLatestEmoji.setText(emoji);
                    } else {
                        tvLatestEmotion.setText(
                                "Start your first session!");
                        tvLatestEmoji.setText("🌱");
                    }
                })
                .addOnFailureListener(e -> {
                    tvLatestEmotion.setText(
                            "Start your first session!");
                    tvLatestEmoji.setText("🌱");
                });
    }
}