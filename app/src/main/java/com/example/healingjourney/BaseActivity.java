package com.example.healingjourney;

import android.content.Intent;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navArt = findViewById(R.id.navArt);
        LinearLayout navProgress = findViewById(R.id.navProgress);
        LinearLayout navChat = findViewById(R.id.navChat);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null)
            navHome.setOnClickListener(v -> {
                if (!(this instanceof HomeActivity)) {
                    startActivity(new Intent(this, HomeActivity.class));
                }
            });

        if (navArt != null)
            navArt.setOnClickListener(v -> {
                if (!(this instanceof ArtActivity)) {
                    startActivity(new Intent(this, ArtActivity.class));
                }
            });

        if (navProgress != null)
            navProgress.setOnClickListener(v -> {
                if (!(this instanceof ProgressActivity)) {
                    startActivity(new Intent(this, ProgressActivity.class));
                }
            });

        if (navChat != null)
            navChat.setOnClickListener(v -> {
                if (!(this instanceof ChatActivity)) {
                    startActivity(new Intent(this, ChatActivity.class));
                }
            });

        if (navProfile != null)
            navProfile.setOnClickListener(v -> {
                if (!(this instanceof ProfileActivity)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                }
            });
    }
}