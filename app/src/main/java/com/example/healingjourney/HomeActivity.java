package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNav();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextView tvUsername = findViewById(R.id.tvUsername);
        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnChatAI = findViewById(R.id.btnChatAI);
        Button btnViewProgress = findViewById(R.id.btnViewProgress);

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

        // ✅ Quick access buttons
        btnStartDrawing.setOnClickListener(v ->
                startActivity(new android.content.Intent(HomeActivity.this, ArtActivity.class)));

        btnChatAI.setOnClickListener(v ->
                startActivity(new android.content.Intent(HomeActivity.this, ChatActivity.class)));

        btnViewProgress.setOnClickListener(v ->
                startActivity(new android.content.Intent(HomeActivity.this, ProgressActivity.class)));


    }
}