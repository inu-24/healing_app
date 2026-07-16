package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends BaseActivity {

    private static final long SEVEN_DAY_GOAL = 7;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    // Header
    TextView tvName;
    TextView tvJoinDate;

    // Mood summary
    TextView tvCheckInsCount;
    TextView tvGoodMoodDays;
    TextView tvMostFrequentMood;
    ProgressBar progressMood;

    // Achievements
    LinearLayout badgeFirstSteps;
    LinearLayout badgeArtist;
    TextView tvArtistSubtitle;
    LinearLayout badgeStreak;
    TextView tvStreakTitle;
    LinearLayout badgeSevenDay;
    TextView tvSevenDayIcon;
    TextView tvSevenDayTitle;
    TextView tvSevenDaySubtitle;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNav();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Header
        tvName = findViewById(R.id.tvName);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        // Mood summary
        tvCheckInsCount = findViewById(R.id.tvCheckInsCount);
        tvGoodMoodDays = findViewById(R.id.tvGoodMoodDays);
        tvMostFrequentMood = findViewById(R.id.tvMostFrequentMood);
        progressMood = findViewById(R.id.progressMood);

        // Achievements
        badgeFirstSteps = findViewById(R.id.badgeFirstSteps);
        badgeArtist = findViewById(R.id.badgeArtist);
        tvArtistSubtitle = findViewById(R.id.tvArtistSubtitle);
        badgeStreak = findViewById(R.id.badgeStreak);
        tvStreakTitle = findViewById(R.id.tvStreakTitle);
        badgeSevenDay = findViewById(R.id.badgeSevenDay);
        tvSevenDayIcon = findViewById(R.id.tvSevenDayIcon);
        tvSevenDayTitle = findViewById(R.id.tvSevenDayTitle);
        tvSevenDaySubtitle = findViewById(R.id.tvSevenDaySubtitle);

        loadUserData();

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, 1);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Pulls the profile + stats from users/{uid} in Firestore.
     * Expected fields on the document (adjust names to match your schema):
     *   fullName            (String)
     *   joinDate            (String, e.g. "Jan 2025")
     *   checkInsThisWeek    (Long)
     *   goodMoodDaysThisWeek(Long)
     *   topMoodName         (String)
     *   topMoodPercent      (Long, 0-100)
     *   currentStreak       (Long)
     *   artworksCount       (Long)
     */
    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(this::bindUserData)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    private void bindUserData(DocumentSnapshot document) {
        if (!document.exists()) return;

        // Header
        String name = document.getString("fullName");
        tvName.setText(name != null ? name : "Your Name");

        String joinDate = document.getString("joinDate");
        tvJoinDate.setText(joinDate != null ? "Healing since " + joinDate : "");

        // Mood summary
        long checkIns = getLong(document, "checkInsThisWeek");
        long goodMoodDays = getLong(document, "goodMoodDaysThisWeek");
        String topMoodName = document.getString("topMoodName");
        long topMoodPercent = getLong(document, "topMoodPercent");

        tvCheckInsCount.setText(String.valueOf(checkIns));
        tvGoodMoodDays.setText(String.valueOf(goodMoodDays));
        tvMostFrequentMood.setText(topMoodName != null
                ? "Most frequent mood: " + topMoodName + " (" + topMoodPercent + "%)"
                : "Most frequent mood");
        progressMood.setProgress((int) topMoodPercent);

        // Achievements
        long artworksCount = getLong(document, "artworksCount");
        long currentStreak = getLong(document, "currentStreak");

        bindFirstStepsBadge(checkIns);
        bindArtistBadge(artworksCount);
        bindStreakBadge(currentStreak);
        bindSevenDayBadge(currentStreak);
    }

    private void bindFirstStepsBadge(long checkIns) {
        boolean earned = checkIns > 0;
        badgeFirstSteps.setBackgroundResource(earned ? R.drawable.bg_badge_earned : R.drawable.bg_badge_locked);
        badgeFirstSteps.setAlpha(earned ? 1f : 0.55f);
    }

    @SuppressLint("SetTextI18n")
    private void bindArtistBadge(long artworksCount) {
        boolean earned = artworksCount > 0;
        badgeArtist.setBackgroundResource(earned ? R.drawable.bg_badge_earned : R.drawable.bg_badge_locked);
        badgeArtist.setAlpha(earned ? 1f : 0.55f);
        tvArtistSubtitle.setText(artworksCount + (artworksCount == 1 ? " artwork" : " artworks"));
    }

    @SuppressLint("SetTextI18n")
    private void bindStreakBadge(long currentStreak) {
        tvStreakTitle.setText(currentStreak + "-Day Streak");
        boolean earned = currentStreak > 0;
        badgeStreak.setBackgroundResource(earned ? R.drawable.bg_badge_earned : R.drawable.bg_badge_locked);
        badgeStreak.setAlpha(earned ? 1f : 0.55f);
    }

    @SuppressLint("SetTextI18n")
    private void bindSevenDayBadge(long currentStreak) {
        boolean unlocked = currentStreak >= SEVEN_DAY_GOAL;
        if (unlocked) {
            badgeSevenDay.setBackgroundResource(R.drawable.bg_badge_earned);
            badgeSevenDay.setAlpha(1f);
            tvSevenDayIcon.setText("🏆");
            tvSevenDayTitle.setText("7-Day Streak");
            tvSevenDayTitle.setTextColor(Color.parseColor("#1B5E20"));
            tvSevenDaySubtitle.setText("Unlocked!");
            tvSevenDaySubtitle.setTextColor(Color.parseColor("#5C8A5F"));
        } else {
            long remaining = SEVEN_DAY_GOAL - currentStreak;
            badgeSevenDay.setBackgroundResource(R.drawable.bg_badge_locked);
            badgeSevenDay.setAlpha(1f);
            tvSevenDayIcon.setText("🔒");
            tvSevenDayTitle.setText("7-Day Streak");
            tvSevenDayTitle.setTextColor(Color.parseColor("#888888"));
            tvSevenDaySubtitle.setText(remaining + " more day" + (remaining == 1 ? "" : "s"));
            tvSevenDaySubtitle.setTextColor(Color.parseColor("#AAAAAA"));
        }
    }

    private long getLong(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? value : 0L;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserData();
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        }
    }
}