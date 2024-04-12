package com.android.PetPamper

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.PetPamper.screen.MainScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun test() = run {
    step("Start Login Screen") {
      ComposeScreen.onComposeScreen<MainScreen>(composeTestRule) {
        loginTitle {
          assertIsDisplayed()
          assertTextEquals("Welcome,")
        }
        loginButton {
          performScrollTo()
          assertIsDisplayed()
          assertHasClickAction()
        }
        // Add a new check for the error message
        errorMessage {
          assertIsNotDisplayed()
        }
      }
    }
    step("Attempt to log in with empty credentials") {
      ComposeScreen.onComposeScreen<MainScreen>(composeTestRule) {
        loginButton {
          click()
        }
        // Check if the error message is displayed
        errorMessage {
          assertIsDisplayed()
          assertTextEquals("Login failed, email or password is incorrect")
        }
      }
    }
  }
}
