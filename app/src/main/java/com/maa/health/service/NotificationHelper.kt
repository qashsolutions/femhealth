package com.maa.health.service

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.maa.health.MainActivity
import com.maa.health.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing local notifications
 *
 * Features:
 * - Medication reminders
 * - Vaccination reminders
 * - Appointment reminders
 * - Cycle predictions
 * - Health issue alerts
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        // Request codes for pending intents
        private const val REQUEST_MEDICATION = 1000
        private const val REQUEST_VACCINATION = 2000
        private const val REQUEST_APPOINTMENT = 3000
        private const val REQUEST_CYCLE = 4000
        private const val REQUEST_HEALTH_CHECK = 5000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Ensure all channels exist
            val channels = listOf(
                NotificationChannel(
                    MaaFirebaseMessagingService.CHANNEL_HEALTH_ALERTS,
                    "Health Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Important health alerts and danger sign warnings"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    MaaFirebaseMessagingService.CHANNEL_REMINDERS,
                    "Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Medication, vaccination, and appointment reminders"
                },
                NotificationChannel(
                    MaaFirebaseMessagingService.CHANNEL_AI_INSIGHTS,
                    "AI Health Insights",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Personalized health insights and predictions"
                },
                NotificationChannel(
                    MaaFirebaseMessagingService.CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                }
            )

            manager.createNotificationChannels(channels)
        }
    }

    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Show immediate notification
     */
    fun showNotification(
        title: String,
        body: String,
        channelId: String = MaaFirebaseMessagingService.CHANNEL_GENERAL,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        notificationId: Int = MaaFirebaseMessagingService.getNextNotificationId()
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Show medication reminder notification
     */
    fun showMedicationReminder(
        medicationName: String,
        dosage: String,
        notificationId: Int
    ) {
        showNotification(
            title = "💊 Time for your medication",
            body = "Take $dosage of $medicationName",
            channelId = MaaFirebaseMessagingService.CHANNEL_REMINDERS,
            priority = NotificationCompat.PRIORITY_HIGH,
            notificationId = notificationId
        )
    }

    /**
     * Show vaccination reminder notification
     */
    fun showVaccinationReminder(
        childName: String?,
        vaccineName: String,
        dueDate: String,
        notificationId: Int
    ) {
        val title = if (childName != null) {
            "💉 Vaccination due for $childName"
        } else {
            "💉 Vaccination reminder"
        }

        showNotification(
            title = title,
            body = "$vaccineName is due on $dueDate. Schedule your visit today.",
            channelId = MaaFirebaseMessagingService.CHANNEL_REMINDERS,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            notificationId = notificationId
        )
    }

    /**
     * Show cycle prediction notification
     */
    fun showCyclePrediction(
        predictedDate: String,
        daysUntil: Int,
        notificationId: Int
    ) {
        val body = if (daysUntil == 0) {
            "Your period is predicted to start today. Track any symptoms you experience."
        } else {
            "Your period is predicted to start in $daysUntil days ($predictedDate)."
        }

        showNotification(
            title = "🔴 Cycle Prediction",
            body = body,
            channelId = MaaFirebaseMessagingService.CHANNEL_AI_INSIGHTS,
            priority = NotificationCompat.PRIORITY_LOW,
            notificationId = notificationId
        )
    }

    /**
     * Show health alert for danger signs
     */
    fun showDangerSignAlert(
        symptom: String,
        urgency: String,
        notificationId: Int
    ) {
        showNotification(
            title = "⚠️ Health Alert - $urgency",
            body = "You logged $symptom. This may require immediate attention. Please check the app for guidance.",
            channelId = MaaFirebaseMessagingService.CHANNEL_HEALTH_ALERTS,
            priority = NotificationCompat.PRIORITY_HIGH,
            notificationId = notificationId
        )
    }

    /**
     * Show AI health insight notification
     */
    fun showHealthInsight(
        title: String,
        insight: String,
        notificationId: Int
    ) {
        showNotification(
            title = "💡 $title",
            body = insight,
            channelId = MaaFirebaseMessagingService.CHANNEL_AI_INSIGHTS,
            priority = NotificationCompat.PRIORITY_LOW,
            notificationId = notificationId
        )
    }

    /**
     * Show screening reminder
     */
    fun showScreeningReminder(
        screeningType: String,
        reason: String,
        notificationId: Int
    ) {
        showNotification(
            title = "📋 Mental Health Check-in",
            body = "It's time for your $screeningType assessment. $reason",
            channelId = MaaFirebaseMessagingService.CHANNEL_REMINDERS,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            notificationId = notificationId
        )
    }

    /**
     * Schedule a notification at a specific time
     */
    fun scheduleNotification(
        title: String,
        body: String,
        triggerTime: LocalDateTime,
        channelId: String,
        requestCode: Int
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("channelId", channelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMillis = triggerTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Exact alarm permission not granted
        }
    }

    /**
     * Cancel a scheduled notification
     */
    fun cancelScheduledNotification(requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
