package com.seungmin.miram

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.seungmin.miram.routes.main.MainRoute
import com.seungmin.miram.shared.alarm.AlarmRuntimeRequirements
import com.seungmin.miram.shared.style.MiRamTheme
import com.seungmin.miram.shared.style.withFixedFontScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.withFixedFontScale())
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.fontScale = 1.0f
        super.applyOverrideConfiguration(overrideConfiguration?.withFixedFontScale())
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAlarmWindowBehavior()
        enableEdgeToEdge()
        setContent {
            var showFullScreenIntentPermissionDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                showFullScreenIntentPermissionDialog =
                    AlarmRuntimeRequirements.needsFullScreenIntentPermission(this@MainActivity)
            }

            MiRamTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainRoute()
                }

                if (showFullScreenIntentPermissionDialog) {
                    FullScreenIntentPermissionDialog(
                        onDismiss = {
                            showFullScreenIntentPermissionDialog = false
                        },
                        onOpenSettings = {
                            showFullScreenIntentPermissionDialog = false
                            val intent = AlarmRuntimeRequirements.specialAppAccessSettingsIntent()
                            val launcherIntent =
                                if (intent.resolveActivity(packageManager) != null) {
                                    intent
                                } else {
                                    AlarmRuntimeRequirements.settingsFallbackIntent()
                                }
                            startActivity(launcherIntent)
                        }
                    )
                }
            }
        }
    }

    // 앱이 실행 중일 때 fullScreenIntent 또는 알림 탭으로 진입하는 경우
    // AlarmStateHolder가 이미 설정되어 있으므로 MainRoute의 LaunchedEffect가 자동 처리
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyAlarmWindowBehavior()
    }

    private fun applyAlarmWindowBehavior() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}

@Composable
private fun FullScreenIntentPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알람이 울리지 않으시나요?") },
        text = {
            Text("잠금 화면이나 화면이 꺼진 상태에서도 알람 화면이 표시되려면 특수 앱 액세스에서 전체화면 알림 권한을 허용해야 합니다.")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("나중에")
            }
        }
    )
}
