package com.wbjang.footballdiary.ui.main.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class ReviewSortOrder { MATCH_DATE, WRITTEN_DATE }

@HiltViewModel
class DiaryViewModel @Inject constructor(
    repository: FootballRepository
) : ViewModel() {

    val sortOrder = MutableStateFlow(ReviewSortOrder.MATCH_DATE)
    val selectedSeason = MutableStateFlow<String?>(null) // null = 전체

    val followingTeamId: StateFlow<Int?> = repository.getFollowingTeamId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allReviews: StateFlow<List<Review>> = repository.getAllReviews()
        .combine(followingTeamId) { list, teamId ->
            if (teamId == null) list
            else list.filter { it.homeTeamId == teamId || it.awayTeamId == teamId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 작성된 소감에서 시즌 목록 추출 (최신 시즌 순)
    val availableSeasons: StateFlow<List<String>> = allReviews
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.mapNotNull { it.seasonLabel }
                .distinct()
                .sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<Review>> = allReviews
        .combine(selectedSeason) { list, season ->
            if (season == null) list else list.filter { it.seasonLabel == season }
        }
        .combine(sortOrder) { list, order ->
            when (order) {
                ReviewSortOrder.MATCH_DATE   -> list.sortedByDescending { it.utcDate }
                ReviewSortOrder.WRITTEN_DATE -> list.sortedByDescending { it.createdAt }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: ReviewSortOrder) {
        sortOrder.value = order
    }

    fun setSelectedSeason(season: String?) {
        selectedSeason.value = season
    }
}
