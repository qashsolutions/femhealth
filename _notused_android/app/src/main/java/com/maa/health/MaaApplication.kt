package com.maa.health

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for Maa
 *
 * Initializes:
 * - Hilt dependency injection
 * - WorkManager for background sync
 * - Sound/haptic feedback system
 */
@HiltAndroidApp
class MaaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Additional initialization can be added here
        // e.g., Firebase, Analytics, Crash reporting
    }

    companion object {
        lateinit var instance: MaaApplication
            private set
    }
}
