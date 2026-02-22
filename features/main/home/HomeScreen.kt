package com.example.miram.features.main.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.miram.shared.model.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddAlarm: () -> Unit = {},
    onEditAlarm: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("알람") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarm) {
                Icon(Icons.Default.Add, contentDescription = "알람 추가")
            }
        }
    ) { innerPadding ->
        if (uiState.alarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("알람이 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.alarms, key = { it.id }) { alarm ->
                    AlarmItem(
                        alarm = alarm,
                        onToggle = { viewModel.toggleEnabled(alarm) },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onClick = { onEditAlarm(alarm.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AlarmItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val weekdayLabels = alarm.repeatWeekdays().joinToString(" ") { it.label }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val timeColor = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.twelveHourTimeString,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Thin,
                    color = timeColor
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = alarm.amPm,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = timeColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                text = alarm.label.ifBlank { "알람" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (alarm.label.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            if (weekdayLabels.isNotBlank()) {
                Text(
                    weekdayLabels,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
    }
}
