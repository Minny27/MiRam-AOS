package com.example.miram.routes.main

import android.net.Uri
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

sealed interface MainDestination {
    val route: String

    data object Home : MainDestination {
        override val route = "home"
    }

    data object AlarmAdd : MainDestination {
        override val route = "alarm/add"
    }

    data object AlarmEdit : MainDestination {
        const val alarmIdArg = "alarmId"
        override val route = "alarm/edit/{$alarmIdArg}"

        fun createRoute(alarmId: String): String = "alarm/edit/$alarmId"
    }

    data object AlarmRinging : MainDestination {
        const val alarmIdArg = "alarmId"
        const val labelArg = "label"
        const val ringDurationArg = "ringDuration"
        override val route =
            "alarm/ringing/{$alarmIdArg}?$labelArg={$labelArg}&$ringDurationArg={$ringDurationArg}"

        fun createRoute(alarm: AlarmStateHolder.RingingAlarm): String {
            val encodedLabel = Uri.encode(alarm.label)
            return "alarm/ringing/${alarm.alarmId}?label=$encodedLabel&ringDuration=${alarm.ringDuration}"
        }
    }
}

@Composable
fun MainRoute() {
    val navController = rememberNavController()

    // AlarmForegroundService가 startRinging()을 호출하면 즉시 AlarmRingingScreen으로 이동
    val ringingAlarm by AlarmStateHolder.ringingAlarm.collectAsState()
    LaunchedEffect(ringingAlarm) {
        val alarm = ringingAlarm ?: return@LaunchedEffect
        navController.navigate(MainDestination.AlarmRinging.createRoute(alarm)) {
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = MainDestination.Home.route) {
        composable(MainDestination.Home.route) {
            HomeScreen(
                onAddAlarm = { navController.navigate(MainDestination.AlarmAdd.route) },
                onEditAlarm = { id -> navController.navigate(MainDestination.AlarmEdit.createRoute(id)) }
            )
        }

        composable(MainDestination.AlarmAdd.route) {
            AlarmDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = MainDestination.AlarmEdit.route,
            arguments = listOf(navArgument(MainDestination.AlarmEdit.alarmIdArg) { type = NavType.StringType })
        ) {
            AlarmDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = MainDestination.AlarmRinging.route,
            arguments = listOf(
                navArgument(MainDestination.AlarmRinging.alarmIdArg) { type = NavType.StringType },
                navArgument(MainDestination.AlarmRinging.labelArg) { type = NavType.StringType; defaultValue = "" },
                navArgument(MainDestination.AlarmRinging.ringDurationArg) { type = NavType.IntType; defaultValue = 60 }
            )
        ) { backStackEntry ->
            val alarmId =
                backStackEntry.arguments?.getString(MainDestination.AlarmRinging.alarmIdArg) ?: ""
            val label =
                backStackEntry.arguments?.getString(MainDestination.AlarmRinging.labelArg)?.let(Uri::decode)
                    ?: ""
            val ringDuration =
                backStackEntry.arguments?.getInt(MainDestination.AlarmRinging.ringDurationArg) ?: 60
            AlarmRingingScreen(
                alarmId = alarmId,
                label = label,
                ringDuration = ringDuration,
                onDismiss = { navController.popBackStack(MainDestination.Home.route, inclusive = false) }
            )
        }
    }
}
