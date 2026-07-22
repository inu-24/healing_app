package com.example.healingjourney;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout itemEditProfile =
                findViewById(R.id.itemEditProfile);
        LinearLayout itemChangePassword =
                findViewById(R.id.itemChangePassword);
        LinearLayout itemPrivacyPolicy =
                findViewById(R.id.itemPrivacyPolicy);
        LinearLayout itemTerms =
                findViewById(R.id.itemTerms);
        LinearLayout itemRateApp =
                findViewById(R.id.itemRateApp);
        LinearLayout itemDeleteAccount =
                findViewById(R.id.itemDeleteAccount);
        SwitchCompat switchNotifications =
                findViewById(R.id.switchNotifications);
        SwitchCompat switchDarkMode =
                findViewById(R.id.switchDarkMode);

        // Load saved preferences
        SharedPreferences prefs = getSharedPreferences(
                "settings", MODE_PRIVATE);
        switchNotifications.setChecked(
                prefs.getBoolean("notifications", true));
        switchDarkMode.setChecked(
                prefs.getBoolean("darkMode", false));

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Edit Profile
        itemEditProfile.setOnClickListener(v ->
                startActivity(new Intent(
                        this, EditProfileActivity.class)));

        // Change Password
        itemChangePassword.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String email = mAuth.getCurrentUser()
                        .getEmail();
                if (email != null) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(t ->
                                    Toast.makeText(this,
                                            "Password reset email sent! 📧",
                                            Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                }
            }
        });

        // Notifications toggle
        switchNotifications.setOnCheckedChangeListener(
                (btn, checked) -> {
                    prefs.edit()
                            .putBoolean("notifications", checked)
                            .apply();
                    Toast.makeText(this,
                            checked ?
                                    "Notifications enabled 🔔" :
                                    "Notifications disabled",
                            Toast.LENGTH_SHORT).show();
                });

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener(
                (btn, checked) -> {
                    prefs.edit()
                            .putBoolean("darkMode", checked)
                            .apply();
                    Toast.makeText(this,
                            checked ?
                                    "Dark mode coming soon! 🌙" :
                                    "Light mode",
                            Toast.LENGTH_SHORT).show();
                });

        // Privacy Policy
        itemPrivacyPolicy.setOnClickListener(v ->
                Toast.makeText(this,
                        "Privacy Policy - Healing Journey 🌿",
                        Toast.LENGTH_SHORT).show());

        // Terms
        itemTerms.setOnClickListener(v ->
                Toast.makeText(this,
                        "Terms of Service - Healing Journey",
                        Toast.LENGTH_SHORT).show());

        // Rate App
        itemRateApp.setOnClickListener(v ->
                Toast.makeText(this,
                        "Thank you for using Healing Journey! ⭐",
                        Toast.LENGTH_SHORT).show());

        // Delete Account
        itemDeleteAccount.setOnClickListener(v ->
                showDeleteAccountDialog());
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage(
                        "Are you sure you want to delete " +
                                "your account? This action cannot " +
                                "be undone.")
                .setPositiveButton("Delete",
                        (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        // Delete Firestore data first
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> {
                    // Delete Auth account
                    mAuth.getCurrentUser().delete()
                            .addOnSuccessListener(t -> {
                                Toast.makeText(this,
                                        "Account deleted",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(
                                        this,
                                        LoginActivity.class);
                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                });
    }
}