package com.maa.health.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.maa.health.data.local.database.MaaDatabase
import com.maa.health.data.local.datastore.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Account Deletion Manager
 *
 * Handles complete account and data deletion as required by Google Play Store policy.
 *
 * Google Play Requirements (effective May 31, 2024):
 * 1. In-app account deletion option
 * 2. Web-based account deletion option
 * 3. Complete deletion of all associated data
 * 4. Clear disclosure of what data is deleted vs retained
 *
 * Data deleted:
 * - User profile (name, age, phone)
 * - Health records (symptoms, cycles, pregnancy data)
 * - Children profiles and vaccination records
 * - Mental health logs and screening results
 * - All preferences and settings
 * - Cached files and images
 *
 * Data retained (with user disclosure):
 * - Anonymous, aggregated analytics (non-identifiable)
 * - Deletion request logs (for legal compliance, 30 days)
 */
@Singleton
class AccountDeletionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MaaDatabase,
    private val userPreferences: UserPreferences
) {
    companion object {
        // Web URL for account deletion (required by Google Play)
        const val ACCOUNT_DELETION_WEB_URL = "https://maa.health/delete-account"

        // Retention period for deletion logs (legal compliance)
        const val DELETION_LOG_RETENTION_DAYS = 30
    }

    /**
     * Delete all user data from the device
     *
     * This performs a complete wipe of all user-related data:
     * 1. Clears all Room database tables
     * 2. Clears all DataStore preferences
     * 3. Clears app cache
     * 4. Clears any saved files
     *
     * @return Result indicating success or failure with error message
     */
    suspend fun deleteAllUserData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Clear all database tables
            clearDatabase()

            // Step 2: Clear all preferences
            clearPreferences()

            // Step 3: Clear cache directory
            clearCache()

            // Step 4: Clear files directory (user-generated content)
            clearFiles()

            // Step 5: Clear external files if any
            clearExternalFiles()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all database tables
     */
    private suspend fun clearDatabase() {
        database.clearAllTables()
    }

    /**
     * Clear all DataStore preferences
     */
    private suspend fun clearPreferences() {
        userPreferences.clearAllPreferences()
    }

    /**
     * Clear app cache directory
     */
    private fun clearCache() {
        context.cacheDir.deleteRecursively()
        context.externalCacheDir?.deleteRecursively()
    }

    /**
     * Clear files directory
     */
    private fun clearFiles() {
        context.filesDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    /**
     * Clear external files
     */
    private fun clearExternalFiles() {
        context.getExternalFilesDir(null)?.deleteRecursively()
    }

    /**
     * Get summary of data that will be deleted
     * Used for showing user what will be removed
     */
    suspend fun getDataDeletionSummary(): DataDeletionSummary = withContext(Dispatchers.IO) {
        DataDeletionSummary(
            hasUserProfile = userPreferences.isLoggedIn(),
            healthRecordsCount = getHealthRecordsCount(),
            childrenProfilesCount = getChildrenCount(),
            symptomLogsCount = getSymptomLogsCount(),
            cycleLogsCount = getCycleLogsCount(),
            mentalHealthLogsCount = getMentalHealthLogsCount(),
            cachedDataSizeBytes = getCacheSize()
        )
    }

    private suspend fun getHealthRecordsCount(): Int {
        // Count all health-related records
        return try {
            database.symptomDao().getSymptomLogCount() +
            database.cycleDao().getCycleLogCount()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getChildrenCount(): Int {
        return try {
            database.childDao().getChildCount()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getSymptomLogsCount(): Int {
        return try {
            database.symptomDao().getSymptomLogCount()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getCycleLogsCount(): Int {
        return try {
            database.cycleDao().getCycleLogCount()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getMentalHealthLogsCount(): Int {
        return try {
            database.moodDao().getMoodLogCount()
        } catch (e: Exception) {
            0
        }
    }

    private fun getCacheSize(): Long {
        val cacheSize = context.cacheDir.walkTopDown().sumOf { it.length() }
        val externalCacheSize = context.externalCacheDir?.walkTopDown()?.sumOf { it.length() } ?: 0
        return cacheSize + externalCacheSize
    }
}

/**
 * Summary of data that will be deleted
 */
data class DataDeletionSummary(
    val hasUserProfile: Boolean,
    val healthRecordsCount: Int,
    val childrenProfilesCount: Int,
    val symptomLogsCount: Int,
    val cycleLogsCount: Int,
    val mentalHealthLogsCount: Int,
    val cachedDataSizeBytes: Long
) {
    val totalRecordsCount: Int
        get() = healthRecordsCount + symptomLogsCount + cycleLogsCount + mentalHealthLogsCount

    val cachedDataSizeMB: String
        get() = String.format("%.2f MB", cachedDataSizeBytes / (1024.0 * 1024.0))

    val hasAnyData: Boolean
        get() = hasUserProfile || totalRecordsCount > 0 || childrenProfilesCount > 0
}
