package com.example.miram.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miram.shared.alarm.AlarmStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun checkInitialRoute() {
        viewModelScope.launch {
            // 알람이 울리는 중이면 스플래시 딜레이 없이 즉시 진입
            if (AlarmStateHolder.ringingAlarm.value == null) {
                delay(1_500L)
            }
            _isAuthenticated.value = false
            _isReady.value = true
        }
    }
}
