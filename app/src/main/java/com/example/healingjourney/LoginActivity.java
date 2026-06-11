package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

        // If already logged in → go to Home
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this,
                        "Enter your email first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Reset email sent! Check your inbox 📧",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @SuppressLint("SetTextI18n")
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

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
                        Toast.makeText(this,
                                "Login failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Log In");
                    }
                });
    }
}