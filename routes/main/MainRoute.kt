package com.example.miram.routes.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miram.features.main.alarmdetail.AlarmDetailScreen
import com.example.miram.features.main.alarmringing.AlarmRingingScreen
import com.example.miram.features.main.home.HomeScreen
import com.example.miram.shared.alarm.AlarmStateHolder

sealed class MainDestination(val route: String) {
    data object Home : MainDestination("home")
    data object AlarmAdd : MainDestination("alarm/add")
    data class AlarmEdit(val alarmId: String) : MainDestination("alarm/edit/{alarmId}") {
        companion object { const val ROUTE = "alarm/edit/{alarmId}" }
    }
    data class AlarmRinging(val alarmId: String, val label: String, val ringDuration: Int) :
        MainDestination("alarm/ringing/{alarmId}?label={label}&ringDuration={ringDuration}") {
        companion object { const val ROUTE = "alarm/ringing/{alarmId}?label={label}&ringDuration={ringDuration}" }
    }
}

@Composable
fun MainRoute() {
    val navController = rememberNavController()

    // AlarmForegroundService가 startRinging()을 호출하면 즉시 AlarmRingingScreen으로 이동
    val ringingAlarm by AlarmStateHolder.ringingAlarm.collectAsState()
    LaunchedEffect(ringingAlarm) {
        val alarm = ringingAlarm ?: return@LaunchedEffect
        navController.navigate(
            "alarm/ringing/${alarm.alarmId}?label=${alarm.label}&ringDuration=${alarm.ringDuration}"
        ) {
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = MainDestination.Home.route) {
        composable(MainDestination.Home.route) {
            HomeScreen(
                onAddAlarm = { navController.navigate(MainDestination.AlarmAdd.route) },
                onEditAlarm = { id -> navController.navigate("alarm/edit/$id") }
            )
        }

        composable(MainDestination.AlarmAdd.route) {
            AlarmDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = MainDestination.AlarmEdit.ROUTE,
            arguments = listOf(navArgument("alarmId") { type = NavType.StringType })
        ) {
            AlarmDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = MainDestination.AlarmRinging.ROUTE,
            arguments = listOf(
                navArgument("alarmId") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType; defaultValue = "" },
                navArgument("ringDuration") { type = NavType.IntType; defaultValue = 60 }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId") ?: ""
            val label = backStackEntry.arguments?.getString("label") ?: ""
            val ringDuration = backStackEntry.arguments?.getInt("ringDuration") ?: 60
            AlarmRingingScreen(
                alarmId = alarmId,
                label = label,
                ringDuration = ringDuration,
                onDismiss = { navController.popBackStack(MainDestination.Home.route, inclusive = false) }
            )
        }
    }
}
