package com.example.miram.shared.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.miram.shared.model.Alarm
import com.example.miram.shared.model.Weekday
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        cancel(alarm)
        if (!alarm.isEnabled) return
        if (alarm.isOneTime) scheduleOnce(alarm)
        else alarm.repeatWeekdays().forEach { scheduleRepeating(alarm, it) }
    }

    fun cancel(alarm: Alarm) {
        buildPendingIntent(alarm, oneTimeRequestCode(alarm))?.let { alarmManager.cancel(it) }
        Weekday.entries.forEach {
            buildPendingIntent(alarm, repeatingRequestCode(alarm, it))?.let { pi -> alarmManager.cancel(pi) }
        }
    }

    private fun scheduleOnce(alarm: Alarm) {
        val triggerAt = nextAlarmTimeMillis(alarm.hour, alarm.minute, null)
        val pi = buildPendingIntent(alarm, oneTimeRequestCode(alarm)) ?: return
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    private fun scheduleRepeating(alarm: Alarm, weekday: Weekday) {
        val triggerAt = nextAlarmTimeMillis(alarm.hour, alarm.minute, weekday.calendarDayOfWeek())
        val pi = buildPendingIntent(alarm, repeatingRequestCode(alarm, weekday)) ?: return
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    private fun buildPendingIntent(alarm: Alarm, requestCode: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_RING_DURATION, alarm.ringDuration)
            putExtra(AlarmReceiver.EXTRA_SOUND_URI, alarm.soundUri)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextAlarmTimeMillis(hour: Int, minute: Int, dayOfWeek: Int?): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (dayOfWeek != null) {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
            } else {
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    private fun oneTimeRequestCode(alarm: Alarm) = alarm.id.hashCode()
    private fun repeatingRequestCode(alarm: Alarm, weekday: Weekday) = (alarm.id + weekday.value).hashCode()
}

private fun Weekday.calendarDayOfWeek(): Int = when (this) {
    Weekday.SUN -> Calendar.SUNDAY
    Weekday.MON -> Calendar.MONDAY
    Weekday.TUE -> Calendar.TUESDAY
    Weekday.WED -> Calendar.WEDNESDAY
    Weekday.THU -> Calendar.THURSDAY
    Weekday.FRI -> Calendar.FRIDAY
    Weekday.SAT -> Calendar.SATURDAY
}
