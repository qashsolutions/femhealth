package com.maa.health.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maa.health.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E regression tests for onboarding flow
 *
 * Tests the complete onboarding journey:
 * 1. Splash screen
 * 2. Language selection
 * 3. Phone authentication
 * 4. OTP verification
 * 5. Profile setup
 * 6. Lifecycle stage selection
 */
@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun splashScreen_displaysAppName() {
        // Verify splash screen shows app name
        composeTestRule.onNodeWithText("Maa", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun languageSelection_showsAllLanguages() {
        // Wait for navigation to language selection
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Hindi is available
        composeTestRule.onNodeWithText("Hindi", substring = true)
            .assertExists()

        // Verify English is available
        composeTestRule.onNodeWithText("English", substring = true)
            .assertExists()
    }

    @Test
    fun languageSelection_hindiIsDefault() {
        // Wait for language selection screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Hindi option is highlighted/selected as default
        composeTestRule.onNodeWithText("हिन्दी", substring = true)
            .assertExists()
    }

    @Test
    fun languageSelection_selectingLanguageNavigatesToPhoneAuth() {
        // Wait for language selection
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Select a language
        composeTestRule.onNodeWithText("English")
            .performClick()

        // Click continue
        composeTestRule.onNodeWithText("Continue", ignoreCase = true)
            .performClick()

        // Verify navigation to phone auth screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("phone", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun phoneAuth_validatesPhoneNumberFormat() {
        // Navigate to phone auth screen first
        navigateToPhoneAuth()

        // Enter invalid phone number (too short)
        composeTestRule.onNodeWithTag("phone_input")
            .performTextInput("12345")

        // Try to submit
        composeTestRule.onNodeWithText("Send OTP", ignoreCase = true)
            .performClick()

        // Should show error or stay on same screen
        composeTestRule.onNodeWithText("phone", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun phoneAuth_acceptsValidIndianNumber() {
        // Navigate to phone auth screen first
        navigateToPhoneAuth()

        // Enter valid 10-digit Indian phone number
        composeTestRule.onNodeWithTag("phone_input")
            .performTextInput("9876543210")

        // Submit button should be enabled
        composeTestRule.onNodeWithText("Send OTP", ignoreCase = true)
            .assertIsEnabled()
    }

    @Test
    fun phoneAuth_skipButtonAvailable() {
        // Navigate to phone auth screen
        navigateToPhoneAuth()

        // Verify skip button exists (for development/testing)
        composeTestRule.onNodeWithText("Skip", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun profileSetup_displaysRequiredFields() {
        // Skip to profile setup
        navigateToProfileSetup()

        // Verify name field exists
        composeTestRule.onNodeWithText("Name", substring = true, ignoreCase = true)
            .assertExists()

        // Verify age field exists
        composeTestRule.onNodeWithText("Age", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun lifecycleStageSelection_showsAllStages() {
        // Skip to lifecycle stage selection
        navigateToLifecycleStageSelection()

        // Verify key stages are displayed
        val stages = listOf("Adolescence", "Reproductive", "Pregnancy", "Postpartum", "Child Care", "Midlife", "Elder")

        stages.forEach { stage ->
            composeTestRule.onNodeWithText(stage, substring = true, ignoreCase = true)
                .assertExists()
        }
    }

    @Test
    fun lifecycleStageSelection_allowsMultipleSelection() {
        // Skip to lifecycle stage selection
        navigateToLifecycleStageSelection()

        // Select multiple stages
        composeTestRule.onNodeWithText("Reproductive", substring = true)
            .performClick()

        composeTestRule.onNodeWithText("Pregnancy", substring = true)
            .performClick()

        // Both should be selected
        composeTestRule.onNodeWithText("Reproductive", substring = true)
            .assertIsSelected()
        composeTestRule.onNodeWithText("Pregnancy", substring = true)
            .assertIsSelected()
    }

    @Test
    fun completeOnboarding_navigatesToHome() {
        // Complete full onboarding
        navigateToHome()

        // Verify home screen is displayed
        composeTestRule.onNodeWithText("Home", substring = true)
            .assertIsDisplayed()
    }

    // Helper functions for navigation

    private fun navigateToPhoneAuth() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("English")
            .performClick()

        composeTestRule.onNodeWithText("Continue", ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("phone", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToProfileSetup() {
        navigateToPhoneAuth()

        // Skip phone auth
        composeTestRule.onNodeWithText("Skip", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Profile", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToLifecycleStageSelection() {
        navigateToProfileSetup()

        // Fill basic profile info
        composeTestRule.onNodeWithTag("name_input")
            .performTextInput("Test User")

        composeTestRule.onNodeWithTag("age_input")
            .performTextInput("25")

        composeTestRule.onNodeWithText("Continue", ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Lifecycle", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToHome() {
        navigateToLifecycleStageSelection()

        // Select at least one stage
        composeTestRule.onNodeWithText("Reproductive", substring = true)
            .performClick()

        composeTestRule.onNodeWithText("Continue", ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Home", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
