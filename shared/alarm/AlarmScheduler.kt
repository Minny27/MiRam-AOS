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
        when {
            alarm.specificDateMillis != null -> scheduleOnce(alarm)
            alarm.repeatWeekdays().isNotEmpty() -> alarm.repeatWeekdays().forEach { scheduleRepeating(alarm, it) }
            else -> scheduleOnce(alarm)
        }
    }

    fun cancel(alarm: Alarm) {
        buildPendingIntent(alarm, oneTimeRequestCode(alarm))?.let { alarmManager.cancel(it) }
        Weekday.entries.forEach {
            buildPendingIntent(alarm, repeatingRequestCode(alarm, it))?.let { pi -> alarmManager.cancel(pi) }
        }
    }

    private fun scheduleOnce(alarm: Alarm) {
        val triggerAt = alarm.specificDateMillis?.let { specified ->
            Calendar.getInstance().apply {
                timeInMillis = specified
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                while (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }.timeInMillis
        } ?: nextAlarmTimeMillis(alarm.hour, alarm.minute, null)
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
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(AlarmReceiver.EXTRA_RING_DURATION, alarm.ringDuration)
            putExtra(AlarmReceiver.EXTRA_SOUND_URI, alarm.soundUri)
            putExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, alarm.soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, alarm.vibrateEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATION_MODE, alarm.vibrationMode)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL_MIN, alarm.snoozeIntervalMinutes)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_REPEAT_COUNT, alarm.snoozeRepeatCount)
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

    fun scheduleSnooze(
        alarmId: String,
        label: String,
        hour: Int,
        minute: Int,
        ringDuration: Int,
        soundUri: String,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean,
        vibrationMode: String,
        snoozeEnabled: Boolean,
        snoozeIntervalMinutes: Int,
        snoozeRepeatCount: Int
    ) {
        val triggerAt = System.currentTimeMillis() + snoozeIntervalMinutes * 60 * 1000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmReceiver.EXTRA_RING_DURATION, ringDuration)
            putExtra(AlarmReceiver.EXTRA_SOUND_URI, soundUri)
            putExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, vibrateEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATION_MODE, vibrationMode)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL_MIN, snoozeIntervalMinutes)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_REPEAT_COUNT, snoozeRepeatCount)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            (alarmId + "_snooze").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }
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
