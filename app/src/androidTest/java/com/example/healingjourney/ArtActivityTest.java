package com.example.healingjourney;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ArtActivityTest {

    @Test
    public void defaultMode_isFill() {
        try (ActivityScenario<ArtActivity> scenario =
                     ActivityScenario.launch(ArtActivity.class)) {

            scenario.onActivity(activity ->
                    assertEquals(DrawingView.Mode.FILL, activity.drawingView.getMode()));
        }
    }

    @Test
    public void tappingFillButton_togglesToDrawModeAndBack() {
        try (ActivityScenario<ArtActivity> scenario =
                     ActivityScenario.launch(ArtActivity.class)) {

            onView(withId(R.id.btnFill)).perform(click());
            scenario.onActivity(activity ->
                    assertEquals(DrawingView.Mode.DRAW, activity.drawingView.getMode()));

            onView(withId(R.id.btnFill)).perform(click());
            scenario.onActivity(activity ->
                    assertEquals(DrawingView.Mode.FILL, activity.drawingView.getMode()));
        }
    }

    @Test
    public void swipingOnCanvas_increasesCoverage() {
        try (ActivityScenario<ArtActivity> scenario =
                     ActivityScenario.launch(ArtActivity.class)) {

            // Switch to Draw mode so a swipe actually draws a stroke
            onView(withId(R.id.btnFill)).perform(click());
            onView(withId(R.id.colorRed)).perform(click());
            onView(withId(R.id.drawingView)).perform(swipeRight());

            scenario.onActivity(activity ->
                    assertTrue("Expected coverage > 0 after drawing",
                            activity.drawingView.getCanvasCoverage() > 0f));
        }
    }

    @Test
    public void clearCanvas_resetsCoverageToZero() {
        try (ActivityScenario<ArtActivity> scenario =
                     ActivityScenario.launch(ArtActivity.class)) {

            onView(withId(R.id.btnFill)).perform(click());
            onView(withId(R.id.drawingView)).perform(swipeRight());
            onView(withId(R.id.btnClear)).perform(click());

            scenario.onActivity(activity ->
                    assertEquals(0f, activity.drawingView.getCanvasCoverage(), 0.01f));
        }
    }

    @Test
    public void undo_removesLastStroke() {
        try (ActivityScenario<ArtActivity> scenario =
                     ActivityScenario.launch(ArtActivity.class)) {

            onView(withId(R.id.btnFill)).perform(click());
            onView(withId(R.id.drawingView)).perform(swipeRight());
            onView(withId(R.id.btnUndo)).perform(click());

            scenario.onActivity(activity ->
                    assertEquals(0f, activity.drawingView.getCanvasCoverage(), 0.01f));
        }
    }
}