package com.maa.health.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Broadcast receiver for scheduled notifications
 *
 * Triggered by AlarmManager to show local notifications
 */
@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Maa"
        val body = intent.getStringExtra("body") ?: ""
        val channelId = intent.getStringExtra("channelId")
            ?: MaaFirebaseMessagingService.CHANNEL_GENERAL

        notificationHelper.showNotification(
            title = title,
            body = body,
            channelId = channelId
        )
    }
}
