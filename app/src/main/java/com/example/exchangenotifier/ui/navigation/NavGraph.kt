package com.example.exchangenotifier.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.exchangenotifier.ui.main.MainScreen
import com.example.exchangenotifier.ui.settings.SettingsScreen

private const val ROUTE_MAIN     = "main"
private const val ROUTE_SETTINGS = "settings"
private const val ANIM_MS        = 350

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController    = navController,
        startDestination = ROUTE_MAIN,
        enterTransition  = { fadeIn(tween(ANIM_MS)) },
        exitTransition   = { fadeOut(tween(ANIM_MS)) },
        popEnterTransition  = { fadeIn(tween(ANIM_MS)) },
        popExitTransition   = { fadeOut(tween(ANIM_MS)) },
    ) {
        composable(
            route = ROUTE_MAIN,
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_MS))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_MS))
            },
        ) {
            MainScreen(onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) })
        }

        composable(
            route = ROUTE_SETTINGS,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_MS))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_MS))
            },
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
