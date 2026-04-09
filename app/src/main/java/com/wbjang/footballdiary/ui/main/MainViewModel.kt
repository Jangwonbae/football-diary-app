package com.wbjang.footballdiary.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.ui.navigation.Screen
import com.wbjang.footballdiary.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: FootballRepository
) : ViewModel() {

    val followingTeamId: Flow<Int?> = repository.getFollowingTeamId()
    val followingTeamName: Flow<String?> = repository.getFollowingTeamName()

    val themeMode: StateFlow<ThemeMode> = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val startDestination: String = runBlocking {
        if (repository.getFollowingTeamId().first() != null) Screen.Main.route
        else Screen.Onboarding.route
    }

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    private val _selectedMatchDetail = MutableStateFlow<MatchDetail?>(null)
    val selectedMatchDetail: StateFlow<MatchDetail?> = _selectedMatchDetail.asStateFlow()

    private val _selectedReview = MutableStateFlow<Review?>(null)
    val selectedReview: StateFlow<Review?> = _selectedReview.asStateFlow()

    fun selectMatch(match: Match) {
        _selectedMatch.value = match
        _selectedMatchDetail.value = null
        _selectedReview.value = null
    }

    fun selectMatchDetail(detail: MatchDetail) {
        _selectedMatchDetail.value = detail
    }

    fun selectReview(review: Review?) {
        _selectedReview.value = review
    }
}
