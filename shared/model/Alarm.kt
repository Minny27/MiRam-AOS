package com.example.miram.shared.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    /** Weekday values 콤마 구분 문자열 (예: "1,2,3"), 빈 문자열 = 1회성 */
    val repeatDays: String = "",
    val label: String = "",
    val isEnabled: Boolean = true,
    /** 초 단위 (5~3600) */
    val ringDuration: Int = 60,
    /** 시스템 린톤 URI 문자열. 빈 문자열 = 시스템 기본 알람 */
    val soundUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun repeatWeekdays(): List<Weekday> =
        if (repeatDays.isBlank()) emptyList()
        else repeatDays.split(",").mapNotNull { Weekday.fromValue(it.trim().toIntOrNull() ?: return@mapNotNull null) }

    val isOneTime: Boolean get() = repeatDays.isBlank()

    /** 24시간 포맷 (예: "08:30") */
    val timeString: String get() = "%02d:%02d".format(hour, minute)

    /** 12시간 포맷 (예: "08:30") */
    val twelveHourTimeString: String get() {
        val h = if (hour % 12 == 0) 12 else hour % 12
        return "%02d:%02d".format(h, minute)
    }

    /** AM / PM */
    val amPm: String get() = if (hour < 12) "AM" else "PM"
}

fun List<Weekday>.toRepeatDaysString(): String = joinToString(",") { it.value.toString() }
