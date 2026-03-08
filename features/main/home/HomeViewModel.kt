package com.example.miram.features.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miram.shared.data.AlarmRepository
import com.example.miram.shared.model.Alarm
import com.example.miram.shared.model.Weekday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState = repository.getAllAlarms()
        .map { alarms -> alarms.sortedForClockOrder() }
        .combine(selectedIds) { alarms, selected ->
            HomeUiState(
                alarms = alarms,
                selectionMode = selected.isNotEmpty(),
                selectedIds = selected
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch { repository.deleteAlarm(alarm) }
    }

    fun toggleEnabled(alarm: Alarm) {
        viewModelScope.launch { repository.setEnabled(alarm, !alarm.isEnabled) }
    }

    fun onAlarmLongPress(alarmId: String) {
        selectedIds.value = selectedIds.value + alarmId
    }

    fun onAlarmTap(alarmId: String, onEdit: () -> Unit) {
        if (selectedIds.value.isEmpty()) {
            onEdit()
            return
        }
        selectedIds.value = if (alarmId in selectedIds.value) {
            selectedIds.value - alarmId
        } else {
            selectedIds.value + alarmId
        }
    }

    fun toggleSelectAll() {
        val alarms = uiState.value.alarms
        selectedIds.value = if (selectedIds.value.size == alarms.size) {
            emptySet()
        } else {
            alarms.map { it.id }.toSet()
        }
    }

    fun exitSelectionMode() {
        selectedIds.value = emptySet()
    }

    fun enableSelected() {
        val selected = selectedIds.value
        viewModelScope.launch {
            uiState.value.alarms.filter { it.id in selected }.forEach { alarm ->
                repository.setEnabled(alarm, true)
            }
            selectedIds.value = emptySet()
        }
    }

    fun deleteSelected() {
        val selected = selectedIds.value
        viewModelScope.launch {
            uiState.value.alarms.filter { it.id in selected }.forEach { alarm ->
                repository.deleteAlarm(alarm)
            }
            selectedIds.value = emptySet()
        }
    }
}

private fun List<Alarm>.sortedForClockOrder(): List<Alarm> {
    val now = Calendar.getInstance()
    return sortedWith(
        compareByDescending<Alarm> { alarm -> nextTriggerAtMillis(alarm, now) ?: Long.MIN_VALUE }
            .thenByDescending { it.createdAt }
    )
}

private fun nextTriggerAtMillis(alarm: Alarm, now: Calendar = Calendar.getInstance()): Long? {
    fun fromBase(base: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = base }
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
        cal.set(Calendar.MINUTE, alarm.minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        while (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    if (alarm.specificDateMillis != null) return fromBase(alarm.specificDateMillis)

    val repeatDays = alarm.repeatWeekdays().toSet()
    if (repeatDays.isNotEmpty()) {
        val map = mapOf(
            Weekday.SUN to Calendar.SUNDAY,
            Weekday.MON to Calendar.MONDAY,
            Weekday.TUE to Calendar.TUESDAY,
            Weekday.WED to Calendar.WEDNESDAY,
            Weekday.THU to Calendar.THURSDAY,
            Weekday.FRI to Calendar.FRIDAY,
            Weekday.SAT to Calendar.SATURDAY
        )
        return repeatDays.mapNotNull { weekday ->
            val targetDow = map[weekday] ?: return@mapNotNull null
            Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                set(Calendar.DAY_OF_WEEK, targetDow)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.WEEK_OF_YEAR, 1)
            }.timeInMillis
        }.minOrNull()
    }

    return Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis
}
