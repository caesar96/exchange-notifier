package com.example.exchangenotifier.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.exchangenotifier.MainActivity
import com.example.exchangenotifier.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID     = "exchange_rate_alerts"
        private const val ID_ABOVE = 1001
        private const val ID_BELOW = 1002
    }

    private val notificationManager: NotificationManager
        get() = context.getSystemService(NotificationManager::class.java)

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.notification_channel_desc) }
        notificationManager.createNotificationChannel(channel)
    }

    fun notifyAboveUpper(rate: Double, threshold: Double) = post(
        id    = ID_ABOVE,
        title = context.getString(R.string.notification_above_title),
        body  = context.getString(
            R.string.notification_above_body,
            "%.4f".format(rate),
            "%.4f".format(threshold),
        )
    )

    fun notifyBelowLower(rate: Double, threshold: Double) = post(
        id    = ID_BELOW,
        title = context.getString(R.string.notification_below_title),
        body  = context.getString(
            R.string.notification_below_body,
            "%.4f".format(rate),
            "%.4f".format(threshold),
        )
    )

    private fun post(id: Int, title: String, body: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, id, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
