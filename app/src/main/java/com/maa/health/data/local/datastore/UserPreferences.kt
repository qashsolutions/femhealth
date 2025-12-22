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
        val LIFECYCLE_STAGE = stringPreferencesKey("lifecycle_stage")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
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
            UserPreferencesData(
                isLoggedIn = preferences[Keys.IS_LOGGED_IN] ?: false,
                userId = preferences[Keys.USER_ID],
                phoneNumber = preferences[Keys.PHONE_NUMBER],
                languageCode = preferences[Keys.LANGUAGE_CODE] ?: SupportedLanguage.HINDI.code,
                lifecycleStage = preferences[Keys.LIFECYCLE_STAGE]?.let {
                    LifecycleStage.valueOf(it)
                },
                biometricEnabled = preferences[Keys.BIOMETRIC_ENABLED] ?: false,
                isPremium = preferences[Keys.IS_PREMIUM] ?: false,
                onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false,
                lastSyncTime = preferences[Keys.LAST_SYNC_TIME] ?: 0L,
                voiceMinutesUsed = preferences[Keys.VOICE_MINUTES_USED] ?: 0,
                freeScreeningsUsed = preferences[Keys.FREE_SCREENINGS_USED] ?: 0,
                freeTriagesUsed = preferences[Keys.FREE_TRIAGES_USED] ?: 0
            )
        }

    // Individual preference flows
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val languageCode: Flow<String> = dataStore.data.map { it[Keys.LANGUAGE_CODE] ?: SupportedLanguage.HINDI.code }
    val isPremium: Flow<Boolean> = dataStore.data.map { it[Keys.IS_PREMIUM] ?: false }
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

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_CODE] = languageCode
        }
    }

    suspend fun setLifecycleStage(stage: LifecycleStage) {
        dataStore.edit { preferences ->
            preferences[Keys.LIFECYCLE_STAGE] = stage.name
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setPremium(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_PREMIUM] = isPremium
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
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

    suspend fun clearAll() {
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
    val userId: String? = null,
    val phoneNumber: String? = null,
    val languageCode: String = SupportedLanguage.HINDI.code,
    val lifecycleStage: LifecycleStage? = null,
    val biometricEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val lastSyncTime: Long = 0L,
    val voiceMinutesUsed: Int = 0,
    val freeScreeningsUsed: Int = 0,
    val freeTriagesUsed: Int = 0
) {
    // Free tier limits
    companion object {
        const val FREE_VOICE_MINUTES_LIMIT = 5
        const val FREE_SCREENINGS_LIMIT = 1
        const val FREE_TRIAGES_LIMIT = 3
    }

    val canUseVoice: Boolean
        get() = isPremium || voiceMinutesUsed < FREE_VOICE_MINUTES_LIMIT

    val canUseScreening: Boolean
        get() = isPremium || freeScreeningsUsed < FREE_SCREENINGS_LIMIT

    val canUseTriage: Boolean
        get() = isPremium || freeTriagesUsed < FREE_TRIAGES_LIMIT
}
