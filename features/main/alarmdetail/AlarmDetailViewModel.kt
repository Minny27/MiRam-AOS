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
import com.example.miram.shared.model.normalizeSpecificDateMillis
import com.example.miram.shared.model.wasSpecificDateAdjusted
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmDetailUiState(
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "",
    val isLabelLimitExceeded: Boolean = false,
    val selectedDays: Set<Weekday> = emptySet(),
    val ringDuration: Int = 0,
    val soundUri: String = "",
    val soundTitle: String = "기본 알람",
    val soundEnabled: Boolean = true,
    val selectedDateMillis: Long? = null,
    val vibrateEnabled: Boolean = true,
    val vibrationMode: String = "Basic call",
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val hasUnsavedChanges: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    private val repository: AlarmRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val MAX_ALARM_LABEL_LENGTH = 40
    }

    private data class AlarmDetailDraft(
        val hour: Int,
        val minute: Int,
        val label: String,
        val selectedDays: Set<Weekday>,
        val ringDuration: Int,
        val soundUri: String,
        val soundTitle: String,
        val soundEnabled: Boolean,
        val selectedDateMillis: Long?,
        val vibrateEnabled: Boolean,
        val vibrationMode: String,
        val snoozeEnabled: Boolean,
        val snoozeIntervalMinutes: Int,
        val snoozeRepeatCount: Int
    )

    private val alarmId: String? = savedStateHandle["alarmId"]
    val isEditMode: Boolean get() = alarmId != null

    private val _uiState = MutableStateFlow(AlarmDetailUiState())
    val uiState: StateFlow<AlarmDetailUiState> = _uiState.asStateFlow()
    private var initialDraft: AlarmDetailDraft? = null

    init {
        if (alarmId != null) loadAlarm(alarmId)
        else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val defaultSoundTitle = defaultSoundUri?.let {
                RingtoneManager.getRingtone(context, it)?.getTitle(context)
            } ?: "기본 알람"
            setInitialState(_uiState.value.copy(
                hour = 6,
                minute = 0,
                ringDuration = RingDuration.normalize(_uiState.value.ringDuration),
                soundUri = defaultSoundUri?.toString().orEmpty(),
                soundTitle = defaultSoundTitle
            ))
        }
    }

    private fun loadAlarm(id: String) {
        viewModelScope.launch {
            val alarm = repository.getAlarmById(id) ?: return@launch
            setInitialState(_uiState.value.copy(
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label.take(MAX_ALARM_LABEL_LENGTH),
                isLabelLimitExceeded = alarm.label.length > MAX_ALARM_LABEL_LENGTH,
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
            ))
        }
    }

    fun onHourChange(hour: Int) = updateState { copy(hour = hour) }
    fun onMinuteChange(minute: Int) = updateState { copy(minute = minute) }
    fun onLabelChange(label: String) {
        val normalizedLabel = label.take(MAX_ALARM_LABEL_LENGTH)
        updateState {
            copy(
                label = normalizedLabel,
                isLabelLimitExceeded = label.length > MAX_ALARM_LABEL_LENGTH
            )
        }
    }
    fun onRingDurationChange(seconds: Int) {
        updateState { copy(ringDuration = RingDuration.normalize(seconds)) }
    }

    fun onSoundSelected(uri: String, title: String) {
        updateState { copy(soundUri = uri, soundTitle = title) }
    }
    fun onSoundToggle(enabled: Boolean) = updateState { copy(soundEnabled = enabled) }
    fun onDateSelected(millis: Long?) {
        updateState {
            copy(
                selectedDateMillis = millis,
                selectedDays = if (millis != null) emptySet() else selectedDays
            )
        }
    }
    fun onVibrateToggle(enabled: Boolean) = updateState { copy(vibrateEnabled = enabled) }
    fun onVibrationModeChange(mode: String) = updateState { copy(vibrationMode = mode) }
    fun onSnoozeToggle(enabled: Boolean) = updateState { copy(snoozeEnabled = enabled) }
    fun onSnoozeIntervalChange(minutes: Int) = updateState { copy(snoozeIntervalMinutes = minutes) }
    fun onSnoozeRepeatCountChange(count: Int) = updateState { copy(snoozeRepeatCount = count) }

    fun toggleWeekday(weekday: Weekday) {
        updateState {
            val current = selectedDays.toMutableSet()
            if (weekday in current) current.remove(weekday) else current.add(weekday)
            copy(
                selectedDays = current,
                selectedDateMillis = if (current.isNotEmpty()) null else selectedDateMillis
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            val normalizedDateMillis = normalizeSpecificDateMillis(
                selectedDateMillis = s.selectedDateMillis,
                hour = s.hour,
                minute = s.minute
            )
            if (wasSpecificDateAdjusted(s.selectedDateMillis, s.hour, s.minute)) {
                updateState { copy(selectedDateMillis = normalizedDateMillis) }
                Toast.makeText(
                    context,
                    "이미 지난 날짜는 선택할 수 없어요. 알람이 내일 울리도록 설정했어요",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            val alarm = Alarm(
                id = alarmId ?: java.util.UUID.randomUUID().toString(),
                hour = s.hour,
                minute = s.minute,
                repeatDays = s.selectedDays.toRepeatDaysString(order = Weekday.storageOrder),
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
            repository.removeDuplicatesOf(alarm, excludeId = alarmId)
            if (alarmId == null) repository.addAlarm(alarm)
            else repository.updateAlarm(alarm)
            initialDraft = _uiState.value.toDraft()
            _uiState.value = _uiState.value.copy(
                hasUnsavedChanges = false,
                isSaved = true
            )
        }
    }

    private fun setInitialState(state: AlarmDetailUiState) {
        initialDraft = state.toDraft()
        _uiState.value = state.copy(
            hasUnsavedChanges = false,
            isSaved = false,
            error = null
        )
    }

    private fun updateState(transform: AlarmDetailUiState.() -> AlarmDetailUiState) {
        val updated = _uiState.value.transform().copy(isSaved = false)
        val hasUnsavedChanges = initialDraft?.let { updated.toDraft() != it } ?: false
        _uiState.value = updated.copy(hasUnsavedChanges = hasUnsavedChanges)
    }

    private fun AlarmDetailUiState.toDraft(): AlarmDetailDraft = AlarmDetailDraft(
        hour = hour,
        minute = minute,
            label = label,
            selectedDays = selectedDays,
            ringDuration = ringDuration,
        soundUri = soundUri,
        soundTitle = soundTitle,
        soundEnabled = soundEnabled,
        selectedDateMillis = selectedDateMillis,
        vibrateEnabled = vibrateEnabled,
        vibrationMode = vibrationMode,
        snoozeEnabled = snoozeEnabled,
        snoozeIntervalMinutes = snoozeIntervalMinutes,
        snoozeRepeatCount = snoozeRepeatCount
    )
}
