package com.example.miram.features.main.alarmdetail

import android.app.Activity
import android.app.AlertDialog as PlatformAlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.miram.shared.model.RingDuration
import com.example.miram.shared.model.Weekday
import com.example.miram.shared.style.AccentColor
import com.example.miram.shared.style.Background
import com.example.miram.shared.style.BackgroundGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    onBack: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    val latestOnBack by rememberUpdatedState(onBack)
    val latestSave by rememberUpdatedState(viewModel::save)
    val isDarkTheme = isSystemInDarkTheme()
    val screenBackground = if (isDarkTheme) Color.Black else Color.Background
    val cardBackground = if (isDarkTheme) Color.BackgroundGray else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    val requestBackNavigation = {
        if (uiState.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = uiState.hasUnsavedChanges && !uiState.isSaved) {
        showDiscardDialog = true
    }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.let { data ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
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
        containerColor = screenBackground,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(screenBackground),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = contentColor
                    ),
                    onClick = requestBackNavigation,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent)
                ) {
                    Text(
                        "취소", fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.background(Color.Transparent)
                    )
                }
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = contentColor
                    ),
                    onClick = { viewModel.save() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "저장", fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.background(Color.Transparent)
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBackground)
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
                    containerColor = cardBackground
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
                            val currentUri = uiState.soundUri.takeIf { it.isNotBlank() }?.toUri()
                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_ALARM
                                )
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
                    RingDurationSelector(
                        selected = uiState.ringDuration,
                        onSelect = viewModel::onRingDurationChange
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showDiscardDialog) {
        DisposableEffect(context) {
            val dialog = PlatformAlertDialog.Builder(context)
                .setTitle("변경사항을 저장할까요?")
                .setNegativeButton("취소", null)
                .setNeutralButton("저장 안 함") { _, _ ->
                    latestOnBack()
                }
                .setPositiveButton("저장") { _, _ ->
                    latestSave()
                }
                .create()
            dialog.show()
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
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
    val context = LocalContext.current
    val today = Calendar.getInstance()
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val formatter = remember { SimpleDateFormat("M월 d일 (E)", Locale.KOREAN) }
    val weekdayOrder = listOf(
        Weekday.SUN,
        Weekday.MON,
        Weekday.TUE,
        Weekday.WED,
        Weekday.THU,
        Weekday.FRI,
        Weekday.SAT
    )

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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdayOrder.forEach { weekday ->
                FilterChip(
                    selected = weekday in selectedDays,
                    onClick = { onToggleWeekday(weekday) },
                    label = { Text(weekday.label, textAlign = TextAlign.Center) },
                )
            }
        }
    }
}

@Composable
private fun TimePickerSection(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val sectionBackground = if (isDarkTheme) Color.Black else Color.Background
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val isPm = hour >= 12
    val hour12 = if (hour % 12 == 0) 12 else hour % 12

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(sectionBackground),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
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
        Text(
            ":",
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 6.dp),
            color = contentColor
        )
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
    width: Dp,
    onSelect: (Int) -> Unit
) {
    if (values.isEmpty()) return
    WheelPicker(values = values, selectedIndex = selectedIndex, width = width, onSelect = onSelect)
}

@Composable
private fun WheelPicker(
    values: List<String>,
    selectedIndex: Int,
    width: Dp,
    onSelect: (Int) -> Unit
) {
    if (values.isEmpty()) return
    val isDarkTheme = isSystemInDarkTheme()
    val wheelBackground = if (isDarkTheme) Color.BackgroundGray else Color.Background
    val safeIndex = selectedIndex.coerceIn(0, values.lastIndex)
    val itemHeight = 104.dp
    val isLooping = values.size > 2
    val repeatedCycles = if (isLooping) 100 else 1
    val totalItems = values.size * repeatedCycles
    val scope = rememberCoroutineScope()

    fun actualIndex(listIndex: Int): Int =
        ((listIndex % values.size) + values.size) % values.size

    fun targetListIndex(actual: Int, around: Int? = null): Int {
        if (!isLooping) return actual
        if (around == null) {
            return values.size * (repeatedCycles / 2) + actual
        }
        val cycle = around / values.size
        return listOf(cycle - 1, cycle, cycle + 1)
            .map { it * values.size + actual }
            .filter { it in 0 until totalItems }
            .minByOrNull { abs(it - around) }
            ?: (values.size * (repeatedCycles / 2) + actual)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = targetListIndex(safeIndex)
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val selectedListIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo
                .minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }
                ?.index
                ?.coerceIn(0, totalItems - 1)
                ?: targetListIndex(safeIndex)
        }
    }

    LaunchedEffect(safeIndex, totalItems) {
        val currentActual = actualIndex(selectedListIndex)
        if (!listState.isScrollInProgress && currentActual != safeIndex) {
            listState.scrollToItem(targetListIndex(safeIndex, selectedListIndex))
        }
    }

    LaunchedEffect(listState, totalItems, safeIndex) {
        snapshotFlow {
            listState.isScrollInProgress to selectedListIndex
        }.collect { (isScrolling, currentSelectedIndex) ->
            if (isScrolling) return@collect
            val actual = actualIndex(currentSelectedIndex)
            onSelect(actual)
            if (isLooping) {
                val recenteredIndex = targetListIndex(actual, currentSelectedIndex)
                if (abs(recenteredIndex - currentSelectedIndex) > values.size * 10) {
                    listState.scrollToItem(recenteredIndex)
                }
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.width(width),
        colors = CardDefaults.elevatedCardColors(
            containerColor = wheelBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(312.dp)
                .background(wheelBackground)
                .clip(MaterialTheme.shapes.large)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.shapes.medium
                    )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = itemHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(totalItems, key = { index -> index }) { listIndex ->
                    val actual = actualIndex(listIndex)
                    val isSelected = listIndex == selectedListIndex
                    val distance = abs(listIndex - selectedListIndex)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .clickable {
                                scope.launch {
                                    listState.animateScrollToItem(listIndex)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = values[actual],
                            fontSize = if (isSelected) 28.sp else 22.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onSurface
                                distance == 1 -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> Color.LightGray.copy(alpha = 0.55f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundSettingRow(
    soundTitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onPickRingtone: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val rowBackground = if (isDarkTheme) Color.BackgroundGray else Color.White
    OutlinedCard(onClick = onPickRingtone, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .background(rowBackground)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text("알림음", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) soundTitle else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = detailSwitchColors()
            )
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
    val isDarkTheme = isSystemInDarkTheme()
    val rowBackground = if (isDarkTheme) Color.BackgroundGray else Color.White
    OutlinedCard(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(rowBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("진동", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) mode else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = detailSwitchColors()
            )
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
    val isDarkTheme = isSystemInDarkTheme()
    val rowBackground = if (isDarkTheme) Color.BackgroundGray else Color.White
    OutlinedCard(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .background(rowBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("다시 울림", style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (enabled) "${intervalMinutes}분, ${repeatCount}회" else "사용 안 함",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = detailSwitchColors()
            )
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
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) Color.BackgroundGray else Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, MaterialTheme.shapes.large)
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        RingDuration.allValues.forEach { seconds ->
            FilterChip(
                selected = seconds == selected,
                onClick = { onSelect(seconds) },
                label = { Text(RingDuration.label(seconds)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = containerColor,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        }
    }
}

@Composable
private fun detailSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = AccentColor,
    checkedBorderColor = AccentColor,
    uncheckedThumbColor = Color.LightGray,
    uncheckedTrackColor = Color.White,
    uncheckedBorderColor = Color.LightGray
)
