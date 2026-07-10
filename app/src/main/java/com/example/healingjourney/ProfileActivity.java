package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
    TextView tvName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNav(); // ← Uses BaseActivity's version now

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvName = findViewById(R.id.tvName);
        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        // Load user data from Firestore
        loadUserData();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // ✅ Edit Profile button - Navigate to EditProfileActivity
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, 1);
        });

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

    // Method to load user data
    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("fullName");
                            if (name != null) tvName.setText(name);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Handle result from EditProfileActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh the profile data after editing
            loadUserData();
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        }
    }
}