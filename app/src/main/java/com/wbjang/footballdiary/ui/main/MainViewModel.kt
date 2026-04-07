package com.wbjang.footballdiary.ui.main

import androidx.lifecycle.ViewModel
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: FootballRepository
) : ViewModel() {

    val followingTeamId: Flow<Int?> = repository.getFollowingTeamId()
    val followingTeamName: Flow<String?> = repository.getFollowingTeamName()

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
