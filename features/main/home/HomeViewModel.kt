package com.seungmin.miram.features.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seungmin.miram.shared.data.AlarmRepository
import com.seungmin.miram.shared.model.Alarm
import com.seungmin.miram.shared.model.nextTriggerAtMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeSortOrder {
    AlarmTime,
    Manual
}

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val sortOrder: HomeSortOrder = HomeSortOrder.AlarmTime,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val selectionMode = MutableStateFlow(false)
    private val sortOrder = MutableStateFlow(HomeSortOrder.AlarmTime)

    val uiState = combine(
        repository.getAllAlarms(),
        selectedIds,
        selectionMode,
        sortOrder
    ) { alarms, selected, isSelectionMode, currentSortOrder ->
            HomeUiState(
                alarms = alarms.sortedFor(currentSortOrder),
                sortOrder = currentSortOrder,
                selectionMode = isSelectionMode,
                selectedIds = selected
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun toggleEnabled(alarm: Alarm) {
        viewModelScope.launch { repository.setEnabled(alarm, !alarm.isEnabled) }
    }

    fun onAlarmLongPress(alarmId: String) {
        selectionMode.value = true
        selectedIds.value = selectedIds.value + alarmId
    }

    fun onAlarmTap(alarmId: String, onEdit: () -> Unit) {
        if (!selectionMode.value) {
            onEdit()
            return
        }
        updateSelectedIds { selected ->
            if (alarmId in selected) selected - alarmId else selected + alarmId
        }
    }

    fun enterSelectionMode() {
        val alarms = uiState.value.alarms
        if (alarms.isEmpty()) return
        selectionMode.value = true
        selectedIds.value = if (alarms.size == 1) {
            setOf(alarms.first().id)
        } else {
            emptySet()
        }
    }

    fun updateSortOrder(order: HomeSortOrder) {
        sortOrder.value = order
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
        selectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun enableSelected() {
        applyToSelectedAlarms { alarm -> repository.setEnabled(alarm, true) }
    }

    fun deleteSelected() {
        applyToSelectedAlarms(repository::deleteAlarm)
    }

    private fun updateSelectedIds(transform: (Set<String>) -> Set<String>) {
        selectedIds.value = transform(selectedIds.value)
    }

    private fun applyToSelectedAlarms(action: suspend (Alarm) -> Unit) {
        val selected = selectedIds.value
        viewModelScope.launch {
            for (alarm in uiState.value.alarms) {
                if (alarm.id in selected) {
                    action(alarm)
                }
            }
            exitSelectionMode()
        }
    }
}

private fun List<Alarm>.sortedFor(sortOrder: HomeSortOrder): List<Alarm> = when (sortOrder) {
    HomeSortOrder.AlarmTime -> sortedForClockOrder()
    HomeSortOrder.Manual -> sortedBy { it.createdAt }
}

private fun List<Alarm>.sortedForClockOrder(): List<Alarm> {
    return sortedWith(
        compareBy<Alarm> { alarm -> alarm.nextTriggerAtMillis() }
            .thenBy { it.createdAt }
    )
}
