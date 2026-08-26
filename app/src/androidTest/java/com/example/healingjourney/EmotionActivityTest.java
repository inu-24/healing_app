package com.example.healingjourney;

import android.content.Intent;
import android.graphics.Color;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class EmotionActivityTest {

    private Intent buildIntent(int[] colors, float[] percentages, float coverage) {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(), EmotionActivity.class);
        if (colors != null) intent.putExtra("topColors", colors);
        if (percentages != null) intent.putExtra("topPercentages", percentages);
        intent.putExtra("coverage", coverage);
        return intent;
    }

    @Test
    public void redDominant_highCoverage_showsStressedAngry() {
        Intent intent = buildIntent(new int[]{Color.RED}, new float[]{80f}, 70f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Stressed / Angry"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("😟")));
        }
    }

    @Test
    public void blueDominant_confidentLead_showsCalmPeaceful() {
        Intent intent = buildIntent(new int[]{Color.BLUE}, new float[]{60f}, 20f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Calm / Peaceful"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("😌")));
        }
    }

    @Test
    public void greenDominant_showsHopefulHealing() {
        Intent intent = buildIntent(new int[]{Color.GREEN}, new float[]{50f}, 30f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Hopeful / Healing"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("🌱")));
        }
    }

    @Test
    public void yellowDominant_showsHappyJoyful() {
        Intent intent = buildIntent(new int[]{Color.YELLOW}, new float[]{50f}, 30f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Happy / Joyful"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("😊")));
        }
    }

    @Test
    public void lowEngagement_showsSadLonely() {
        Intent intent = buildIntent(new int[]{Color.BLUE}, new float[]{30f}, 10f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Sad / Lonely"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("😔")));
        }
    }

    @Test
    public void noColorData_showsBalancedNeutralDefault() {
        Intent intent = buildIntent(null, null, 0f);
        try (ActivityScenario<EmotionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvEmotionResult))
                    .check(matches(withText(containsString("Balanced / Neutral"))));
            onView(withId(R.id.tvEmojiFace)).check(matches(withText("🙂")));
        }
    }
}