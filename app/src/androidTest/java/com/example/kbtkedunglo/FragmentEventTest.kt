package com.example.kbtkedunglo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentEventTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    @Test
    fun testMapViewZoomRotate() {
        // Melakukan zoom in pada MapView
        onView(withId(R.id.mapViewEvent))
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.BOTTOM_CENTER,
                    GeneralLocation.TOP_CENTER,
                    Press.FINGER
                )
            )

        // Melakukan zoom out pada MapView
        onView(withId(R.id.mapViewEvent))
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.TOP_CENTER,
                    GeneralLocation.BOTTOM_CENTER,
                    Press.FINGER
                )
            )

        // Melakukan rotasi pada MapView (memutar dua jari)
        onView(withId(R.id.mapViewEvent))
            .perform(
                GeneralSwipeAction(
                    Swipe.SLOW,
                    GeneralLocation.BOTTOM_CENTER,
                    GeneralLocation.BOTTOM_RIGHT,
                    Press.FINGER
                )
            )

        // Tambahkan pengujian lain sesuai kebutuhan
    }
}