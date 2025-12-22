package com.maa.health.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maa.health.MainActivity
import com.maa.health.R
import com.maa.health.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service for Maa
 *
 * Handles:
 * - Push notifications from server
 * - Token refresh for device registration
 * - Health alerts and reminders
 * - AI-powered health issue notifications
 */
@AndroidEntryPoint
class MaaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        // Notification channels
        const val CHANNEL_HEALTH_ALERTS = "health_alerts"
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_AI_INSIGHTS = "ai_insights"
        const val CHANNEL_GENERAL = "general"

        // Notification types
        const val TYPE_DANGER_SIGN = "danger_sign"
        const val TYPE_MEDICATION_REMINDER = "medication_reminder"
        const val TYPE_VACCINATION_DUE = "vaccination_due"
        const val TYPE_APPOINTMENT_REMINDER = "appointment_reminder"
        const val TYPE_CYCLE_PREDICTION = "cycle_prediction"
        const val TYPE_AI_HEALTH_INSIGHT = "ai_health_insight"
        const val TYPE_SCREENING_REMINDER = "screening_reminder"

        private var notificationId = 0
        fun getNextNotificationId(): Int = notificationId++
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        serviceScope.launch {
            // Check if notifications are enabled
            val prefs = userPreferencesDataStore.getUserPreferences().first()
            if (!prefs.notificationsEnabled) {
                return@launch
            }

            // Handle data payload
            remoteMessage.data.isNotEmpty().let {
                handleDataMessage(remoteMessage.data)
            }

            // Handle notification payload
            remoteMessage.notification?.let {
                showNotification(
                    title = it.title ?: "Maa",
                    body = it.body ?: "",
                    channelId = CHANNEL_GENERAL
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server for push notification targeting
        serviceScope.launch {
            sendTokenToServer(token)
        }
    }

    private suspend fun sendTokenToServer(token: String) {
        // TODO: Implement API call to register FCM token with backend
        // This will be used to send targeted push notifications
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        val title = data["title"] ?: "Maa"
        val body = data["body"] ?: ""
        val priority = data["priority"] ?: "normal"

        val (channelId, importance) = when (type) {
            TYPE_DANGER_SIGN -> CHANNEL_HEALTH_ALERTS to NotificationCompat.PRIORITY_HIGH
            TYPE_MEDICATION_REMINDER -> CHANNEL_REMINDERS to NotificationCompat.PRIORITY_DEFAULT
            TYPE_VACCINATION_DUE -> CHANNEL_REMINDERS to NotificationCompat.PRIORITY_DEFAULT
            TYPE_APPOINTMENT_REMINDER -> CHANNEL_REMINDERS to NotificationCompat.PRIORITY_DEFAULT
            TYPE_CYCLE_PREDICTION -> CHANNEL_AI_INSIGHTS to NotificationCompat.PRIORITY_LOW
            TYPE_AI_HEALTH_INSIGHT -> CHANNEL_AI_INSIGHTS to NotificationCompat.PRIORITY_DEFAULT
            TYPE_SCREENING_REMINDER -> CHANNEL_REMINDERS to NotificationCompat.PRIORITY_DEFAULT
            else -> CHANNEL_GENERAL to NotificationCompat.PRIORITY_DEFAULT
        }

        showNotification(
            title = title,
            body = body,
            channelId = channelId,
            priority = importance,
            data = data
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        data: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pass data for deep linking
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            getNextNotificationId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        try {
            NotificationManagerCompat.from(this).notify(getNextNotificationId(), notification)
        } catch (e: SecurityException) {
            // Handle missing notification permission
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Health Alerts Channel (High importance)
            val healthAlertsChannel = NotificationChannel(
                CHANNEL_HEALTH_ALERTS,
                "Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important health alerts and danger sign warnings"
                enableVibration(true)
                enableLights(true)
            }

            // Reminders Channel (Default importance)
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Medication, vaccination, and appointment reminders"
            }

            // AI Insights Channel (Low importance)
            val aiInsightsChannel = NotificationChannel(
                CHANNEL_AI_INSIGHTS,
                "AI Health Insights",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Personalized health insights and predictions"
            }

            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(healthAlertsChannel, remindersChannel, aiInsightsChannel, generalChannel)
            )
        }
    }
}
