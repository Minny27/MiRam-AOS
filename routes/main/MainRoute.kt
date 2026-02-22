package com.example.miram.routes.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miram.features.main.alarmdetail.AlarmDetailScreen
import com.example.miram.features.main.alarmringing.AlarmRingingScreen
import com.example.miram.features.main.home.HomeScreen

sealed class MainDestination(val route: String) {
    data object Home : MainDestination("home")
    data object AlarmAdd : MainDestination("alarm/add")
    data class AlarmEdit(val alarmId: String) : MainDestination("alarm/edit/{alarmId}") {
        companion object { const val ROUTE = "alarm/edit/{alarmId}" }
    }
    data class AlarmRinging(val alarmId: String, val label: String) :
        MainDestination("alarm/ringing/{alarmId}?label={label}") {
        companion object { const val ROUTE = "alarm/ringing/{alarmId}?label={label}" }
    }
}

@Composable
fun MainRoute(initialAlarmId: String? = null, initialAlarmLabel: String? = null) {
    val navController = rememberNavController()

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
                navArgument("label") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId") ?: ""
            val label = backStackEntry.arguments?.getString("label") ?: ""
            AlarmRingingScreen(
                alarmId = alarmId,
                label = label,
                onDismiss = { navController.popBackStack(MainDestination.Home.route, inclusive = false) }
            )
        }
    }

    // 외부 Intent로 알람 울림 화면 진입 처리
    if (initialAlarmId != null) {
        navController.navigate("alarm/ringing/$initialAlarmId?label=${initialAlarmLabel ?: ""}")
    }
}
