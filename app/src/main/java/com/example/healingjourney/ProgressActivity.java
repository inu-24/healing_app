package com.example.healingjourney;

import android.os.Bundle;
import android.widget.TextView;

public class ProgressActivity extends BaseActivity {

    TextView tabWeek, tabMonth, tabYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        setupBottomNav();

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
        tabWeek.setBackgroundColor(0x00000000);
        tabMonth.setBackgroundColor(0x00000000);
        tabYear.setBackgroundColor(0x00000000);
        tabWeek.setTextColor(0xFF2E7D32);
        tabMonth.setTextColor(0xFF2E7D32);
        tabYear.setTextColor(0xFF2E7D32);

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