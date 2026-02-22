package com.example.miram.shared.alarm

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

    private val _ringingAlarm = MutableStateFlow<RingingAlarm?>(null)
    val ringingAlarm: StateFlow<RingingAlarm?> = _ringingAlarm.asStateFlow()

    fun startRinging(alarmId: String, label: String, ringDuration: Int) {
        _ringingAlarm.value = RingingAlarm(alarmId, label, ringDuration)
    }

    fun stopRinging() {
        _ringingAlarm.value = null
    }
}
