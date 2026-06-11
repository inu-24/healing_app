package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView tvUsername = findViewById(R.id.tvUsername);
        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnChatAI = findViewById(R.id.btnChatAI);
        Button btnViewProgress = findViewById(R.id.btnViewProgress);
        LinearLayout navArt = findViewById(R.id.navArt);
        LinearLayout navChat = findViewById(R.id.navChat);
        LinearLayout navProgress = findViewById(R.id.navProgress);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // Load username from Firestore
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("fullName");
                            if (name != null) {
                                tvUsername.setText(name + " 🌿");
                            }
                        }
                    });
        }

        // ✅ Each button has only ONE click listener
        btnStartDrawing.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ArtActivity.class)));

        btnChatAI.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ChatActivity.class)));

        btnViewProgress.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProgressActivity.class)));

        navArt.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ArtActivity.class)));

        navChat.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ChatActivity.class)));

        navProgress.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProgressActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }
}