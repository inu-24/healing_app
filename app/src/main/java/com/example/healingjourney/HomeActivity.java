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
import java.util.Calendar;

public class HomeActivity extends BaseActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String selectedStressLevel = null;
    TextView tvGreeting, tvUsername, tvHomeEmotions,
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

        tvGreeting = findViewById(R.id.tvGreeting);
        tvUsername = findViewById(R.id.tvUsername);
        tvHomeEmotions = findViewById(R.id.tvHomeEmotions);
        tvHomeArtworks = findViewById(R.id.tvHomeArtworks);
        tvHomeStreak = findViewById(R.id.tvHomeStreak);
        tvLatestEmotion = findViewById(R.id.tvLatestEmotion);
        tvLatestEmoji = findViewById(R.id.tvLatestEmoji);
        tvHomeSessionCount =
                findViewById(R.id.tvHomeSessionCount);

        setGreeting();

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
            ProfileImageHelper.loadProfileImage(this, ivProfile);
        }

        ivProfile.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ProfileActivity.class)));

        btnStartDrawing.setOnClickListener(v -> {
            if (selectedStressLevel == null) {
                android.widget.Toast.makeText(this,
                        "Tap how you're feeling above first 🙂",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, MandalaActivity.class);
            intent.putExtra("stressLevel", selectedStressLevel);
            startActivity(intent);
        });

        btnChatAI.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ChatActivity.class)));

        btnViewProgress.setOnClickListener(v ->
                startActivity(new Intent(
                        HomeActivity.this,
                        ProgressActivity.class)));
    }

    @SuppressLint("SetTextI18n")
    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }
        tvGreeting.setText(greeting);
    }

    private void selectMood(TextView selected) {
        for (TextView mood : moodViews) {
            mood.setSelected(mood == selected);
        }

        // mood1/mood2 = 😢 😔  → High stress
        // mood3       = 😐    → Medium stress
        // mood4/mood5 = 🙂 😄 → Low stress
        int id = selected.getId();
        if (id == R.id.mood1 || id == R.id.mood2) {
            selectedStressLevel = "High";
        } else if (id == R.id.mood3) {
            selectedStressLevel = "Medium";
        } else {
            selectedStressLevel = "Low";
        }

        saveMoodCheckIn(selectedStressLevel);
    }

    private void saveMoodCheckIn(String stressLevel) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("userId", userId);
        data.put("stressLevel", stressLevel);
        data.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("moodCheckins").add(data);
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