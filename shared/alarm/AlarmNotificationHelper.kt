package com.example.miram.shared.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object AlarmNotificationHelper {
    const val CHANNEL_ID = "alarm_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "알람",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "알람 울림 알림"
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun buildAlarmNotification(
        context: Context,
        alarm: AlarmPayload
    ): Notification {
        val fullScreenIntent = alarm.fillIntent(Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        val fullScreenPi = PendingIntent.getActivity(
            context, alarm.alarmId.hashCode(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = alarm.fillIntent(Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_STOP
        })
        val stopPi = PendingIntent.getService(
            context, ("${alarm.alarmId}-stop").hashCode(), stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = alarm.fillIntent(Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_SNOOZE
        })
        val snoozePi = PendingIntent.getService(
            context, ("${alarm.alarmId}-snooze").hashCode(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label.ifBlank { "알람" })
            .setContentText(alarm.timeText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPi, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "해제", stopPi)
            .setOngoing(true)
            .setAutoCancel(false)
        if (alarm.snoozeEnabled && alarm.snoozeRepeatCount > 0) {
            builder.addAction(android.R.drawable.ic_popup_reminder, "다시 울림", snoozePi)
        }
        return builder.build()
    }
}
