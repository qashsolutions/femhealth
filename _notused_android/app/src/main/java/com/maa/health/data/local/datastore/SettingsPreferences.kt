package com.maa.health.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App settings stored locally using DataStore
 */
@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PERIOD_REMINDERS = booleanPreferencesKey("period_reminders")
        val MEDICATION_REMINDERS = booleanPreferencesKey("medication_reminders")
        val ANC_REMINDERS = booleanPreferencesKey("anc_reminders")
        val VACCINATION_REMINDERS = booleanPreferencesKey("vaccination_reminders")
        val MOOD_CHECK_REMINDERS = booleanPreferencesKey("mood_check_reminders")
        val KICK_COUNT_REMINDERS = booleanPreferencesKey("kick_count_reminders")
        val OFFLINE_MODE_ENABLED = booleanPreferencesKey("offline_mode_enabled")
        val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")
    }

    val settingsFlow: Flow<SettingsData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsData(
                soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
                hapticEnabled = preferences[Keys.HAPTIC_ENABLED] ?: true,
                notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true,
                periodReminders = preferences[Keys.PERIOD_REMINDERS] ?: true,
                medicationReminders = preferences[Keys.MEDICATION_REMINDERS] ?: true,
                ancReminders = preferences[Keys.ANC_REMINDERS] ?: true,
                vaccinationReminders = preferences[Keys.VACCINATION_REMINDERS] ?: true,
                moodCheckReminders = preferences[Keys.MOOD_CHECK_REMINDERS] ?: true,
                kickCountReminders = preferences[Keys.KICK_COUNT_REMINDERS] ?: true,
                offlineModeEnabled = preferences[Keys.OFFLINE_MODE_ENABLED] ?: false,
                dataSaverMode = preferences[Keys.DATA_SAVER_MODE] ?: false
            )
        }

    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    val hapticEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.HAPTIC_ENABLED] ?: true }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setPeriodReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.PERIOD_REMINDERS] = enabled }
    }

    suspend fun setMedicationReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.MEDICATION_REMINDERS] = enabled }
    }

    suspend fun setAncReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.ANC_REMINDERS] = enabled }
    }

    suspend fun setVaccinationReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.VACCINATION_REMINDERS] = enabled }
    }

    suspend fun setMoodCheckReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.MOOD_CHECK_REMINDERS] = enabled }
    }

    suspend fun setKickCountReminders(enabled: Boolean) {
        dataStore.edit { it[Keys.KICK_COUNT_REMINDERS] = enabled }
    }

    suspend fun setOfflineModeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.OFFLINE_MODE_ENABLED] = enabled }
    }

    suspend fun setDataSaverMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DATA_SAVER_MODE] = enabled }
    }
}

/**
 * Data class representing all settings
 */
data class SettingsData(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val periodReminders: Boolean = true,
    val medicationReminders: Boolean = true,
    val ancReminders: Boolean = true,
    val vaccinationReminders: Boolean = true,
    val moodCheckReminders: Boolean = true,
    val kickCountReminders: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val dataSaverMode: Boolean = false
)
