package com.wbjang.footballdiary.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.domain.model.ThemeMode
import com.wbjang.footballdiary.notification.NotificationScheduler
import com.wbjang.footballdiary.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FootballRepository,
    private val notificationScheduler: NotificationScheduler
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
            notificationScheduler.refreshScheduling()
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
