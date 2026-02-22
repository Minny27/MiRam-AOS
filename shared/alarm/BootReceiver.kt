package com.example.miram.shared.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.miram.shared.data.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val alarms = repository.getAllAlarms().first()
            alarms.filter { it.isEnabled }.forEach { scheduler.schedule(it) }
        }
    }
}
