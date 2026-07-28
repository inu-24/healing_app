package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProgressActivity extends BaseActivity {

    TextView tabWeek, tabMonth, tabYear;
    TextView tvSessions, tvArtworks, tvStreak;
    TextView tvEncouragement, tvGreatJob;
    ProgressBar progressCalm, progressHappy, progressStressed;
    View barMon, barTue, barWed, barThu, barFri, barSat, barSun;
    Button btnGenerateReport;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String currentTab = "week";

    // Cached stats, updated every time loadProgressData() finishes, so the
    // report button can use the latest numbers whenever it's tapped.
    int lastSessions = 0, lastArtworks = 0, lastCalm = 0, lastHappy = 0, lastStressed = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        setupBottomNav();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView btnBack = findViewById(R.id.btnBack);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabYear = findViewById(R.id.tabYear);
        tvSessions = findViewById(R.id.tvSessions);
        tvArtworks = findViewById(R.id.tvArtworks);
        tvStreak = findViewById(R.id.tvStreak);
        tvEncouragement = findViewById(R.id.tvEncouragement);
        tvGreatJob = findViewById(R.id.tvGreatJob);
        progressCalm = findViewById(R.id.progressCalm);
        progressHappy = findViewById(R.id.progressHappy);
        progressStressed = findViewById(R.id.progressStressed);
        barMon = findViewById(R.id.barMon);
        barTue = findViewById(R.id.barTue);
        barWed = findViewById(R.id.barWed);
        barThu = findViewById(R.id.barThu);
        barFri = findViewById(R.id.barFri);
        barSat = findViewById(R.id.barSat);
        barSun = findViewById(R.id.barSun);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        btnBack.setOnClickListener(v -> finish());

        btnGenerateReport.setOnClickListener(v -> generatePdfReport());

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

        loadProgressData();
    }

    @SuppressLint("SetTextI18n")
    private void loadProgressData() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        // ✅ Get ALL emotions for this user
        // then filter by date in code (no index needed!)
        db.collection("emotions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {

                    // Get date range
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    if (currentTab.equals("week")) {
                        cal.set(Calendar.DAY_OF_WEEK,
                                cal.getFirstDayOfWeek());
                    } else if (currentTab.equals("month")) {
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                    } else {
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                    }
                    Date startDate = cal.getTime();

                    int totalSessions = 0;
                    int calmCount = 0;
                    int happyCount = 0;
                    int stressedCount = 0;
                    int[] dailyCounts = new int[7];

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Timestamp ts = doc.getTimestamp("timestamp");

                        // ✅ Filter by date in code
                        if (ts != null &&
                                ts.toDate().after(startDate)) {

                            totalSessions++;
                            String emotion = doc.getString("emotion");

                            if (emotion != null) {
                                if (emotion.contains("Calm") ||
                                        emotion.contains("Hopeful") ||
                                        emotion.contains("Balanced") ||
                                        emotion.contains("Peaceful")) {
                                    calmCount++;
                                } else if (emotion.contains("Happy") ||
                                        emotion.contains("Joyful") ||
                                        emotion.contains("Creative")) {
                                    happyCount++;
                                } else if (emotion.contains("Stressed") ||
                                        emotion.contains("Angry") ||
                                        emotion.contains("Sad") ||
                                        emotion.contains("Lonely")) {
                                    stressedCount++;
                                }
                            }

                            // Count by day
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

                    // ✅ Cache for the PDF report
                    lastSessions = totalSessions;
                    lastCalm = calmCount;
                    lastHappy = happyCount;
                    lastStressed = stressedCount;

                    // ✅ Update Sessions
                    tvSessions.setText(
                            String.valueOf(totalSessions));

                    // ✅ Update Streak
                    tvStreak.setText(
                            totalSessions + "🔥");

                    // ✅ Update Mood Bars
                    int total = calmCount + happyCount +
                            stressedCount;
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

                    // ✅ Update encouragement message
                    updateEncouragement(totalSessions,
                            calmCount, stressedCount);

                    // ✅ Update Bar Chart
                    updateBarChart(dailyCounts);
                })
                .addOnFailureListener(e -> {
                    tvSessions.setText("0");
                    tvStreak.setText("0🔥");
                    tvArtworks.setText("0");
                });

        // ✅ Load artworks
        db.collection("artworks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {

                    // Filter by date
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

                    int artworkCount = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Timestamp ts = doc.getTimestamp("timestamp");
                        if (ts != null &&
                                ts.toDate().after(startDate)) {
                            artworkCount++;
                        }
                    }
                    lastArtworks = artworkCount;
                    tvArtworks.setText(
                            String.valueOf(artworkCount));
                })
                .addOnFailureListener(e ->
                        tvArtworks.setText("0"));
    }

    @SuppressLint("SetTextI18n")
    private void updateEncouragement(int sessions,
                                     int calm, int stressed) {

        if (tvGreatJob == null ||
                tvEncouragement == null) return;

        if (sessions == 0) {
            tvGreatJob.setText("👋 Welcome!");
            tvEncouragement.setText(
                    "Start your healing journey by " +
                            "coloring a mandala! 🎨");
        } else if (sessions < 3) {
            tvGreatJob.setText("🌱 Great start!");
            tvEncouragement.setText(
                    "You have completed " + sessions +
                            " session(s). Keep going! 💚");
        } else if (stressed > calm) {
            tvGreatJob.setText("💙 Keep healing!");
            tvEncouragement.setText(
                    "You seem stressed lately. " +
                            "Try coloring with blue or green " +
                            "tones to feel calmer. 🌊");
        } else {
            tvGreatJob.setText("🎉 Great job!");
            tvEncouragement.setText(
                    "You have completed " + sessions +
                            " sessions! Your emotional wellness " +
                            "is improving. Keep it up! 🌿");
        }
    }

    private void updateBarChart(int[] dailyCounts) {
        View[] bars = {
                barSun, barMon, barTue, barWed,
                barThu, barFri, barSat
        };

        int max = 1;
        for (int count : dailyCounts) {
            if (count > max) max = count;
        }

        for (int i = 0; i < bars.length; i++) {
            if (bars[i] != null) {
                int heightDp =
                        (dailyCounts[i] * 100) / max;
                if (heightDp < 8) heightDp = 8;
                ViewGroup.LayoutParams params =
                        bars[i].getLayoutParams();
                params.height = dpToPx(heightDp);
                bars[i].setLayoutParams(params);

                bars[i].setBackgroundColor(
                        dailyCounts[i] > 0 ?
                                0xFF2E7D32 : 0xFFA5D6A7);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
        private void generatePdfReport() {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size in points
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint title = new Paint();
            title.setTextSize(20);
            title.setColor(0xFF2E7D32);
            title.setFakeBoldText(true);

            Paint label = new Paint();
            label.setTextSize(13);
            label.setColor(0xFF444444);

            Paint value = new Paint();
            value.setTextSize(13);
            value.setColor(0xFF000000);
            value.setFakeBoldText(true);

            String period = currentTab.substring(0, 1).toUpperCase() + currentTab.substring(1);
            String generatedOn = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new java.util.Date());

            int y = 50;
            canvas.drawText("Healing Journey — " + period + "ly Report", 40, y, title);

            y += 30;
            label.setTextSize(11);
            canvas.drawText("Generated on " + generatedOn, 40, y, label);

            y += 40;
            canvas.drawText("Sessions completed:", 40, y, label);
            canvas.drawText(String.valueOf(lastSessions), 250, y, value);

            y += 25;
            canvas.drawText("Artworks created:", 40, y, label);
            canvas.drawText(String.valueOf(lastArtworks), 250, y, value);

            y += 40;
            canvas.drawText("Mood breakdown", 40, y, title);

            int moodTotal = lastCalm + lastHappy + lastStressed;
            int calmPct = moodTotal > 0 ? (lastCalm * 100) / moodTotal : 0;
            int happyPct = moodTotal > 0 ? (lastHappy * 100) / moodTotal : 0;
            int stressedPct = moodTotal > 0 ? (lastStressed * 100) / moodTotal : 0;

            y += 30;
            canvas.drawText("Calm / Peaceful:", 40, y, label);
            canvas.drawText(calmPct + "%", 250, y, value);

            y += 25;
            canvas.drawText("Happy / Joyful:", 40, y, label);
            canvas.drawText(happyPct + "%", 250, y, value);

            y += 25;
            canvas.drawText("Stressed:", 40, y, label);
            canvas.drawText(stressedPct + "%", 250, y, value);

            y += 40;
            canvas.drawText("Note", 40, y, title);
            y += 25;
            String note = tvGreatJob != null ? tvGreatJob.getText().toString() : "";
            String encouragement = tvEncouragement != null ? tvEncouragement.getText().toString() : "";
            canvas.drawText(note, 40, y, label);
            y += 20;
            canvas.drawText(encouragement, 40, y, label);

            document.finishPage(page);

            try {
                File dir = new File(getExternalFilesDir(null), "Documents");
                if (!dir.exists()) dir.mkdirs();

                String fileName = "HealingJourney_" + currentTab + "_report.pdf";
                File file = new File(dir, fileName);
                FileOutputStream out = new FileOutputStream(file);
                document.writeTo(out);
                out.close();
                document.close();

                Uri uri = FileProvider.getUriForFile(
                        this, "com.example.healingjourney.fileprovider", file);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share your report"));

            } catch (Exception e) {
                document.close();
                Toast.makeText(this, "Couldn't create report: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }


    private void setActiveTab(String tab) {
        tabWeek.setBackgroundColor(0x00000000);
        tabMonth.setBackgroundColor(0x00000000);
        tabYear.setBackgroundColor(0x00000000);
        tabWeek.setTextColor(0xFF2E7D32);
        tabMonth.setTextColor(0xFF2E7D32);
        tabYear.setTextColor(0xFF2E7D32);

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