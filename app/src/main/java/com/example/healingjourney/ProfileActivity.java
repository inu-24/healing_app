package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName;
    Button btnEditProfile;
    LinearLayout btnLogout;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName        = findViewById(R.id.tvName);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout     = findViewById(R.id.btnLogout);
        btnBack       = findViewById(R.id.btnBack);

        // ── Back arrow ──────────────────────────────────────────
        btnBack.setOnClickListener(v -> finish());

        // ── Edit Profile ────────────────────────────────────────
        btnEditProfile.setOnClickListener(v -> showEditDialog());

        // ── Log out ─────────────────────────────────────────────
        btnLogout.setOnClickListener(v -> showLogoutConfirm());
    }

    // ── Edit name dialog ────────────────────────────────────────
    private void showEditDialog() {
        // Build the input field
        EditText input = new EditText(this);
        input.setHint("Enter your name");
        input.setText(tvName.getText());
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setMessage("Update your display name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        tvName.setText(newName);
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Logout confirmation dialog ───────────────────────────────
    private void showLogoutConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> {
                    // Clear any saved session here if needed
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}