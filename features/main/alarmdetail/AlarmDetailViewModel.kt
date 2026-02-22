package com.example.miram.features.main.alarmdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miram.shared.data.AlarmRepository
import com.example.miram.shared.model.Alarm
import com.example.miram.shared.model.Weekday
import com.example.miram.shared.model.toRepeatDaysString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AlarmDetailUiState(
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "",
    val selectedDays: Set<Weekday> = emptySet(),
    val ringDuration: Int = 60,
    val soundUri: String = "",
    val soundTitle: String = "기본 알람",
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    private val repository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: String? = savedStateHandle["alarmId"]
    val isEditMode: Boolean get() = alarmId != null

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != null) loadAlarm(alarmId)
        else {
            val now = Calendar.getInstance()
            _uiState.value = _uiState.value.copy(
                hour = now.get(Calendar.HOUR_OF_DAY),
                minute = now.get(Calendar.MINUTE)
            )
        }
    }

    private fun loadAlarm(id: String) {
        viewModelScope.launch {
            val alarm = repository.getAlarmById(id) ?: return@launch
            _uiState.value = _uiState.value.copy(
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                selectedDays = alarm.repeatWeekdays().toSet(),
                ringDuration = alarm.ringDuration,
                soundUri = alarm.soundUri
            )
        }
    }

    fun onHourChange(hour: Int) { _uiState.value = _uiState.value.copy(hour = hour) }
    fun onMinuteChange(minute: Int) { _uiState.value = _uiState.value.copy(minute = minute) }
    fun onLabelChange(label: String) { _uiState.value = _uiState.value.copy(label = label) }
    fun onRingDurationChange(seconds: Int) { _uiState.value = _uiState.value.copy(ringDuration = seconds) }

    fun onSoundSelected(uri: String, title: String) {
        _uiState.value = _uiState.value.copy(soundUri = uri, soundTitle = title)
    }

    fun toggleWeekday(weekday: Weekday) {
        val current = _uiState.value.selectedDays.toMutableSet()
        if (weekday in current) current.remove(weekday) else current.add(weekday)
        _uiState.value = _uiState.value.copy(selectedDays = current)
    }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            val alarm = Alarm(
                id = alarmId ?: java.util.UUID.randomUUID().toString(),
                hour = s.hour,
                minute = s.minute,
                repeatDays = s.selectedDays.sortedBy { it.value }.toRepeatDaysString(),
                label = s.label,
                isEnabled = true,
                ringDuration = s.ringDuration,
                soundUri = s.soundUri
            )
            if (alarmId == null) repository.addAlarm(alarm)
            else repository.updateAlarm(alarm)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
