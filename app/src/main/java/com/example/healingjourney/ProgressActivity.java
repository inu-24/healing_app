package com.example.healingjourney;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    TextView tabWeek, tabMonth, tabYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        TextView btnBack = findViewById(R.id.btnBack);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabYear = findViewById(R.id.tabYear);

        btnBack.setOnClickListener(v -> finish());

        tabWeek.setOnClickListener(v -> setActiveTab("week"));
        tabMonth.setOnClickListener(v -> setActiveTab("month"));
        tabYear.setOnClickListener(v -> setActiveTab("year"));
    }

    private void setActiveTab(String tab) {
        // Reset all tabs
        tabWeek.setBackgroundColor(0x00000000);
        tabMonth.setBackgroundColor(0x00000000);
        tabYear.setBackgroundColor(0x00000000);
        tabWeek.setTextColor(0xFF2E7D32);
        tabMonth.setTextColor(0xFF2E7D32);
        tabYear.setTextColor(0xFF2E7D32);

        // Set active tab
        switch (tab) {
            case "week":
                tabWeek.setBackgroundColor(0xFF2E7D32);
                tabWeek.setTextColor(0xFFFFFFFF);
                break;
            case "month":
                tabMonth.setBackgroundColor(0xFF2E7D32);
                tabMonth.setTextColor(0xFFFFFFFF);
                break;
            case "year":
                tabYear.setBackgroundColor(0xFF2E7D32);
                tabYear.setTextColor(0xFFFFFFFF);
                break;
        }
    }
}