package com.example.exchangenotifier.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.exchangenotifier.ui.main.MainScreen
import com.example.exchangenotifier.ui.settings.SettingsScreen

private const val ROUTE_MAIN     = "main"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainScreen(onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) })
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
