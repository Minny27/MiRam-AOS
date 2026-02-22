package com.example.miram.shared.data

import com.example.miram.shared.alarm.AlarmScheduler
import com.example.miram.shared.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> = dao.getAllAlarms()

    override suspend fun getAlarmById(id: String): Alarm? = dao.getAlarmById(id)

    override suspend fun addAlarm(alarm: Alarm) {
        dao.insertAlarm(alarm)
        if (alarm.isEnabled) scheduler.schedule(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        dao.updateAlarm(alarm)
        scheduler.cancel(alarm)
        if (alarm.isEnabled) scheduler.schedule(alarm)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        scheduler.cancel(alarm)
        dao.deleteAlarm(alarm)
    }

    override suspend fun setEnabled(alarm: Alarm, enabled: Boolean) {
        val updated = alarm.copy(isEnabled = enabled)
        dao.updateAlarm(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }
}
