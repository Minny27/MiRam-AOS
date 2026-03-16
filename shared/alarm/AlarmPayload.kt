package com.example.miram.shared.alarm

import android.content.Intent
import com.example.miram.shared.model.Alarm

data class AlarmPayload(
    val alarmId: String,
    val label: String,
    val hour: Int,
    val minute: Int,
    val ringDuration: Int,
    val soundUri: String,
    val soundEnabled: Boolean,
    val vibrateEnabled: Boolean,
    val vibrationMode: String,
    val snoozeEnabled: Boolean,
    val snoozeIntervalMinutes: Int,
    val snoozeRepeatCount: Int
) {
    val timeText: String
        get() = String.format("%02d:%02d", hour, minute)

    fun fillIntent(intent: Intent): Intent = intent.apply {
        putExtra(AlarmExtras.ALARM_ID, alarmId)
        putExtra(AlarmExtras.ALARM_LABEL, label)
        putExtra(AlarmExtras.ALARM_HOUR, hour)
        putExtra(AlarmExtras.ALARM_MINUTE, minute)
        putExtra(AlarmExtras.RING_DURATION, ringDuration)
        putExtra(AlarmExtras.SOUND_URI, soundUri)
        putExtra(AlarmExtras.SOUND_ENABLED, soundEnabled)
        putExtra(AlarmExtras.VIBRATE_ENABLED, vibrateEnabled)
        putExtra(AlarmExtras.VIBRATION_MODE, vibrationMode)
        putExtra(AlarmExtras.SNOOZE_ENABLED, snoozeEnabled)
        putExtra(AlarmExtras.SNOOZE_INTERVAL_MINUTES, snoozeIntervalMinutes)
        putExtra(AlarmExtras.SNOOZE_REPEAT_COUNT, snoozeRepeatCount)
    }

    companion object {
        fun fromAlarm(alarm: Alarm): AlarmPayload = AlarmPayload(
            alarmId = alarm.id,
            label = alarm.label,
            hour = alarm.hour,
            minute = alarm.minute,
            ringDuration = alarm.ringDuration,
            soundUri = alarm.soundUri,
            soundEnabled = alarm.soundEnabled,
            vibrateEnabled = alarm.vibrateEnabled,
            vibrationMode = alarm.vibrationMode,
            snoozeEnabled = alarm.snoozeEnabled,
            snoozeIntervalMinutes = alarm.snoozeIntervalMinutes,
            snoozeRepeatCount = alarm.snoozeRepeatCount
        )

        fun fromIntent(intent: Intent): AlarmPayload? {
            val alarmId = intent.getStringExtra(AlarmExtras.ALARM_ID)
                ?.takeIf(String::isNotBlank)
                ?: return null

            return AlarmPayload(
                alarmId = alarmId,
                label = intent.getStringExtra(AlarmExtras.ALARM_LABEL).orEmpty(),
                hour = intent.getIntExtra(AlarmExtras.ALARM_HOUR, 0),
                minute = intent.getIntExtra(AlarmExtras.ALARM_MINUTE, 0),
                ringDuration = intent.getIntExtra(AlarmExtras.RING_DURATION, 60),
                soundUri = intent.getStringExtra(AlarmExtras.SOUND_URI).orEmpty(),
                soundEnabled = intent.getBooleanExtra(AlarmExtras.SOUND_ENABLED, true),
                vibrateEnabled = intent.getBooleanExtra(AlarmExtras.VIBRATE_ENABLED, true),
                vibrationMode = intent.getStringExtra(AlarmExtras.VIBRATION_MODE) ?: "Basic call",
                snoozeEnabled = intent.getBooleanExtra(AlarmExtras.SNOOZE_ENABLED, true),
                snoozeIntervalMinutes = intent.getIntExtra(AlarmExtras.SNOOZE_INTERVAL_MINUTES, 5),
                snoozeRepeatCount = intent.getIntExtra(AlarmExtras.SNOOZE_REPEAT_COUNT, 3)
            )
        }
    }
}

object AlarmExtras {
    const val ALARM_ID = "alarm_id"
    const val ALARM_LABEL = "alarm_label"
    const val ALARM_HOUR = "alarm_hour"
    const val ALARM_MINUTE = "alarm_minute"
    const val RING_DURATION = "ring_duration"
    const val SOUND_URI = "sound_uri"
    const val SOUND_ENABLED = "sound_enabled"
    const val VIBRATE_ENABLED = "vibrate_enabled"
    const val VIBRATION_MODE = "vibration_mode"
    const val SNOOZE_ENABLED = "snooze_enabled"
    const val SNOOZE_INTERVAL_MINUTES = "snooze_interval_min"
    const val SNOOZE_REPEAT_COUNT = "snooze_repeat_count"
}
