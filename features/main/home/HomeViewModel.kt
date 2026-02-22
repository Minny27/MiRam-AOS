package com.example.miram.features.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miram.shared.data.AlarmRepository
import com.example.miram.shared.model.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    val uiState = repository.getAllAlarms()
        .map { HomeUiState(alarms = it) }
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
}
