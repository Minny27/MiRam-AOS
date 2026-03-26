package com.seungmin.miram.shared.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AlarmForegroundService ↔ UI 브리지.
 * 서비스가 시작·종료될 때 상태를 업데이트하고,
 * MainRoute가 이를 관찰해 AlarmRingingScreen으로 자동 이동한다.
 */
object AlarmStateHolder {

    data class RingingAlarm(
        val alarmId: String,
        val label: String,
        val ringDuration: Int
    )

    data class ActiveAlarmCycle(
        val alarmId: String,
        val label: String,
        val ringDuration: Int,
        val occurrenceAtMillis: Long,
        val isRinging: Boolean,
        val nextRingAtMillis: Long? = null,
        val remainingSnoozeCount: Int = 0
    )

    private val _ringingAlarm = MutableStateFlow<RingingAlarm?>(null)
    val ringingAlarm: StateFlow<RingingAlarm?> = _ringingAlarm.asStateFlow()
    private val _activeAlarmCycle = MutableStateFlow<ActiveAlarmCycle?>(null)
    val activeAlarmCycle: StateFlow<ActiveAlarmCycle?> = _activeAlarmCycle.asStateFlow()

    fun startRinging(alarmId: String, label: String, ringDuration: Int) {
        _ringingAlarm.value = RingingAlarm(alarmId, label, ringDuration)
        val occurrenceAtMillis = _activeAlarmCycle.value
            ?.takeIf { it.alarmId == alarmId }
            ?.occurrenceAtMillis
            ?: System.currentTimeMillis()
        _activeAlarmCycle.value = ActiveAlarmCycle(
            alarmId = alarmId,
            label = label,
            ringDuration = ringDuration,
            occurrenceAtMillis = occurrenceAtMillis,
            isRinging = true,
            nextRingAtMillis = null,
            remainingSnoozeCount = _activeAlarmCycle.value
                ?.takeIf { it.alarmId == alarmId }
                ?.remainingSnoozeCount
                ?: 0
        )
    }

    fun stopRinging() {
        _ringingAlarm.value = null
        _activeAlarmCycle.value = _activeAlarmCycle.value?.copy(isRinging = false)
    }

    fun keepCyclePending(alarmId: String, nextRingAtMillis: Long, remainingSnoozeCount: Int) {
        _activeAlarmCycle.value = _activeAlarmCycle.value
            ?.takeIf { it.alarmId == alarmId }
            ?.copy(
                isRinging = false,
                nextRingAtMillis = nextRingAtMillis,
                remainingSnoozeCount = remainingSnoozeCount
            )
    }

    fun clearCycle(alarmId: String?) {
        if (_activeAlarmCycle.value?.alarmId == alarmId) {
            _activeAlarmCycle.value = null
        }
    }
}
