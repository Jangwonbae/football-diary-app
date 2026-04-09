package com.wbjang.footballdiary.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wbjang.footballdiary.ui.main.MainScreen
import com.wbjang.footballdiary.ui.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.navigationBarsPadding()
    ) {
        composable(Screen.Onboarding.route) {
            val canGoBack = navController.previousBackStackEntry != null
            OnboardingScreen(
                onTeamSelected = {
                    if (canGoBack) {
                        // Settings에서 진입한 경우 → 뒤로 돌아가기
                        navController.popBackStack()
                    } else {
                        // 최초 온보딩 → Main으로 이동하며 Onboarding 제거
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
                    // 백스택 유지 (Main이 남아있어야 Settings로 돌아올 수 있음)
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
    }
}
