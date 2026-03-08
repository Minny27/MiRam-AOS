package com.example.miram.features.main.alarmdetail

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.miram.shared.model.RingDuration
import com.example.miram.shared.model.Weekday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    onBack: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.let { data ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            }
            val title = uri?.let {
                RingtoneManager.getRingtone(context, it)?.getTitle(context)
            } ?: "기본 알람"
            viewModel.onSoundSelected(uri?.toString() ?: "", title)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "알람 수정" else "알람 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (viewModel.isEditMode) {
                        IconButton(onClick = { viewModel.deleteAlarm() }) {
                            Icon(Icons.Default.Delete, contentDescription = "알람 삭제")
                        }
                    }
                    TextButton(onClick = { viewModel.save() }) {
                        Text("저장", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(Modifier.height(8.dp))
            TimePickerSection(
                hour = uiState.hour,
                minute = uiState.minute,
                onHourChange = viewModel::onHourChange,
                onMinuteChange = viewModel::onMinuteChange
            )

            Spacer(Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RepeatSection(
                        selectedDays = uiState.selectedDays,
                        selectedDateMillis = uiState.selectedDateMillis,
                        onToggleWeekday = viewModel::toggleWeekday,
                        onDateSelected = viewModel::onDateSelected
                    )

                    OutlinedTextField(
                        value = uiState.label,
                        onValueChange = viewModel::onLabelChange,
                        label = { Text("알람 이름") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    SoundSettingRow(
                        soundTitle = uiState.soundTitle,
                        enabled = uiState.soundEnabled,
                        onToggle = viewModel::onSoundToggle,
                        onPickRingtone = {
                            val currentUri = uiState.soundUri.takeIf { it.isNotBlank() }
                                ?.let { Uri.parse(it) }
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람 소리 선택")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                            }
                            ringtoneLauncher.launch(intent)
                        }
                    )

                    VibrationSettingRow(
                        enabled = uiState.vibrateEnabled,
                        mode = uiState.vibrationMode,
                        onToggle = viewModel::onVibrateToggle,
                        onModeChange = viewModel::onVibrationModeChange
                    )

                    SnoozeSettingRow(
                        enabled = uiState.snoozeEnabled,
                        intervalMinutes = uiState.snoozeIntervalMinutes,
                        repeatCount = uiState.snoozeRepeatCount,
                        onToggle = viewModel::onSnoozeToggle,
                        onIntervalChange = viewModel::onSnoozeIntervalChange,
                        onRepeatChange = viewModel::onSnoozeRepeatCountChange
                    )

                    Text("울림 시간", style = MaterialTheme.typography.labelLarge)
                    RingDurationSelector(selected = uiState.ringDuration, onSelect = viewModel::onRingDurationChange)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RepeatSection(
    selectedDays: Set<Weekday>,
    selectedDateMillis: Long?,
    onToggleWeekday: (Weekday) -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val today = Calendar.getInstance()
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val formatter = remember { SimpleDateFormat("M월 d일 (E)", Locale.KOREAN) }
    val weekdayOrder = listOf(Weekday.SUN, Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI, Weekday.SAT)

    val title = when {
        selectedDateMillis != null -> {
            val selected = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            when {
                isSameDate(selected, today) -> "오늘-${formatter.format(selected.time)}"
                isSameDate(selected, tomorrow) -> "내일-${formatter.format(selected.time)}"
                else -> formatter.format(selected.time)
            }
        }
        selectedDays.size == 7 -> "매일"
        selectedDays.isNotEmpty() -> {
            val labels = weekdayOrder.filter { it in selectedDays }.joinToString(", ") { it.label }
            "매주 $labels"
        }
        else -> "내일-${formatter.format(tomorrow.time)}"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = {
                val base = selectedDateMillis ?: tomorrow.timeInMillis
                val cal = Calendar.getInstance().apply { timeInMillis = base }
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        cal.set(y, m, d, 0, 0, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        onDateSelected(cal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = today.timeInMillis
                }.show()
            }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "달력")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            weekdayOrder.forEach { weekday ->
                FilterChip(
                    selected = weekday in selectedDays,
                    onClick = { onToggleWeekday(weekday) },
                    label = { Text(weekday.label, textAlign = TextAlign.Center) }
                )
            }
        }
    }
}

@Composable
private fun TimePickerSection(hour: Int, minute: Int, onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit) {
    val isPm = hour >= 12
    val hour12 = if (hour % 12 == 0) 12 else hour % 12

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AmPmPicker(
            isPm = isPm,
            onChange = { onHourChange(to24Hour(it, hour12)) }
        )
        Spacer(Modifier.width(10.dp))
        Wheel3Picker(
            values = (1..12).map { "%02d".format(it) },
            selectedIndex = hour12 - 1,
            width = 86.dp
        ) { index ->
            onHourChange(to24Hour(isPm, index + 1))
        }
        Text(":", fontSize = 38.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(horizontal = 6.dp))
        Wheel3Picker(
            values = (0..59).map { "%02d".format(it) },
            selectedIndex = minute,
            width = 86.dp
        ) { index ->
            onMinuteChange(index)
        }
    }
}

@Composable
private fun AmPmPicker(isPm: Boolean, onChange: (Boolean) -> Unit) {
    WheelPicker(
        values = listOf("오전", "오후"),
        selectedIndex = if (isPm) 1 else 0,
        width = 100.dp,
        onSelect = { onChange(it == 1) }
    )
}

@Composable
private fun Wheel3Picker(
    values: List<String>,
    selectedIndex: Int,
    width: androidx.compose.ui.unit.Dp,
    onSelect: (Int) -> Unit
) {
    if (values.isEmpty()) return
    WheelPicker(values = values, selectedIndex = selectedIndex, width = width, onSelect = onSelect)
}

@Composable
private fun WheelPicker(
    values: List<String>,
    selectedIndex: Int,
    width: androidx.compose.ui.unit.Dp,
    onSelect: (Int) -> Unit
) {
    if (values.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(0, values.lastIndex)
    ElevatedCard(
        modifier = Modifier.width(width),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = 0
                    maxValue = values.lastIndex
                    displayedValues = values.toTypedArray()
                    wrapSelectorWheel = values.size > 2
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    value = safeIndex
                    removeNumberPickerDivider(this)
                    setOnValueChangedListener { _, _, newVal -> onSelect(newVal) }
                }
            },
            update = { picker ->
                if (picker.maxValue != values.lastIndex) {
                    picker.displayedValues = null
                    picker.minValue = 0
                    picker.maxValue = values.lastIndex
                    picker.displayedValues = values.toTypedArray()
                }
                picker.wrapSelectorWheel = values.size > 2
                if (picker.value != safeIndex) {
                    picker.value = safeIndex
                }
                removeNumberPickerDivider(picker)
                picker.setOnValueChangedListener { _, _, newVal -> onSelect(newVal) }
            }
        )
    }
}

private fun removeNumberPickerDivider(picker: NumberPicker) {
    runCatching {
        val field = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
        field.isAccessible = true
        field.set(picker, ColorDrawable(android.graphics.Color.TRANSPARENT))
    }
}

@Composable
private fun SoundSettingRow(
    soundTitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onPickRingtone: () -> Unit
) {
    OutlinedCard(onClick = onPickRingtone, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text("알림음", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) soundTitle else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) Color.White else Color.Gray
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun VibrationSettingRow(
    enabled: Boolean,
    mode: String,
    onToggle: (Boolean) -> Unit,
    onModeChange: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    OutlinedCard(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("진동", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) mode else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) Color.White else Color.Gray
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text("진동 방식") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("무음", "Basic call", "Heartbeat", "Short").forEach { option ->
                        AssistChip(
                            onClick = {
                                onModeChange(option)
                                openDialog = false
                            },
                            label = { Text(option) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (enabled && mode == option) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                labelColor = if (enabled && mode == option) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { openDialog = false }) { Text("닫기") } }
        )
    }
}

@Composable
private fun SnoozeSettingRow(
    enabled: Boolean,
    intervalMinutes: Int,
    repeatCount: Int,
    onToggle: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRepeatChange: (Int) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    OutlinedCard(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("다시 울림", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) "${intervalMinutes}분, ${repeatCount}회" else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) Color.White else Color.Gray
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text("다시 울림 설정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("간격")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 10).forEach { m ->
                            AssistChip(
                                onClick = { onIntervalChange(m) },
                                label = { Text("${m}분") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (enabled && intervalMinutes == m) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    labelColor = if (enabled && intervalMinutes == m) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            )
                        }
                    }
                    Text("반복")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 5).forEach { c ->
                            AssistChip(
                                onClick = { onRepeatChange(c) },
                                label = { Text("${c}회") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (enabled && repeatCount == c) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    labelColor = if (enabled && repeatCount == c) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { openDialog = false }) { Text("완료") } }
        )
    }
}

private fun to24Hour(isPm: Boolean, hour12: Int): Int {
    val base = hour12 % 12
    return if (isPm) base + 12 else base
}

private fun isSameDate(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

@Composable
private fun RingDurationSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RingDuration.allValues.forEach { seconds ->
            AssistChip(
                onClick = { onSelect(seconds) },
                label = { Text(RingDuration.label(seconds)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (seconds == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    labelColor = if (seconds == selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
