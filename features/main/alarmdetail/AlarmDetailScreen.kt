package com.example.miram.features.main.alarmdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.miram.shared.model.RingDuration
import com.example.miram.shared.model.Weekday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    onBack: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "알람 편집" else "알람 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }) {
                        Text("저장", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 시간 선택 (NumberPicker 대체: 드롭다운)
            TimePickerSection(
                hour = uiState.hour,
                minute = uiState.minute,
                onHourChange = viewModel::onHourChange,
                onMinuteChange = viewModel::onMinuteChange
            )

            // 라벨
            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::onLabelChange,
                label = { Text("라벨 (선택)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 반복 요일
            WeekdaySelector(
                selectedDays = uiState.selectedDays,
                onToggle = viewModel::toggleWeekday
            )

            // Ring Duration
            RingDurationSelector(
                selected = uiState.ringDuration,
                onSelect = viewModel::onRingDurationChange
            )
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
    var showHourPicker by remember { mutableStateOf(false) }
    var showMinutePicker by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("시간", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            // Hour
            TextButton(onClick = { showHourPicker = true }) {
                Text("%02d".format(hour), fontSize = 48.sp)
            }
            Text(":", fontSize = 48.sp)
            // Minute
            TextButton(onClick = { showMinutePicker = true }) {
                Text("%02d".format(minute), fontSize = 48.sp)
            }
        }
    }

    if (showHourPicker) {
        NumberPickerDialog(
            title = "시 선택",
            range = 0..23,
            current = hour,
            onDismiss = { showHourPicker = false },
            onSelect = { onHourChange(it); showHourPicker = false }
        )
    }
    if (showMinutePicker) {
        NumberPickerDialog(
            title = "분 선택",
            range = 0..59,
            current = minute,
            onDismiss = { showMinutePicker = false },
            onSelect = { onMinuteChange(it); showMinutePicker = false }
        )
    }
}

@Composable
private fun NumberPickerDialog(
    title: String,
    range: IntRange,
    current: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var selected by remember { mutableIntStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                range.forEach { value ->
                    TextButton(
                        onClick = { selected = value },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "%02d".format(value),
                            color = if (value == selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSelect(selected) }) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
private fun WeekdaySelector(selectedDays: Set<Weekday>, onToggle: (Weekday) -> Unit) {
    Column {
        Text("반복", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Weekday.entries.forEach { weekday ->
                val selected = weekday in selectedDays
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(weekday) },
                    label = { Text(weekday.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RingDurationSelector(selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("울림 시간", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = RingDuration.label(selected),
                onValueChange = {},
                readOnly = true,
                label = { Text("울림 시간") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                RingDuration.allValues.forEach { seconds ->
                    DropdownMenuItem(
                        text = { Text(RingDuration.label(seconds)) },
                        onClick = { onSelect(seconds); expanded = false }
                    )
                }
            }
        }
    }
}

