package com.example.miram.routes.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miram.features.auth.AuthScreen

sealed class AuthDestination(val route: String) {
    data object Login : AuthDestination("login")
    data object SignUp : AuthDestination("sign_up")
}

@Composable
fun AuthRoute() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthDestination.Login.route) {
        composable(AuthDestination.Login.route) {
            AuthScreen(onLoginSuccess = { navController.navigate(AuthDestination.SignUp.route) })
        }
        composable(AuthDestination.SignUp.route) {
            // SignUpScreen()
        }
    }
}
