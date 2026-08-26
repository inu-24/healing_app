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
public class RegisterActivityTest {

    @Test
    public void emptyFullName_showsRequiredError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etFullName))
                    .check(matches(hasErrorText("Full name is required")));
        }
    }

    @Test
    public void emptyEmail_showsRequiredError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName))
                    .perform(clearText(), typeText("Jane Doe"), closeSoftKeyboard());
            onView(withId(R.id.etEmail)).perform(clearText());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etEmail))
                    .check(matches(hasErrorText("Email is required")));
        }
    }

    @Test
    public void invalidEmailFormat_showsValidEmailError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName))
                    .perform(clearText(), typeText("Jane Doe"), closeSoftKeyboard());
            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("not-an-email"), closeSoftKeyboard());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etEmail))
                    .check(matches(hasErrorText("Enter a valid email")));
        }
    }

    @Test
    public void emptyPassword_showsRequiredError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName))
                    .perform(clearText(), typeText("Jane Doe"), closeSoftKeyboard());
            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("jane@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword)).perform(clearText());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etPassword))
                    .check(matches(hasErrorText("Password is required")));
        }
    }

    @Test
    public void shortPassword_showsLengthError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName))
                    .perform(clearText(), typeText("Jane Doe"), closeSoftKeyboard());
            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("jane@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(clearText(), typeText("123"), closeSoftKeyboard());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etPassword))
                    .check(matches(hasErrorText("Password must be at least 6 characters")));
        }
    }

    @Test
    public void mismatchedPasswords_showsMismatchError() {
        try (ActivityScenario<RegisterActivity> scenario =
                     ActivityScenario.launch(RegisterActivity.class)) {

            onView(withId(R.id.etFullName))
                    .perform(clearText(), typeText("Jane Doe"), closeSoftKeyboard());
            onView(withId(R.id.etEmail))
                    .perform(clearText(), typeText("jane@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(clearText(), typeText("password123"), closeSoftKeyboard());
            onView(withId(R.id.etConfirmPassword))
                    .perform(clearText(), typeText("differentPass"), closeSoftKeyboard());
            onView(withId(R.id.btnSignUp)).perform(click());

            onView(withId(R.id.etConfirmPassword))
                    .check(matches(hasErrorText("Passwords do not match")));
        }
    }
}