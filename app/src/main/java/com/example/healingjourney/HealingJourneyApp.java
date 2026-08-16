package com.example.healingjourney;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class HealingJourneyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ✅ Apply the user's saved dark mode preference as soon as the
        // app process starts, before any Activity is created — this
        // avoids a light-mode flash when the app is set to dark mode.
        SharedPreferences prefs = getSharedPreferences(
                "settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("darkMode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
    }
}