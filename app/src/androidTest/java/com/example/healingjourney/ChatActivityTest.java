package com.example.healingjourney;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ChatActivityTest {

    @Test
    public void sendingMessage_showsUserBubbleAndTypingIndicator() {
        try (ActivityScenario<ChatActivity> scenario =
                     ActivityScenario.launch(ChatActivity.class)) {

            onView(withId(R.id.etMessage))
                    .perform(typeText("Hello, I feel anxious today"), closeSoftKeyboard());
            onView(withId(R.id.btnSend)).perform(click());

            // The user's own message should appear as a bubble
            onView(withText("Hello, I feel anxious today"))
                    .check(matches(isDisplayed()));

            // A "Typing..." placeholder should appear right away (no network wait needed)
            onView(withText("Typing..."))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void sendingMessage_clearsInputField() {
        try (ActivityScenario<ChatActivity> scenario =
                     ActivityScenario.launch(ChatActivity.class)) {

            onView(withId(R.id.etMessage))
                    .perform(typeText("Test message"), closeSoftKeyboard());
            onView(withId(R.id.btnSend)).perform(click());

            onView(withId(R.id.etMessage)).check(matches(withText("")));
        }
    }

    @Test
    public void emptyMessage_doesNothingOnSend() {
        try (ActivityScenario<ChatActivity> scenario =
                     ActivityScenario.launch(ChatActivity.class)) {

            onView(withId(R.id.etMessage)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.btnSend)).perform(click());

            // No "Typing..." bubble should appear since nothing was sent
            onView(withText("Typing...")).check(doesNotExist());
        }
    }
}