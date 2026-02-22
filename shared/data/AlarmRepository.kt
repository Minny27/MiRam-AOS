package com.example.miram.shared.data

import com.example.miram.shared.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: String): Alarm?
    suspend fun addAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun setEnabled(alarm: Alarm, enabled: Boolean)
}
