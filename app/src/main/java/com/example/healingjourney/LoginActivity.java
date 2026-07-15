package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button btnLogin, btnGoogle;
    EditText etEmail, etPassword;
    TextView tvRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Already logged in → go to Home
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(
                    this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword =
                findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this,
                        "Google Sign In coming soon! 🚀",
                        Toast.LENGTH_SHORT).show());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText()
                    .toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Reset email sent! " +
                                            "Check your inbox 📧",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Error: " + task
                                            .getException()
                                            .getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @SuppressLint("SetTextI18n")
    private void loginUser() {
        String email = etEmail.getText()
                .toString().trim();
        String password = etPassword.getText()
                .toString().trim();

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

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Welcome back! 💚",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(
                                LoginActivity.this,
                                HomeActivity.class));
                        finish();
                    } else {
                        String error = task.getException()
                                != null ?
                                task.getException().getMessage()
                                : "Login failed";

                        // User friendly error messages
                        if (error.contains(
                                "password is invalid") ||
                                error.contains("no user")) {
                            Toast.makeText(this,
                                    "Incorrect email or password",
                                    Toast.LENGTH_LONG).show();
                        } else if (error.contains(
                                "network")) {
                            Toast.makeText(this,
                                    "No internet connection",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Login failed: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Log In");
                    }
                });
    }
}