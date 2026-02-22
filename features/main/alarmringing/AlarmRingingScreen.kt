package com.example.miram.features.main.alarmringing

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AlarmRingingScreen(
    alarmId: String,
    label: String,
    ringDuration: Int = 0,
    onDismiss: () -> Unit,
    viewModel: AlarmRingingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(alarmId) {
        viewModel.initialize(ringDuration = ringDuration)
    }

    LaunchedEffect(uiState.isDismissed) {
        if (uiState.isDismissed) onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Default.Alarm,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            if (label.isNotBlank()) {
                Text(label, style = MaterialTheme.typography.headlineMedium)
            }

            if (uiState.totalSeconds > 0) {
                Text(
                    text = uiState.remainingLabel,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Thin
                )
                LinearProgressIndicator(
                    progress = { uiState.progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.dismiss() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("알람 해제", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
