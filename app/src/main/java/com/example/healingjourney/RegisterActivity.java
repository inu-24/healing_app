package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    EditText etFullName, etEmail,
            etPassword, etConfirmPassword;
    Button btnSignUp;
    TextView tvLogin;
    CheckBox cbTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        cbTerms = findViewById(R.id.cbTerms);

        btnSignUp.setOnClickListener(v ->
                registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(
                    RegisterActivity.this,
                    LoginActivity.class));
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private void registerUser() {
        String fullName = etFullName.getText()
                .toString().trim();
        String email = etEmail.getText()
                .toString().trim();
        String password = etPassword.getText()
                .toString().trim();
        String confirmPassword = etConfirmPassword
                .getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError(
                    "Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    "Please agree to Terms & Privacy Policy",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(
                        email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth
                                .getCurrentUser().getUid();

                        // Update display name
                        mAuth.getCurrentUser()
                                .updateProfile(
                                        new com.google.firebase.auth
                                                .UserProfileChangeRequest
                                                .Builder()
                                                .setDisplayName(fullName)
                                                .build());

                        // Save to Firestore
                        saveUserToFirestore(
                                userId, fullName, email);

                    } else {
                        String error = task.getException()
                                != null ?
                                task.getException().getMessage()
                                : "Registration failed";

                        if (error.contains(
                                "email address is already")) {
                            Toast.makeText(this,
                                    "Email already registered. " +
                                            "Please login instead.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Registration failed: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Create Account");
                    }
                });
    }

    private void saveUserToFirestore(
            String userId, String fullName, String email) {

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("userId", userId);
        user.put("createdAt",
                com.google.firebase.Timestamp.now());
        user.put("sessions", 0);
        user.put("artworks", 0);
        user.put("streak", 0);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Welcome to Healing Journey! 💚🌿",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(
                            RegisterActivity.this,
                            HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Create Account");
                });
    }
}