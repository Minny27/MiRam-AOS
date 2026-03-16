package com.example.miram.shared.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarm = AlarmPayload.fromIntent(intent) ?: return
        val serviceIntent = alarm.fillIntent(Intent(context, AlarmForegroundService::class.java))
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
