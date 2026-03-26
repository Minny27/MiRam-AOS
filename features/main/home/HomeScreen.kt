package com.seungmin.miram.features.main.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.seungmin.miram.shared.model.Alarm
import com.seungmin.miram.shared.model.Weekday
import com.seungmin.miram.shared.style.AccentColor
import com.seungmin.miram.shared.style.Background
import com.seungmin.miram.shared.style.BackgroundGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAddAlarm: () -> Unit = {},
    onEditAlarm: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.selectionMode) {
        viewModel.exitSelectionMode()
    }

    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) Color.Black else Color.Background
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val uncheckedBoxBorderColor = if (isDarkTheme) Color.White else Color.LightGray
    val checkboxColors = CheckboxColors(
        checkedCheckmarkColor = Color.White,
        uncheckedCheckmarkColor = Color.Transparent,
        checkedBoxColor = AccentColor,
        uncheckedBoxColor = Color.Transparent,
        disabledCheckedBoxColor = Color.Transparent,
        disabledUncheckedBoxColor = Color.Transparent,
        disabledIndeterminateBoxColor = Color.Transparent,
        checkedBorderColor = AccentColor,
        uncheckedBorderColor = uncheckedBoxBorderColor,
        disabledBorderColor = Color.Transparent,
        disabledUncheckedBorderColor = Color.Transparent,
        disabledIndeterminateBorderColor = Color.Transparent
    )
    val dropdownContainerColor = if (isDarkTheme) {
        Color.BackgroundGray.copy(alpha = 0.92f)
    } else {
        Color.White.copy(alpha = 0.96f)
    }
    val dropdownContentColor = if (isDarkTheme) {
        Color.White
    } else {
        Color.Black
    }

    Scaffold(
        containerColor = containerColor,
        bottomBar = {
            if (uiState.selectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { viewModel.enableSelected() }) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = contentColor
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "켜기",
                            color = contentColor
                        )
                    }
                    TextButton(onClick = { viewModel.deleteSelected() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = contentColor
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "모두 삭제",
                            color = contentColor
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (!uiState.selectionMode) {
                val summary = rememberRecentAlarmSummary(uiState.alarms)
                val listState = rememberLazyListState()
                val isActionRowPinned by remember {
                    derivedStateOf { listState.firstVisibleItemIndex > 0 }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Spacer(Modifier.height(100.dp))
                        if (summary.isOffState || summary.isEmptyState) {
                            Text(
                                text = summary.title,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = contentColor
                            )
                        } else {
                            Text(
                                text = summary.title,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = contentColor
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = summary.dateTime,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = contentColor
                            )
                        }
                        Spacer(Modifier.height(80.dp))
                    }

                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(containerColor)
                                .padding(top = 8.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (isActionRowPinned) {
                                    Text(
                                        text = "알람",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = contentColor,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                            }
                            IconButton(onClick = onAddAlarm) {
                                Icon(Icons.Default.Add, contentDescription = "알람 추가")
                            }
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = dropdownContainerColor
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("편집", color = dropdownContentColor) },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.enterSelectionMode()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("정렬", color = dropdownContentColor) },
                                        onClick = {
                                            showMoreMenu = false
                                            showSortMenu = true
                                        }
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = dropdownContainerColor
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "알람 시간 순서",
                                                color = dropdownContentColor
                                            )
                                        },
                                        leadingIcon = {
                                            if (uiState.sortOrder == HomeSortOrder.AlarmTime) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = dropdownContentColor
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSortMenu = false
                                            viewModel.updateSortOrder(HomeSortOrder.AlarmTime)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "직접설정한 순서",
                                                color = dropdownContentColor
                                            )
                                        },
                                        leadingIcon = {
                                            if (uiState.sortOrder == HomeSortOrder.Manual) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = dropdownContentColor
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSortMenu = false
                                            viewModel.updateSortOrder(HomeSortOrder.Manual)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.alarms.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "알림이 없습니다",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            AlarmCardGroup(
                                alarms = uiState.alarms,
                                selectedIds = uiState.selectedIds,
                                selectionMode = uiState.selectionMode,
                                onTap = { alarm ->
                                    viewModel.onAlarmTap(alarm.id) { onEditAlarm(alarm.id) }
                                },
                                onLongPress = { alarm -> viewModel.onAlarmLongPress(alarm.id) },
                                onToggle = { alarm -> viewModel.toggleEnabled(alarm) }
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(100.dp))
                Text(
                    text = "${uiState.selectedIds.size}개 선택됨",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(80.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.selectedIds.size == uiState.alarms.size && uiState.alarms.isNotEmpty(),
                        onCheckedChange = { viewModel.toggleSelectAll() },
                        colors = checkboxColors
                    )
                    Text(
                        "전체",
                        color = contentColor
                    )
                }
            }

            if (uiState.selectionMode) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        AlarmCardGroup(
                            alarms = uiState.alarms,
                            selectedIds = uiState.selectedIds,
                            selectionMode = uiState.selectionMode,
                            onTap = { alarm ->
                                viewModel.onAlarmTap(alarm.id) { onEditAlarm(alarm.id) }
                            },
                            onLongPress = { alarm -> viewModel.onAlarmLongPress(alarm.id) },
                            onToggle = { alarm -> viewModel.toggleEnabled(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmCardGroup(
    alarms: List<Alarm>,
    selectedIds: Set<String>,
    selectionMode: Boolean,
    onTap: (Alarm) -> Unit,
    onLongPress: (Alarm) -> Unit,
    onToggle: (Alarm) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) Color.BackgroundGray else Color.White
    val separatorColor = if (isDarkTheme) Color.Black else Color.Background

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            alarms.forEachIndexed { index, alarm ->
                AlarmRow(
                    alarm = alarm,
                    selected = alarm.id in selectedIds,
                    selectionMode = selectionMode,
                    isDarkTheme = isDarkTheme,
                    onTap = { onTap(alarm) },
                    onLongPress = { onLongPress(alarm) },
                    onToggle = { onToggle(alarm) }
                )
                if (index != alarms.lastIndex) {
                    HorizontalDivider(
                        color = separatorColor,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmRow(
    alarm: Alarm,
    selected: Boolean,
    selectionMode: Boolean,
    isDarkTheme: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onToggle: () -> Unit
) {
    val dateLabel = rememberAlarmDateLabel(alarm)
    val activeTextColor = if (isDarkTheme) Color.White else Color.Black
    val inactiveTextColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.42f)
    } else {
        Color.Black.copy(alpha = 0.32f)
    }
    val selectedTextColor = MaterialTheme.colorScheme.primary
    val contentColor = when {
        selected -> selectedTextColor
        alarm.isEnabled -> activeTextColor
        else -> inactiveTextColor
    }
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = AccentColor,
        checkedBorderColor = AccentColor,
        uncheckedThumbColor = Color.LightGray,
        uncheckedTrackColor = Color.White,
        uncheckedBorderColor = Color.LightGray,
    )
    val uncheckedBoxBorderColor = if (isDarkTheme) Color.White else Color.LightGray
    val checkboxColors = CheckboxColors(
        checkedCheckmarkColor = Color.White,
        uncheckedCheckmarkColor = Color.Transparent,
        checkedBoxColor = AccentColor,
        uncheckedBoxColor = Color.Transparent,
        disabledCheckedBoxColor = Color.Transparent,
        disabledUncheckedBoxColor = Color.Transparent,
        disabledIndeterminateBoxColor = Color.Transparent,
        checkedBorderColor = AccentColor,
        uncheckedBorderColor = uncheckedBoxBorderColor,
        disabledBorderColor = Color.Transparent,
        disabledUncheckedBorderColor = Color.Transparent,
        disabledIndeterminateBorderColor = Color.Transparent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectionMode) {
            Checkbox(
                checked = selected, onCheckedChange = { onTap() },
                colors = checkboxColors
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (!alarm.label.isBlank()) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                Spacer(Modifier.height(20.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.amPm,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = alarm.twelveHourTimeString,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = contentColor
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (!selectionMode) {
                Spacer(Modifier.width(10.dp))
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = switchColors
                )
            }
        }
    }
}

@Composable
private fun rememberRecentAlarmSummary(alarms: List<Alarm>): RecentAlarmSummary {
    if (alarms.isEmpty()) {
        return RecentAlarmSummary(
            title = "알림",
            time = "",
            dateTime = "",
            isOffState = false,
            isEmptyState = true
        )
    }
    val now = Calendar.getInstance()
    val nextEnabled = alarms
        .filter { it.isEnabled }
        .mapNotNull { alarm -> nextTriggerTimeMillis(alarm, now)?.let { alarm to it } }
        .minByOrNull { it.second }
        ?: return RecentAlarmSummary(
            title = "모든 알람이 꺼진 상태입니다",
            time = "",
            dateTime = "",
            isOffState = true,
            isEmptyState = false
        )
    val (alarm, triggerAtMillis) = nextEnabled
    val diffMillis = (triggerAtMillis - now.timeInMillis).coerceAtLeast(0L)
    val diffMinute = (diffMillis / 60000L).toInt()
    val dayDiff = calendarDayDiff(now, triggerAtMillis)

    val hours = diffMinute / 60
    val mins = diffMinute % 60
    val remain = when {
        diffMinute == 0 -> "곧 알람이 울립니다"
        dayDiff > 0 -> "${dayDiff}일 후에 알람이 울립니다"
        hours == 0 -> "${mins}분 후에 알람이 울립니다"
        mins == 0 -> "${hours}시간 후에 알람이 울립니다"
        else -> "${hours}시간 ${mins}분 후에 알람이 울립니다"
    }

    val dateTime = SimpleDateFormat("M월 d일 (E) a h:mm", Locale.KOREAN).format(triggerAtMillis)

    val amPm = if (alarm.hour < 12) "오전" else "오후"
    val hour12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
    return RecentAlarmSummary(
        title = remain,
        time = "$amPm $hour12:${"%02d".format(alarm.minute)}",
        dateTime = dateTime,
        isOffState = false,
        isEmptyState = false
    )
}

private fun calendarDayDiff(now: Calendar, triggerAtMillis: Long): Int {
    val startOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val triggerDay = Calendar.getInstance().apply {
        timeInMillis = triggerAtMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return ((triggerDay.timeInMillis - startOfToday.timeInMillis) / (24 * 60 * 60 * 1000L)).toInt()
}

private data class RecentAlarmSummary(
    val title: String,
    val time: String,
    val dateTime: String,
    val isOffState: Boolean,
    val isEmptyState: Boolean
)

private fun nextTriggerTimeMillis(alarm: Alarm, now: Calendar = Calendar.getInstance()): Long? {
    fun fromBase(base: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = base }
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour)
        cal.set(Calendar.MINUTE, alarm.minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        while (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    if (alarm.specificDateMillis != null) return fromBase(alarm.specificDateMillis)

    val repeat = alarm.repeatWeekdays().toSet()
    if (repeat.isNotEmpty()) {
        val map = mapOf(
            Weekday.SUN to Calendar.SUNDAY,
            Weekday.MON to Calendar.MONDAY,
            Weekday.TUE to Calendar.TUESDAY,
            Weekday.WED to Calendar.WEDNESDAY,
            Weekday.THU to Calendar.THURSDAY,
            Weekday.FRI to Calendar.FRIDAY,
            Weekday.SAT to Calendar.SATURDAY
        )
        return repeat.mapNotNull { weekday ->
            val targetDow = map[weekday] ?: return@mapNotNull null
            val cal = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                set(Calendar.DAY_OF_WEEK, targetDow)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.WEEK_OF_YEAR, 1)
            }
            cal.timeInMillis
        }.minOrNull()
    }

    val cal = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
}

@Composable
private fun rememberAlarmDateLabel(alarm: Alarm): String {
    alarm.specificDateMillis?.let { millis ->
        return SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(millis)
    }

    val weekdayOrder = listOf(
        Weekday.SUN, Weekday.MON, Weekday.TUE, Weekday.WED,
        Weekday.THU, Weekday.FRI, Weekday.SAT
    )
    val repeatDays = alarm.repeatWeekdays().toSet()
    if (repeatDays.size == 7) return "매일"
    if (repeatDays.isNotEmpty()) {
        return weekdayOrder.filter { it in repeatDays }.joinToString(", ") { it.label }
    }

    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
    return SimpleDateFormat("M월 d일 (E)", Locale.KOREAN).format(cal.time)
}
