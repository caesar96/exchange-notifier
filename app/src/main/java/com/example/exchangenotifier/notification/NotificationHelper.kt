package com.example.exchangenotifier.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
        const val CHANNEL_ID       = "exchange_rate_alerts"
        private const val ID_ABOVE = 1001
        private const val ID_BELOW = 1002
        private const val ID_TEST  = 1003
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

    fun notifyAboveUpper(pairLabel: String, rate: Double, threshold: Double) = post(
        id       = ID_ABOVE,
        iconRes  = R.drawable.ic_notification_up,
        title    = context.getString(R.string.notification_above_title, pairLabel),
        body     = context.getString(
            R.string.notification_above_body,
            "%.4f".format(rate),
            "%.4f".format(threshold),
        )
    )

    fun notifyBelowLower(pairLabel: String, rate: Double, threshold: Double) = post(
        id       = ID_BELOW,
        iconRes  = R.drawable.ic_notification_down,
        title    = context.getString(R.string.notification_below_title, pairLabel),
        body     = context.getString(
            R.string.notification_below_body,
            "%.4f".format(rate),
            "%.4f".format(threshold),
        )
    )

    fun sendTestNotification() = post(
        id      = ID_TEST,
        iconRes = R.drawable.ic_notification_up,
        title   = context.getString(R.string.notification_test_title),
        body    = context.getString(R.string.notification_test_body),
    )

    private fun post(id: Int, iconRes: Int, title: String, body: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, id, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
