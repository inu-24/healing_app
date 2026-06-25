package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.Date;

public class ProgressActivity extends BaseActivity {

    TextView tabWeek, tabMonth, tabYear;
    TextView tvSessions, tvArtworks, tvStreak;
    ProgressBar progressCalm, progressHappy, progressStressed;
    View barMon, barTue, barWed, barThu, barFri, barSat, barSun;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String currentTab = "week";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        setupBottomNav();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Buttons
        TextView btnBack = findViewById(R.id.btnBack);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabYear = findViewById(R.id.tabYear);

        // Stats
        tvSessions = findViewById(R.id.tvSessions);
        tvArtworks = findViewById(R.id.tvArtworks);
        tvStreak = findViewById(R.id.tvStreak);

        // Progress bars
        progressCalm = findViewById(R.id.progressCalm);
        progressHappy = findViewById(R.id.progressHappy);
        progressStressed = findViewById(R.id.progressStressed);

        // Bar chart
        barMon = findViewById(R.id.barMon);
        barTue = findViewById(R.id.barTue);
        barWed = findViewById(R.id.barWed);
        barThu = findViewById(R.id.barThu);
        barFri = findViewById(R.id.barFri);
        barSat = findViewById(R.id.barSat);
        barSun = findViewById(R.id.barSun);

        btnBack.setOnClickListener(v -> finish());

        tabWeek.setOnClickListener(v -> {
            currentTab = "week";
            setActiveTab("week");
            loadProgressData();
        });
        tabMonth.setOnClickListener(v -> {
            currentTab = "month";
            setActiveTab("month");
            loadProgressData();
        });
        tabYear.setOnClickListener(v -> {
            currentTab = "year";
            setActiveTab("year");
            loadProgressData();
        });

        // ✅ Load real data on start
        loadProgressData();
    }

    @SuppressLint("SetTextI18n")
    private void loadProgressData() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        // Get start date based on selected tab
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (currentTab.equals("week")) {
            cal.set(Calendar.DAY_OF_WEEK,
                    cal.getFirstDayOfWeek());
        } else if (currentTab.equals("month")) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            cal.set(Calendar.DAY_OF_YEAR, 1);
        }

        Date startDate = cal.getTime();

        // ✅ Load emotions from Firestore
        db.collection("emotions")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp",
                        new Timestamp(startDate))
                .get()
                .addOnSuccessListener(snapshots -> {
                    int totalSessions = snapshots.size();
                    int calmCount = 0;
                    int happyCount = 0;
                    int stressedCount = 0;
                    int[] dailyCounts = new int[7];

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String emotion = doc.getString("emotion");
                        Timestamp ts = doc.getTimestamp("timestamp");

                        // Count emotion types
                        if (emotion != null) {
                            if (emotion.contains("Calm") ||
                                    emotion.contains("Hopeful") ||
                                    emotion.contains("Balanced")) {
                                calmCount++;
                            } else if (emotion.contains("Happy") ||
                                    emotion.contains("Creative")) {
                                happyCount++;
                            } else if (emotion.contains("Stressed") ||
                                    emotion.contains("Sad")) {
                                stressedCount++;
                            }
                        }

                        // Count by day of week for bar chart
                        if (ts != null) {
                            Calendar emotionCal =
                                    Calendar.getInstance();
                            emotionCal.setTime(ts.toDate());
                            int day = emotionCal.get(
                                    Calendar.DAY_OF_WEEK) - 1;
                            if (day >= 0 && day < 7) {
                                dailyCounts[day]++;
                            }
                        }
                    }

                    // ✅ Update sessions & streak
                    tvSessions.setText(
                            String.valueOf(totalSessions));
                    tvStreak.setText(
                            Math.min(totalSessions, 30) + "🔥");

                    // ✅ Update mood progress bars
                    int total = calmCount + happyCount + stressedCount;
                    if (total > 0) {
                        progressCalm.setProgress(
                                (calmCount * 100) / total);
                        progressHappy.setProgress(
                                (happyCount * 100) / total);
                        progressStressed.setProgress(
                                (stressedCount * 100) / total);
                    } else {
                        progressCalm.setProgress(0);
                        progressHappy.setProgress(0);
                        progressStressed.setProgress(0);
                    }

                    // ✅ Update bar chart
                    updateBarChart(dailyCounts);
                })
                .addOnFailureListener(e -> {
                    tvSessions.setText("0");
                    tvStreak.setText("0🔥");
                });

        // ✅ Load artworks count from Firestore
        db.collection("artworks")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp",
                        new Timestamp(startDate))
                .get()
                .addOnSuccessListener(snapshots ->
                        tvArtworks.setText(
                                String.valueOf(snapshots.size())))
                .addOnFailureListener(e ->
                        tvArtworks.setText("0"));
    }

    private void updateBarChart(int[] dailyCounts) {
        View[] bars = {
                barSun, barMon, barTue, barWed,
                barThu, barFri, barSat
        };

        // Find max value for scaling
        int max = 1;
        for (int count : dailyCounts) {
            if (count > max) max = count;
        }

        int maxHeightDp = 100;

        for (int i = 0; i < bars.length; i++) {
            if (bars[i] != null) {
                int heightDp = (dailyCounts[i] * maxHeightDp) / max;
                if (heightDp < 10) heightDp = 10;
                ViewGroup.LayoutParams params =
                        bars[i].getLayoutParams();
                params.height = dpToPx(heightDp);
                bars[i].setLayoutParams(params);

                // Green for days with activity
                if (dailyCounts[i] > 0) {
                    bars[i].setBackgroundColor(
                            0xFF2E7D32);
                } else {
                    bars[i].setBackgroundColor(
                            0xFFA5D6A7);
                }
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setActiveTab(String tab) {
        // Reset all tabs
        tabWeek.setBackgroundColor(0x00000000);
        tabMonth.setBackgroundColor(0x00000000);
        tabYear.setBackgroundColor(0x00000000);
        tabWeek.setTextColor(0xFF2E7D32);
        tabMonth.setTextColor(0xFF2E7D32);
        tabYear.setTextColor(0xFF2E7D32);

        // Set active tab
        switch (tab) {
            case "week":
                tabWeek.setBackgroundColor(0xFF2E7D32);
                tabWeek.setTextColor(0xFFFFFFFF);
                break;
            case "month":
                tabMonth.setBackgroundColor(0xFF2E7D32);
                tabMonth.setTextColor(0xFFFFFFFF);
                break;
            case "year":
                tabYear.setBackgroundColor(0xFF2E7D32);
                tabYear.setTextColor(0xFFFFFFFF);
                break;
        }
    }
}