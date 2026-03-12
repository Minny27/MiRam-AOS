package com.example.miram.shared.data

import com.example.miram.shared.alarm.AlarmScheduler
import com.example.miram.shared.model.Alarm
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> = dao.getAllAlarms()

    override suspend fun getAlarmById(id: String): Alarm? = dao.getAlarmById(id)

    override suspend fun removeDuplicatesOf(alarm: Alarm, excludeId: String?) {
        val normalized = alarm.normalizePastSpecificDate()
        dao.findDuplicateAlarms(
            hour = normalized.hour,
            minute = normalized.minute,
            repeatDays = normalized.repeatDays,
            specificDateMillis = normalized.specificDateMillis,
            excludeId = excludeId
        ).forEach { duplicate ->
            scheduler.cancel(duplicate)
            dao.deleteAlarm(duplicate)
        }
    }

    override suspend fun addAlarm(alarm: Alarm) {
        val normalized = alarm.normalizePastSpecificDate()
        dao.insertAlarm(normalized)
        if (normalized.isEnabled) scheduler.schedule(normalized)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val normalized = alarm.normalizePastSpecificDate()
        dao.updateAlarm(normalized)
        scheduler.cancel(normalized)
        if (normalized.isEnabled) scheduler.schedule(normalized)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        scheduler.cancel(alarm)
        dao.deleteAlarm(alarm)
    }

    override suspend fun setEnabled(alarm: Alarm, enabled: Boolean) {
        val updated = alarm.copy(isEnabled = enabled).normalizePastSpecificDate()
        dao.updateAlarm(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }
}

private fun Alarm.normalizePastSpecificDate(now: Calendar = Calendar.getInstance()): Alarm {
    val specific = specificDateMillis ?: return this
    val cal = Calendar.getInstance().apply {
        timeInMillis = specific
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (cal.timeInMillis > now.timeInMillis) return copy(specificDateMillis = cal.timeInMillis)
    while (cal.timeInMillis <= now.timeInMillis) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return copy(specificDateMillis = cal.timeInMillis)
}
