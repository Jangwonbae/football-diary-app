package com.wbjang.footballdiary.ui.main.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.domain.model.ThemeMode
import com.wbjang.footballdiary.notification.MatchNotificationWorker
import com.wbjang.footballdiary.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FootballRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val followingTeamName: StateFlow<String?> = repository.getFollowingTeamName()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<ThemeMode> = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val notificationEnabled: StateFlow<Boolean> = repository.getNotificationEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.saveThemeMode(mode) }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        AppLogger.d(TAG, "알림 설정 변경: $enabled")
        viewModelScope.launch {
            repository.saveNotificationEnabled(enabled)
            if (enabled) scheduleNotificationWork() else cancelNotificationWork()
        }
    }

    private fun scheduleNotificationWork() {
        AppLogger.d(TAG, "알림 Worker 스케줄 등록")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<MatchNotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelNotificationWork() {
        AppLogger.d(TAG, "알림 Worker 취소")
        WorkManager.getInstance(context).cancelUniqueWork(NOTIFICATION_WORK_NAME)
    }

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val NOTIFICATION_WORK_NAME = "match_notification_work"
    }
}
