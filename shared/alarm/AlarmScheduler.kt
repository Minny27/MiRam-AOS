package com.example.miram.shared.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.miram.shared.model.Alarm
import com.example.miram.shared.model.Weekday
import com.example.miram.shared.model.nextTriggerAtMillis
import com.example.miram.shared.model.toCalendarDayOfWeek
import com.example.miram.shared.model.withNormalizedSpecificDate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        val normalizedAlarm = alarm.withNormalizedSpecificDate()
        cancel(normalizedAlarm)
        if (!normalizedAlarm.isEnabled) return
        when {
            normalizedAlarm.specificDateMillis != null -> scheduleOnce(normalizedAlarm)
            normalizedAlarm.repeatWeekdays().isNotEmpty() -> {
                normalizedAlarm.repeatWeekdays().forEach { scheduleRepeating(normalizedAlarm, it) }
            }
            else -> scheduleOnce(normalizedAlarm)
        }
    }

    fun cancel(alarm: Alarm) {
        val payload = AlarmPayload.fromAlarm(alarm)
        buildPendingIntent(payload, oneTimeRequestCode(alarm))?.let { alarmManager.cancel(it) }
        Weekday.entries.forEach {
            buildPendingIntent(payload, repeatingRequestCode(alarm, it))
                ?.let { pi -> alarmManager.cancel(pi) }
        }
    }

    private fun scheduleOnce(alarm: Alarm) {
        val triggerAt = alarm.nextTriggerAtMillis()
        val pi = buildPendingIntent(AlarmPayload.fromAlarm(alarm), oneTimeRequestCode(alarm)) ?: return
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    private fun scheduleRepeating(alarm: Alarm, weekday: Weekday) {
        val triggerAt = nextTriggerAtMillis(
            hour = alarm.hour,
            minute = alarm.minute,
            dayOfWeek = weekday.toCalendarDayOfWeek()
        )
        val pi = buildPendingIntent(AlarmPayload.fromAlarm(alarm), repeatingRequestCode(alarm, weekday))
            ?: return
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    private fun buildPendingIntent(payload: AlarmPayload, requestCode: Int): PendingIntent? {
        val intent = payload.fillIntent(Intent(context, AlarmReceiver::class.java))
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun oneTimeRequestCode(alarm: Alarm) = alarm.id.hashCode()
    private fun repeatingRequestCode(alarm: Alarm, weekday: Weekday) = (alarm.id + weekday.value).hashCode()

    fun scheduleSnooze(alarm: AlarmPayload) {
        val triggerAt = System.currentTimeMillis() + alarm.snoozeIntervalMinutes * 60 * 1000L
        val intent = alarm.fillIntent(Intent(context, AlarmReceiver::class.java))
        val pi = PendingIntent.getBroadcast(
            context,
            (alarm.alarmId + "_snooze").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }
}
