package com.example.miram.shared.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
        val hour = intent.getIntExtra(EXTRA_ALARM_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_ALARM_MINUTE, 0)
        val ringDuration = intent.getIntExtra(EXTRA_RING_DURATION, 60)
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI) ?: ""
        val soundEnabled = intent.getBooleanExtra(EXTRA_SOUND_ENABLED, true)
        val vibrateEnabled = intent.getBooleanExtra(EXTRA_VIBRATE_ENABLED, true)
        val vibrationMode = intent.getStringExtra(EXTRA_VIBRATION_MODE) ?: "Basic call"
        val snoozeEnabled = intent.getBooleanExtra(EXTRA_SNOOZE_ENABLED, true)
        val snoozeInterval = intent.getIntExtra(EXTRA_SNOOZE_INTERVAL_MIN, 5)
        val snoozeRepeatCount = intent.getIntExtra(EXTRA_SNOOZE_REPEAT_COUNT, 3)

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_HOUR, hour)
            putExtra(EXTRA_ALARM_MINUTE, minute)
            putExtra(EXTRA_RING_DURATION, ringDuration)
            putExtra(EXTRA_SOUND_URI, soundUri)
            putExtra(EXTRA_SOUND_ENABLED, soundEnabled)
            putExtra(EXTRA_VIBRATE_ENABLED, vibrateEnabled)
            putExtra(EXTRA_VIBRATION_MODE, vibrationMode)
            putExtra(EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(EXTRA_SNOOZE_INTERVAL_MIN, snoozeInterval)
            putExtra(EXTRA_SNOOZE_REPEAT_COUNT, snoozeRepeatCount)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_HOUR = "alarm_hour"
        const val EXTRA_ALARM_MINUTE = "alarm_minute"
        const val EXTRA_RING_DURATION = "ring_duration"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_SOUND_ENABLED = "sound_enabled"
        const val EXTRA_VIBRATE_ENABLED = "vibrate_enabled"
        const val EXTRA_VIBRATION_MODE = "vibration_mode"
        const val EXTRA_SNOOZE_ENABLED = "snooze_enabled"
        const val EXTRA_SNOOZE_INTERVAL_MIN = "snooze_interval_min"
        const val EXTRA_SNOOZE_REPEAT_COUNT = "snooze_repeat_count"
    }
}
