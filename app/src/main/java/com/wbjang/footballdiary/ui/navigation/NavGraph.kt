package com.wbjang.footballdiary.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wbjang.footballdiary.ui.main.MainScreen
import com.wbjang.footballdiary.ui.onboarding.OnboardingScreen
import com.wbjang.footballdiary.ui.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String?
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.navigationBarsPadding()
    ) {
        composable(Screen.Splash.route) {
            LaunchedEffect(startDestination) {
                if (startDestination != null) {
                    navController.navigate(startDestination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            SplashScreen()
        }

        composable(Screen.Onboarding.route) {
            val canGoBack = navController.previousBackStackEntry != null
            OnboardingScreen(
                onTeamSelected = {
                    if (canGoBack) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
    }
}
