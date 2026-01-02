package com.maa.health.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maa.health.data.local.datastore.UserPreferences
import com.maa.health.data.local.datastore.UserPreferencesData
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SupportedLanguage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UserPreferences
 */
class UserPreferencesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        userPreferences = UserPreferences(dataStore)
    }

    @Test
    fun `isPremium always returns true for free app`() = runTest {
        // Given - App is now free for all users

        // When
        val result = userPreferences.isPremium.first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `UserPreferencesData canUseVoice always returns true`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then - All features are free
        assertTrue(prefsData.canUseVoice)
    }

    @Test
    fun `UserPreferencesData canUseScreening always returns true`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then - All features are free
        assertTrue(prefsData.canUseScreening)
    }

    @Test
    fun `UserPreferencesData canUseTriage always returns true`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then - All features are free
        assertTrue(prefsData.canUseTriage)
    }

    @Test
    fun `default language is Hindi`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then
        assertEquals(SupportedLanguage.HINDI, prefsData.language)
    }

    @Test
    fun `default notifications are enabled`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then
        assertTrue(prefsData.notificationsEnabled)
    }

    @Test
    fun `default biometric is disabled`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then
        assertFalse(prefsData.biometricEnabled)
    }

    @Test
    fun `default onboarding is not complete`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then
        assertFalse(prefsData.isOnboardingComplete)
    }

    @Test
    fun `lifecycle stages are empty by default`() {
        // Given
        val prefsData = UserPreferencesData()

        // Then
        assertTrue(prefsData.lifecycleStages.isEmpty())
    }

    @Test
    fun `setLanguage updates datastore`() = runTest {
        // When
        userPreferences.setLanguage(SupportedLanguage.TAMIL)

        // Then
        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setLifecycleStages updates datastore`() = runTest {
        // Given
        val stages = setOf(LifecycleStage.REPRODUCTIVE, LifecycleStage.PREGNANCY)

        // When
        userPreferences.setLifecycleStages(stages)

        // Then
        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `clearAllPreferences clears datastore`() = runTest {
        // When
        userPreferences.clearAllPreferences()

        // Then
        coVerify { dataStore.edit(any()) }
    }
}
