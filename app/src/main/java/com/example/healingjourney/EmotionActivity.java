package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmotionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnShare = findViewById(R.id.btnShare);
        Button btnSaveProgress = findViewById(R.id.btnSaveProgress);
        TextView tvDrawAgain = findViewById(R.id.tvDrawAgain);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Share button
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "My emotion analysis from Healing Journey: " +
                            "Feeling calm with slight stress tones 🌿");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Save to progress
        btnSaveProgress.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Saved to your progress! 🎉",
                    Toast.LENGTH_SHORT).show();

            // Go to Progress screen
            new android.os.Handler().postDelayed(() -> {
                startActivity(new Intent(
                        EmotionActivity.this,
                        ProgressActivity.class));
                finish();
            }, 1000);
        });

        // Draw Again
        tvDrawAgain.setOnClickListener(v -> {
            startActivity(new Intent(
                    EmotionActivity.this,
                    ArtActivity.class));
            finish();
        });
    }
}