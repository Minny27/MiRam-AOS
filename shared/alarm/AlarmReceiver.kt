package com.example.miram.shared.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarm = AlarmPayload.fromIntent(intent) ?: return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "${context.packageName}:alarm-delivery"
        )?.acquire(30_000L)
        val serviceIntent = alarm.fillIntent(Intent(context, AlarmForegroundService::class.java))
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
