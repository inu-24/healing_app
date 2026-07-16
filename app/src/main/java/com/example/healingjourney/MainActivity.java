package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        TextView tvSignIn = findViewById(R.id.tvSignIn);
        tvSignIn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }
}