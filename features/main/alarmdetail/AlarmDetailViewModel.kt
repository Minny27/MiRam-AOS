package com.example.miram.features.main.alarmdetail

import android.content.Context
import android.media.RingtoneManager
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miram.shared.data.AlarmRepository
import com.example.miram.shared.model.Alarm
import com.example.miram.shared.model.RingDuration
import com.example.miram.shared.model.Weekday
import com.example.miram.shared.model.toRepeatDaysString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val soundEnabled: Boolean = true,
    val selectedDateMillis: Long? = null,
    val vibrateEnabled: Boolean = true,
    val vibrationMode: String = "Basic call",
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    private val repository: AlarmRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val alarmId: String? = savedStateHandle["alarmId"]
    val isEditMode: Boolean get() = alarmId != null

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != null) loadAlarm(alarmId)
        else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val defaultSoundTitle = defaultSoundUri?.let {
                RingtoneManager.getRingtone(context, it)?.getTitle(context)
            } ?: "기본 알람"
            _uiState.value = _uiState.value.copy(
                hour = 6,
                minute = 0,
                ringDuration = RingDuration.normalize(_uiState.value.ringDuration),
                soundUri = defaultSoundUri?.toString().orEmpty(),
                soundTitle = defaultSoundTitle
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
                ringDuration = RingDuration.normalize(alarm.ringDuration),
                soundUri = alarm.soundUri,
                soundEnabled = alarm.soundEnabled,
                selectedDateMillis = alarm.specificDateMillis,
                vibrateEnabled = alarm.vibrateEnabled,
                vibrationMode = alarm.vibrationMode,
                snoozeEnabled = alarm.snoozeEnabled,
                snoozeIntervalMinutes = alarm.snoozeIntervalMinutes,
                snoozeRepeatCount = alarm.snoozeRepeatCount,
                soundTitle = alarm.soundUri.takeIf { it.isNotBlank() }?.let { uriString ->
                    runCatching {
                        RingtoneManager.getRingtone(context, android.net.Uri.parse(uriString))
                            ?.getTitle(context)
                    }.getOrNull()
                } ?: "기본 알람"
            )
        }
    }

    fun onHourChange(hour: Int) { _uiState.value = _uiState.value.copy(hour = hour) }
    fun onMinuteChange(minute: Int) { _uiState.value = _uiState.value.copy(minute = minute) }
    fun onLabelChange(label: String) { _uiState.value = _uiState.value.copy(label = label) }
    fun onRingDurationChange(seconds: Int) {
        _uiState.value = _uiState.value.copy(ringDuration = RingDuration.normalize(seconds))
    }

    fun onSoundSelected(uri: String, title: String) {
        _uiState.value = _uiState.value.copy(soundUri = uri, soundTitle = title)
    }
    fun onSoundToggle(enabled: Boolean) { _uiState.value = _uiState.value.copy(soundEnabled = enabled) }
    fun onDateSelected(millis: Long?) {
        _uiState.value = _uiState.value.copy(
            selectedDateMillis = millis,
            selectedDays = if (millis != null) emptySet() else _uiState.value.selectedDays
        )
    }
    fun onVibrateToggle(enabled: Boolean) { _uiState.value = _uiState.value.copy(vibrateEnabled = enabled) }
    fun onVibrationModeChange(mode: String) { _uiState.value = _uiState.value.copy(vibrationMode = mode) }
    fun onSnoozeToggle(enabled: Boolean) { _uiState.value = _uiState.value.copy(snoozeEnabled = enabled) }
    fun onSnoozeIntervalChange(minutes: Int) { _uiState.value = _uiState.value.copy(snoozeIntervalMinutes = minutes) }
    fun onSnoozeRepeatCountChange(count: Int) { _uiState.value = _uiState.value.copy(snoozeRepeatCount = count) }

    fun toggleWeekday(weekday: Weekday) {
        val current = _uiState.value.selectedDays.toMutableSet()
        if (weekday in current) current.remove(weekday) else current.add(weekday)
        _uiState.value = _uiState.value.copy(
            selectedDays = current,
            selectedDateMillis = if (current.isNotEmpty()) null else _uiState.value.selectedDateMillis
        )
    }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            val weekdayOrder = listOf(
                Weekday.SUN,
                Weekday.MON,
                Weekday.TUE,
                Weekday.WED,
                Weekday.THU,
                Weekday.FRI,
                Weekday.SAT
            )
            val (normalizedDateMillis, wasAdjustedToTomorrow) = normalizeSpecificDateMillis(
                selectedDateMillis = s.selectedDateMillis,
                hour = s.hour,
                minute = s.minute
            )
            val alarm = Alarm(
                id = alarmId ?: java.util.UUID.randomUUID().toString(),
                hour = s.hour,
                minute = s.minute,
                repeatDays = weekdayOrder.filter { it in s.selectedDays }.toRepeatDaysString(),
                label = s.label,
                isEnabled = true,
                ringDuration = RingDuration.normalize(s.ringDuration),
                soundUri = s.soundUri.ifBlank {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.toString().orEmpty()
                },
                soundEnabled = s.soundEnabled,
                specificDateMillis = normalizedDateMillis,
                vibrateEnabled = s.vibrateEnabled,
                vibrationMode = s.vibrationMode,
                snoozeEnabled = s.snoozeEnabled,
                snoozeIntervalMinutes = s.snoozeIntervalMinutes,
                snoozeRepeatCount = s.snoozeRepeatCount
            )
            if (alarmId == null) repository.addAlarm(alarm)
            else repository.updateAlarm(alarm)
            if (wasAdjustedToTomorrow) {
                Toast.makeText(
                    context,
                    "이미 지난 날짜는 선택할 수 없어요. 알람이 내일 울리도록 설정했어요",
                    Toast.LENGTH_LONG
                ).show()
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private fun normalizeSpecificDateMillis(
        selectedDateMillis: Long?,
        hour: Int,
        minute: Int
    ): Pair<Long?, Boolean> {
        if (selectedDateMillis == null) return null to false
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis > now.timeInMillis) return cal.timeInMillis to false
        while (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis to true
    }

    fun deleteAlarm() {
        val id = alarmId ?: return
        viewModelScope.launch {
            val alarm = repository.getAlarmById(id) ?: return@launch
            repository.deleteAlarm(alarm)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
