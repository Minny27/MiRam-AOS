package com.seungmin.miram.shared.model

import java.util.Calendar

fun Alarm.normalizedSpecificDateMillis(
    now: Calendar = Calendar.getInstance()
): Long? = normalizeSpecificDateMillis(
    selectedDateMillis = specificDateMillis,
    hour = hour,
    minute = minute,
    now = now
)

fun Alarm.withNormalizedSpecificDate(
    now: Calendar = Calendar.getInstance()
): Alarm = copy(specificDateMillis = normalizedSpecificDateMillis(now))

fun Alarm.nextTriggerAtMillis(
    now: Calendar = Calendar.getInstance()
): Long = when {
    specificDateMillis != null -> normalizedSpecificDateMillis(now) ?: Long.MAX_VALUE
    repeatWeekdays().isNotEmpty() -> repeatWeekdays()
        .map { weekday -> nextTriggerAtMillis(hour, minute, weekday.toCalendarDayOfWeek(), now) }
        .minOrNull()
        ?: Long.MAX_VALUE
    else -> nextTriggerAtMillis(hour, minute, now = now)
}

fun normalizeSpecificDateMillis(
    selectedDateMillis: Long?,
    hour: Int,
    minute: Int,
    now: Calendar = Calendar.getInstance()
): Long? {
    if (selectedDateMillis == null) return null
    val triggerAt = buildSpecificDateCalendar(selectedDateMillis, hour, minute)
    if (triggerAt.timeInMillis > now.timeInMillis) return triggerAt.timeInMillis

    while (triggerAt.timeInMillis <= now.timeInMillis) {
        triggerAt.add(Calendar.DAY_OF_YEAR, 1)
    }
    return triggerAt.timeInMillis
}

fun wasSpecificDateAdjusted(
    selectedDateMillis: Long?,
    hour: Int,
    minute: Int,
    now: Calendar = Calendar.getInstance()
): Boolean {
    if (selectedDateMillis == null) return false
    val requestedTriggerAt = buildSpecificDateCalendar(selectedDateMillis, hour, minute).timeInMillis
    return normalizeSpecificDateMillis(selectedDateMillis, hour, minute, now) != requestedTriggerAt
}

fun nextTriggerAtMillis(
    hour: Int,
    minute: Int,
    dayOfWeek: Int? = null,
    now: Calendar = Calendar.getInstance()
): Long = Calendar.getInstance().apply {
    timeInMillis = now.timeInMillis
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)

    if (dayOfWeek != null) {
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        if (timeInMillis <= now.timeInMillis) add(Calendar.WEEK_OF_YEAR, 1)
    } else if (timeInMillis <= now.timeInMillis) {
        add(Calendar.DAY_OF_YEAR, 1)
    }
}.timeInMillis

fun Weekday.toCalendarDayOfWeek(): Int = when (this) {
    Weekday.SUN -> Calendar.SUNDAY
    Weekday.MON -> Calendar.MONDAY
    Weekday.TUE -> Calendar.TUESDAY
    Weekday.WED -> Calendar.WEDNESDAY
    Weekday.THU -> Calendar.THURSDAY
    Weekday.FRI -> Calendar.FRIDAY
    Weekday.SAT -> Calendar.SATURDAY
}

private fun buildSpecificDateCalendar(
    selectedDateMillis: Long,
    hour: Int,
    minute: Int
): Calendar = Calendar.getInstance().apply {
    timeInMillis = selectedDateMillis
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
