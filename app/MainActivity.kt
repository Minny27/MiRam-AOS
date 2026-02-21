package com.example.miram

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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                SplashScreen(
                    onAuthRequired = { AuthRoute() },
                    onAuthenticated = { MainRoute() }
                )
            }
        }
    }
}
