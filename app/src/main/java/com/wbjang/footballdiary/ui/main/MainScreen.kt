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
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wbjang.footballdiary.ui.main.schedule.MatchDetailScreen
import com.wbjang.footballdiary.ui.main.schedule.MatchDetailViewModel
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
import com.wbjang.footballdiary.ui.main.diary.WriteReviewScreen
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

private const val ROUTE_MATCH_DETAIL = "matchDetail"
private const val ROUTE_WRITE_REVIEW = "writeReview"

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != ROUTE_MATCH_DETAIL && currentRoute != ROUTE_WRITE_REVIEW

    val selectedMatch by viewModel.selectedMatch.collectAsStateWithLifecycle()
    val selectedMatchDetail by viewModel.selectedMatchDetail.collectAsStateWithLifecycle()
    val selectedReview by viewModel.selectedReview.collectAsStateWithLifecycle()
    val followingTeamId by viewModel.followingTeamId.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(navController = tabNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomNavItem.Schedule.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Schedule.route) {
                ScheduleScreen(
                    onMatchClick = { match ->
                        viewModel.selectMatch(match)
                        tabNavController.navigate(ROUTE_MATCH_DETAIL)
                    }
                )
            }
            composable(BottomNavItem.Diary.route) { DiaryScreen() }
            composable(BottomNavItem.Settings.route) { SettingsScreen() }
            composable(ROUTE_WRITE_REVIEW) {
                val match = selectedMatch
                if (match != null) {
                    WriteReviewScreen(
                        match = match,
                        matchDetail = selectedMatchDetail,
                        existingReview = selectedReview,
                        onBack = { tabNavController.popBackStack() }
                    )
                }
            }
            composable(ROUTE_MATCH_DETAIL) {
                val detailViewModel: MatchDetailViewModel = hiltViewModel()
                selectedMatch?.let { match ->
                    LaunchedEffect(match.id) {
                        detailViewModel.loadMatchDetail(match.id, match)
                    }
                    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(detailState.matchDetail) {
                        detailState.matchDetail?.let { viewModel.selectMatchDetail(it) }
                    }
                    MatchDetailScreen(
                        match = match,
                        followingTeamId = followingTeamId,
                        onBack = { tabNavController.popBackStack() },
                        onWriteReview = { existingReview ->
                            viewModel.selectReview(existingReview)
                            tabNavController.navigate(ROUTE_WRITE_REVIEW)
                        },
                        viewModel = detailViewModel
                    )
                }
            }
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
