package com.example.healingjourney;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Test
    public void emptyEmail_showsRequiredError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            onView(withId(R.id.etEmail)).perform(clearText());
            onView(withId(R.id.etPassword)).perform(clearText());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etEmail))
                    .check(matches(hasErrorText("Email is required")));
        }
    }

    @Test
    public void invalidEmailFormat_showsValidEmailError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("not-an-email"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(clearText(), typeText("somepassword"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etEmail))
                    .check(matches(hasErrorText("Enter a valid email")));
        }
    }

    @Test
    public void emptyPassword_showsRequiredError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("test@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword)).perform(clearText());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etPassword))
                    .check(matches(hasErrorText("Password is required")));
        }
    }

    @Test
    public void shortPassword_showsLengthError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {

            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("test@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(clearText(), typeText("123"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etPassword))
                    .check(matches(hasErrorText("Password must be at least 6 characters")));
        }
    }
}