package com.maa.health.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User preferences stored locally using DataStore
 */
@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // Keys for preferences
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val LIFECYCLE_STAGES = stringPreferencesKey("lifecycle_stages")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val OFFLINE_CONTENT_ENABLED = booleanPreferencesKey("offline_content_enabled")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val VOICE_MINUTES_USED = intPreferencesKey("voice_minutes_used")
        val FREE_SCREENINGS_USED = intPreferencesKey("free_screenings_used")
        val FREE_TRIAGES_USED = intPreferencesKey("free_triages_used")
    }

    // Flow of user preferences
    val userPreferencesFlow: Flow<UserPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val languageCode = preferences[Keys.LANGUAGE_CODE] ?: SupportedLanguage.HINDI.code
            val language = SupportedLanguage.entries.find { it.code == languageCode }
                ?: SupportedLanguage.HINDI

            val stagesString = preferences[Keys.LIFECYCLE_STAGES] ?: ""
            val lifecycleStages = if (stagesString.isBlank()) {
                emptySet()
            } else {
                stagesString.split(",").mapNotNull { stageName ->
                    try {
                        LifecycleStage.valueOf(stageName.trim())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }.toSet()
            }

            UserPreferencesData(
                isLoggedIn = preferences[Keys.IS_LOGGED_IN] ?: false,
                oderId = preferences[Keys.USER_ID] ?: "",
                phoneNumber = preferences[Keys.PHONE_NUMBER],
                language = language,
                lifecycleStages = lifecycleStages,
                biometricEnabled = preferences[Keys.BIOMETRIC_ENABLED] ?: false,
                pinEnabled = preferences[Keys.PIN_ENABLED] ?: false,
                notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true,
                offlineContentEnabled = preferences[Keys.OFFLINE_CONTENT_ENABLED] ?: false,
                isPremium = preferences[Keys.IS_PREMIUM] ?: false,
                isOnboardingComplete = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
                lastSyncTime = preferences[Keys.LAST_SYNC_TIME] ?: 0L,
                voiceMinutesUsed = preferences[Keys.VOICE_MINUTES_USED] ?: 0,
                freeScreeningsUsed = preferences[Keys.FREE_SCREENINGS_USED] ?: 0,
                freeTriagesUsed = preferences[Keys.FREE_TRIAGES_USED] ?: 0
            )
        }

    // Individual preference flows
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val languageCode: Flow<String> = dataStore.data.map { it[Keys.LANGUAGE_CODE] ?: SupportedLanguage.HINDI.code }
    val isPremium: Flow<Boolean> = dataStore.data.map { true }  // All features are now free
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }

    // Update functions
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
        }
    }

    suspend fun setPhoneNumber(phoneNumber: String) {
        dataStore.edit { preferences ->
            preferences[Keys.PHONE_NUMBER] = phoneNumber
        }
    }

    suspend fun setLanguage(language: SupportedLanguage) {
        dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_CODE] = language.code
        }
    }

    suspend fun setLifecycleStages(stages: Set<LifecycleStage>) {
        dataStore.edit { preferences ->
            preferences[Keys.LIFECYCLE_STAGES] = stages.joinToString(",") { it.name }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PIN_ENABLED] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setOfflineContentEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.OFFLINE_CONTENT_ENABLED] = enabled
        }
    }

    suspend fun setPremium(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_PREMIUM] = isPremium
        }
    }

    suspend fun setOnboardingComplete(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun incrementVoiceMinutes() {
        dataStore.edit { preferences ->
            val current = preferences[Keys.VOICE_MINUTES_USED] ?: 0
            preferences[Keys.VOICE_MINUTES_USED] = current + 1
        }
    }

    suspend fun incrementFreeScreenings() {
        dataStore.edit { preferences ->
            val current = preferences[Keys.FREE_SCREENINGS_USED] ?: 0
            preferences[Keys.FREE_SCREENINGS_USED] = current + 1
        }
    }

    suspend fun incrementFreeTriages() {
        dataStore.edit { preferences ->
            val current = preferences[Keys.FREE_TRIAGES_USED] ?: 0
            preferences[Keys.FREE_TRIAGES_USED] = current + 1
        }
    }

    suspend fun resetMonthlyCounters() {
        dataStore.edit { preferences ->
            preferences[Keys.VOICE_MINUTES_USED] = 0
            preferences[Keys.FREE_SCREENINGS_USED] = 0
            preferences[Keys.FREE_TRIAGES_USED] = 0
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * Data class representing all user preferences
 */
data class UserPreferencesData(
    val isLoggedIn: Boolean = false,
    val oderId: String = "",
    val phoneNumber: String? = null,
    val language: SupportedLanguage = SupportedLanguage.HINDI,
    val lifecycleStages: Set<LifecycleStage> = emptySet(),
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val offlineContentEnabled: Boolean = false,
    val isPremium: Boolean = true,  // All features are now free for everyone
    val isOnboardingComplete: Boolean = false,
    val lastSyncTime: Long = 0L,
    val voiceMinutesUsed: Int = 0,
    val freeScreeningsUsed: Int = 0,
    val freeTriagesUsed: Int = 0
) {
    // All features are now free - no limits
    val canUseVoice: Boolean = true
    val canUseScreening: Boolean = true
    val canUseTriage: Boolean = true
}
