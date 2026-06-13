package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// ✅ Changed from AppCompatActivity to BaseActivity
public class ProfileActivity extends BaseActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNav(); // ← Uses BaseActivity's version now

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView tvName = findViewById(R.id.tvName);
        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);

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
    }

}