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
        label: String,
        alarmId: String,
        timeText: String,
        hour: Int,
        minute: Int,
        ringDuration: Int,
        soundUri: String,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean,
        vibrationMode: String,
        snoozeIntervalMinutes: Int,
        snoozeRepeatCount: Int,
        snoozeEnabled: Boolean
    ): Notification {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context, alarmId.hashCode(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_STOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val stopPi = PendingIntent.getService(
            context, ("$alarmId-stop").hashCode(), stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_SNOOZE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmReceiver.EXTRA_RING_DURATION, ringDuration)
            putExtra(AlarmReceiver.EXTRA_SOUND_URI, soundUri)
            putExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, vibrateEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATION_MODE, vibrationMode)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL_MIN, snoozeIntervalMinutes)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_REPEAT_COUNT, snoozeRepeatCount)
        }
        val snoozePi = PendingIntent.getService(
            context, ("$alarmId-snooze").hashCode(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label.ifBlank { "알람" })
            .setContentText(timeText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPi, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "해제", stopPi)
            .setOngoing(true)
            .setAutoCancel(false)
        if (snoozeEnabled && snoozeRepeatCount > 0) {
            builder.addAction(android.R.drawable.ic_popup_reminder, "다시 울림", snoozePi)
        }
        return builder.build()
    }
}
