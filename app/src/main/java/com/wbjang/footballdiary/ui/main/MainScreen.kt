package com.wbjang.footballdiary.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.dimensionResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.ui.main.diary.DiaryScreen
import com.wbjang.footballdiary.ui.main.schedule.ScheduleScreen
import com.wbjang.footballdiary.ui.main.settings.SettingsScreen
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Schedule(
        route = "schedule",
        labelRes = R.string.tab_schedule,
        icon = Icons.Filled.CalendarMonth
    ),
    Diary(
        route = "diary",
        labelRes = R.string.tab_diary,
        icon = Icons.Filled.EditNote
    ),
    Settings(
        route = "settings",
        labelRes = R.string.tab_settings,
        icon = Icons.Filled.Settings
    )
}

@Composable
fun MainScreen() {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = { BottomNavigationBar(navController = tabNavController) }
    ) { paddingValues ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomNavItem.Schedule.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Schedule.route) { ScheduleScreen() }
            composable(BottomNavItem.Diary.route) { DiaryScreen() }
            composable(BottomNavItem.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        windowInsets = WindowInsets(0),
        modifier = Modifier.height(dimensionResource(R.dimen.bottom_nav_height))
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(text = stringResource(item.labelRes)) }
            )
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun PreviewBottomNavigationBar() {
    val navController = rememberNavController()
    FootballDiaryTheme {
        BottomNavigationBar(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    FootballDiaryTheme {
        MainScreen()
    }
}
