package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnChatAI = findViewById(R.id.btnChatAI);
        LinearLayout navArt = findViewById(R.id.navArt);
        LinearLayout navChat = findViewById(R.id.navChat);
        LinearLayout navProgress = findViewById(R.id.navProgress);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        btnStartDrawing.setOnClickListener(v -> {
            // Go to Art screen
        });

        btnChatAI.setOnClickListener(v -> {
            // Go to Chat screen
        });

        navArt.setOnClickListener(v -> {
            // Go to Art screen
        });

        navChat.setOnClickListener(v -> {
            // Go to Chat screen
        });

        navProgress.setOnClickListener(v -> {
            // Go to Progress screen
        });

        navProfile.setOnClickListener(v -> {
            // Go to Profile screen
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        btnChatAI.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
        });

        navChat.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
        });
        btnStartDrawing.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ArtActivity.class));
        });

        navArt.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ArtActivity.class));
        });
        navProgress.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProgressActivity.class));
        });

        findViewById(R.id.btnViewProgress).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProgressActivity.class));
        });
    }
}