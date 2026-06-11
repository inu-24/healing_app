package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Correct view types matching XML
        TextView tvName = findViewById(R.id.tvName);
        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);

        // Navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navArt = findViewById(R.id.navArt);
        LinearLayout navProgress = findViewById(R.id.navProgress);
        LinearLayout navChat = findViewById(R.id.navChat);

        // Load user data from Firestore
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("fullName");
                            if (name != null) tvName.setText(name);
                        }
                    });
        }

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // ✅ Logout - clears all screens → goes to Login
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this,
                    "Logged out successfully!",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        navArt.setOnClickListener(v ->
                startActivity(new Intent(this, ArtActivity.class)));

        navProgress.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));

        navChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
    }
}