package com.example.miram

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.miram.features.splash.SplashScreen
import com.example.miram.routes.auth.AuthRoute
import com.example.miram.routes.main.MainRoute
//import com.example.miram.shared.style.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            AppTheme {
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    SplashScreen(
//                        onAuthRequired = { AuthRoute() },
//                        onAuthenticated = { MainRoute() }
//                    )
//                }
//            }
        }
    }

    // 앱이 실행 중일 때 fullScreenIntent 또는 알림 탭으로 진입하는 경우
    // AlarmStateHolder가 이미 설정되어 있으므로 MainRoute의 LaunchedEffect가 자동 처리
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
