package com.seungmin.miram.features.main.alarmringing

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seungmin.miram.shared.alarm.AlarmForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmRingingUiState(
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isUnlimited: Boolean = false,
    val isDismissed: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    val remainingLabel: String
        get() = com.seungmin.miram.shared.model.RingDuration.label(
            if (isUnlimited) 0 else remainingSeconds
        )
}

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmRingingUiState())
    val uiState: StateFlow<AlarmRingingUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun initialize(ringDuration: Int) {
        if (_uiState.value.totalSeconds > 0 || _uiState.value.isUnlimited) return
        val normalizedDuration = ringDuration.coerceAtLeast(0)
        _uiState.value = AlarmRingingUiState(
            remainingSeconds = normalizedDuration,
            totalSeconds = normalizedDuration,
            isUnlimited = normalizedDuration == 0
        )
        if (normalizedDuration > 0) {
            startCountdown()
        }
    }

    fun dismiss() {
        countdownJob?.cancel()
        stopService()
        _uiState.value = _uiState.value.copy(isDismissed = true)
    }

    private fun startCountdown() {
        countdownJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1_000L)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            dismiss()
        }
    }

    private fun stopService() {
        val intent = Intent(context, AlarmForegroundService::class.java).apply {
            action = AlarmForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
