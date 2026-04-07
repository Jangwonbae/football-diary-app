package com.wbjang.footballdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.ui.navigation.NavGraph
import com.wbjang.footballdiary.ui.navigation.Screen
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme
import com.wbjang.footballdiary.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: FootballRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 팔로잉 팀이 설정되었는지 앱 시작 전에 확인 (splash 동안 결정)
        val hasFollowingTeam = runBlocking {
            repository.getFollowingTeamId().first() != null
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by repository.getThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            FootballDiaryTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = if (hasFollowingTeam) {
                        Screen.Main.route
                    } else {
                        Screen.Onboarding.route
                    }
                )
            }
        }
    }
}
