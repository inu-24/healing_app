package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class EmotionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);
        setupBottomNav();

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnShare = findViewById(R.id.btnShare);
        Button btnSaveProgress = findViewById(R.id.btnSaveProgress);
        TextView tvDrawAgain = findViewById(R.id.tvDrawAgain);

        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "My emotion analysis from Healing Journey: " +
                            "Feeling calm with slight stress tones 🌿");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnSaveProgress.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Saved to your progress! 🎉",
                    Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(
                        EmotionActivity.this,
                        ProgressActivity.class));
                finish();
            }, 1000);
        });

        tvDrawAgain.setOnClickListener(v -> {
            startActivity(new Intent(
                    EmotionActivity.this,
                    ArtActivity.class));
            finish();
        });
    }
}