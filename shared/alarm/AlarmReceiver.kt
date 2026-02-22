package com.example.miram.shared.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: ""
        val ringDuration = intent.getIntExtra(EXTRA_RING_DURATION, 60)
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI) ?: ""

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_RING_DURATION, ringDuration)
            putExtra(EXTRA_SOUND_URI, soundUri)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_RING_DURATION = "ring_duration"
        const val EXTRA_SOUND_URI = "sound_uri"
    }
}
