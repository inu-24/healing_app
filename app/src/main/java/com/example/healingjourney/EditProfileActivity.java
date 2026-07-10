package com.example.healingjourney;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etFullName, etEmail, etBio;
    private Button btnSave;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setupBottomNav();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("fullName");
                            String email = document.getString("email");
                            String bio = document.getString("bio");

                            if (name != null) etFullName.setText(name);
                            if (email != null) etEmail.setText(email);
                            if (bio != null) etBio.setText(bio);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty()) {
            etFullName.setError("Name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Show loading state
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Update user's email in Firebase Authentication
            mAuth.getCurrentUser().updateEmail(email)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            // Update Firestore with all fields
                            updateFirestore(userId, fullName, email, bio);
                        } else {
                            // If email update fails, still update other fields
                            updateFirestore(userId, fullName, email, bio);
                            Toast.makeText(this, "Email update failed. Please sign in again.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Still update Firestore even if email update fails
                        updateFirestore(userId, fullName, email, bio);
                        Toast.makeText(this, "Please sign in again to update email", Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void updateFirestore(String userId, String fullName, String email, String bio) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("bio", bio);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                });
    }
}