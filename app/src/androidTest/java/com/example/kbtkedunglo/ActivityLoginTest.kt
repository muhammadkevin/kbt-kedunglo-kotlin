package com.example.kbtkedunglo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityLoginTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLogin() {
        // Isi formulir login
        onView(withId(R.id.editTextUsernameLogin)).perform(ViewActions.typeText("putri"))
        onView(withId(R.id.editTextPasswordLogin)).perform(ViewActions.typeText("putri"))

        // Klik tombol login
        onView(withId(R.id.btnSubmitLogin)).perform(ViewActions.click())
    }
}