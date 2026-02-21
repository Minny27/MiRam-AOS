package com.example.miram.routes.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miram.features.main.home.HomeScreen

sealed class MainDestination(val route: String) {
    data object Home : MainDestination("home")
}

@Composable
fun MainRoute() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainDestination.Home.route) {
        composable(MainDestination.Home.route) { HomeScreen() }
    }
}
