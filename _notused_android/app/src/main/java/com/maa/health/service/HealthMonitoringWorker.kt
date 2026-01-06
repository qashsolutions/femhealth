package com.maa.health.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maa.health.data.local.datastore.UserPreferencesDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for periodic health monitoring
 *
 * Runs in the background to:
 * - Analyze health data patterns
 * - Check for concerning trends
 * - Trigger notifications for detected issues
 */
@HiltWorker
class HealthMonitoringWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val healthMonitoringService: HealthMonitoringService,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if notifications are enabled
            val prefs = userPreferencesDataStore.getUserPreferences().first()
            if (!prefs.notificationsEnabled) {
                return Result.success()
            }

            // Get current user ID (from preferences or session)
            val userId = prefs.userId ?: return Result.success()

            // Process health data and send notifications
            healthMonitoringService.processAndNotify(userId)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
