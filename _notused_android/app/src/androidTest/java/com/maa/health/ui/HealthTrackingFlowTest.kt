package com.maa.health.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maa.health.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E regression tests for health tracking flows
 *
 * Tests:
 * - Cycle tracking flow
 * - Mood logging flow
 * - Symptom triage flow
 * - Mental health screening flow
 */
@RunWith(AndroidJUnit4::class)
class HealthTrackingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ═══════════════════════════════════════════════════════════════════════════
    // CYCLE TRACKING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun cycleTracking_displaysCalendar() {
        skipOnboarding()

        // Navigate to cycle tracking
        composeTestRule.onNodeWithText("Cycle", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Cycle", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify calendar or date view is displayed
        composeTestRule.onNodeWithText("Calendar", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun cycleTracking_canLogPeriod() {
        skipOnboarding()

        // Navigate to cycle tracking
        composeTestRule.onNodeWithText("Cycle", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Log", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click log period button
        composeTestRule.onNodeWithText("Log", substring = true, ignoreCase = true)
            .performClick()

        // Should show period logging options
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Period", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("Flow", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun cycleTracking_showsPrediction() {
        skipOnboarding()

        // Navigate to cycle tracking
        composeTestRule.onNodeWithText("Cycle", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Cycle", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify prediction or next period info is shown
        composeTestRule.onNodeWithText("Next", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MENTAL HEALTH TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun mentalHealth_displaysMoodOptions() {
        skipOnboarding()

        // Navigate to mental health
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Mental", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify mood logging option exists
        composeTestRule.onNodeWithText("Mood", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun mentalHealth_canLogMood() {
        skipOnboarding()

        // Navigate to mental health
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Mood", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click on mood logging
        composeTestRule.onNodeWithText("Mood", substring = true, ignoreCase = true)
            .performClick()

        // Should show mood options (1-5 scale or similar)
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("How are you feeling", substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithContentDescription("mood")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun mentalHealth_showsScreeningOptions() {
        skipOnboarding()

        // Navigate to mental health
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Mental", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify screening options exist
        composeTestRule.onNodeWithText("Screening", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun mentalHealth_screeningsFreeForAll() {
        // Mental health screenings should be free (no premium gate)
        skipOnboarding()

        // Navigate to mental health
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Screening", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click on screening
        composeTestRule.onNodeWithText("Screening", substring = true, ignoreCase = true)
            .performClick()

        // Should NOT show premium paywall
        composeTestRule.onNodeWithText("Premium", substring = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Upgrade", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun mentalHealth_crisisSupportAccessible() {
        skipOnboarding()

        // Navigate to mental health
        composeTestRule.onNodeWithText("Mental", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Mental", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify crisis support is accessible
        composeTestRule.onNodeWithText("Crisis", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM TRIAGE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun symptomTriage_displaysBodyMap() {
        skipOnboarding()

        // Navigate to symptom checker
        composeTestRule.onNodeWithText("Symptom", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Body", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithContentDescription("body map")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun symptomTriage_canSelectBodyRegion() {
        skipOnboarding()

        // Navigate to symptom checker
        composeTestRule.onNodeWithText("Symptom", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithContentDescription("body region")
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("Head", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click on a body region
        try {
            composeTestRule.onNodeWithText("Head", substring = true)
                .performClick()
        } catch (e: Exception) {
            // Alternative: click on body map region
            composeTestRule.onNodeWithContentDescription("head region")
                .performClick()
        }

        // Should show symptom options for that region
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Symptom", substring = true)
                .fetchSemanticsNodes().size > 1
        }
    }

    @Test
    fun symptomTriage_hasEmergencyOption() {
        skipOnboarding()

        // Navigate to symptom checker
        composeTestRule.onNodeWithText("Symptom", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Emergency", substring = true)
                .fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule
                .onAllNodesWithContentDescription("emergency")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun symptomTriage_triageFreeForAll() {
        // Symptom triage should be free (no premium gate)
        skipOnboarding()

        // Navigate to symptom checker
        composeTestRule.onNodeWithText("Symptom", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Body", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Should NOT show premium paywall
        composeTestRule.onNodeWithText("Premium", substring = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Upgrade", substring = true)
            .assertDoesNotExist()
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
