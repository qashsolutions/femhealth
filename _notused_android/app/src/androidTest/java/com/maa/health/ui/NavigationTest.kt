package com.maa.health.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maa.health.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E regression tests for navigation
 *
 * Tests:
 * - Bottom navigation
 * - Screen transitions
 * - Back navigation
 * - Deep linking
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_allTabsAccessible() {
        // Skip onboarding first
        skipOnboarding()

        // Test Home tab
        composeTestRule.onNodeWithContentDescription("Home")
            .performClick()
        composeTestRule.onNodeWithText("Home", substring = true)
            .assertIsDisplayed()

        // Test Track tab
        composeTestRule.onNodeWithContentDescription("Track")
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Track", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Test Learn tab
        composeTestRule.onNodeWithContentDescription("Learn")
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Learn", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Test Care tab
        composeTestRule.onNodeWithContentDescription("Care")
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Care", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Test You/Profile tab
        composeTestRule.onNodeWithContentDescription("You")
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("You", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun homeScreen_navigatesToBodyMap() {
        skipOnboarding()

        // Click on symptom checker / body map button
        composeTestRule.onNodeWithText("Symptom", substring = true, ignoreCase = true)
            .performClick()

        // Verify body map screen is displayed
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Body", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun homeScreen_navigatesToCycleTracking() {
        skipOnboarding()

        // Click on cycle tracking button
        composeTestRule.onNodeWithText("Cycle", substring = true, ignoreCase = true)
            .performClick()

        // Verify cycle tracking screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Cycle", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun homeScreen_navigatesToMentalHealth() {
        skipOnboarding()

        // Click on mental health button
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        // Verify mental health screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Mental", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun backNavigation_returnsToPreviewScreen() {
        skipOnboarding()

        // Navigate to a detail screen
        composeTestRule.onNodeWithText("Cycle", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Cycle", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Press back
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        // Should return to home
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Home", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun emergencyButton_navigatesToEmergencyScreen() {
        skipOnboarding()

        // Find and click emergency button (if visible)
        composeTestRule.onNodeWithText("Emergency", substring = true, ignoreCase = true)
            .performClick()

        // Verify emergency screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Emergency", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun youHub_showsAllSettings() {
        skipOnboarding()

        // Navigate to You tab
        composeTestRule.onNodeWithContentDescription("You")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("You", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify key settings are visible
        composeTestRule.onNodeWithText("Language", substring = true)
            .assertExists()

        composeTestRule.onNodeWithText("Notifications", substring = true)
            .assertExists()

        composeTestRule.onNodeWithText("Privacy", substring = true)
            .assertExists()
    }

    @Test
    fun youHub_noUpgradeToPremiumButton() {
        // App is now free - should not show upgrade buttons
        skipOnboarding()

        // Navigate to You tab
        composeTestRule.onNodeWithContentDescription("You")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("You", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify no premium/upgrade button exists
        composeTestRule.onNodeWithText("Premium", substring = true)
            .assertDoesNotExist()

        composeTestRule.onNodeWithText("Upgrade", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun youHub_cloudSyncAccessibleWithoutPremium() {
        skipOnboarding()

        // Navigate to You tab
        composeTestRule.onNodeWithContentDescription("You")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("You", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Cloud Sync is accessible (not marked as premium)
        composeTestRule.onNodeWithText("Cloud Sync", substring = true)
            .assertExists()

        // Should be toggleable without premium
        composeTestRule.onNodeWithText("Cloud Sync", substring = true)
            .performClick()
    }

    // Helper function
    private fun skipOnboarding() {
        // Wait for app to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("Home", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // If on language selection, complete onboarding
        if (composeTestRule
                .onAllNodesWithText("Select Language", substring = true)
                .fetchSemanticsNodes().isNotEmpty()) {

            // Select English
            composeTestRule.onNodeWithText("English")
                .performClick()
            composeTestRule.onNodeWithText("Continue", ignoreCase = true)
                .performClick()

            // Skip phone auth
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule
                    .onAllNodesWithText("Skip", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Skip", substring = true)
                .performClick()

            // Fill profile
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule
                    .onAllNodesWithTag("name_input")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithTag("name_input")
                .performTextInput("Test User")
            composeTestRule.onNodeWithTag("age_input")
                .performTextInput("25")
            composeTestRule.onNodeWithText("Continue", ignoreCase = true)
                .performClick()

            // Select lifecycle stage
            composeTestRule.waitUntil(timeoutMillis = 3000) {
                composeTestRule
                    .onAllNodesWithText("Reproductive", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Reproductive", substring = true)
                .performClick()
            composeTestRule.onNodeWithText("Continue", ignoreCase = true)
                .performClick()

            // Wait for home
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule
                    .onAllNodesWithText("Home", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
