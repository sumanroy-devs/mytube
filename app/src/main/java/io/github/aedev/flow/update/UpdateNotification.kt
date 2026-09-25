package io.github.aedev.flow.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.aedev.flow.MainActivity
import io.github.aedev.flow.R

/**
 * Posts the "update available" notification built by [UpdateCheckWorker].
 * Tapping it opens [MainActivity], which reacts to [EXTRA_UPDATE_VERSION]
 * by running an update check so the update dialog appears.
 */
object UpdateNotification {
    // Kept from MyTube's previous updater so existing channel settings carry over.
    const val CHANNEL_ID = "updates_channel"
    const val EXTRA_UPDATE_VERSION = "EXTRA_UPDATE_VERSION"
    private const val NOTIFICATION_ID = 7000

    fun createChannel(context: Context) {
        if (!UpdateManager.isUpdateActive) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_updates_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.notification_channel_updates_description)
                    setShowBadge(true)
                }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun show(
        context: Context,
        release: Release,
    ) {
        if (!UpdateManager.isUpdateActive) {
            return
        }
        createChannel(context)
        // Silently skip when notifications are disabled or permission denied
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_UPDATE_VERSION, release.tagName)
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(
                    context.getString(
                        R.string.notification_update_available,
                        release.tagName.removePrefix("v"),
                    ),
                ).setContentText(context.getString(R.string.notification_tap_to_update))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
