package com.seungmin.miram

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.seungmin.miram.routes.main.MainRoute
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAlarmWindowBehavior()
        enableEdgeToEdge()
        setContent {
            MiRamTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainRoute()
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
