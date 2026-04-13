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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

enum class ReviewSortField { MATCH_DATE, WRITTEN_DATE }
enum class ReviewSortDirection { DESC, ASC }

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    val sortField = MutableStateFlow(ReviewSortField.MATCH_DATE)
    val sortDirection = MutableStateFlow(ReviewSortDirection.DESC)
    val selectedSeason = MutableStateFlow<String?>(null) // null = 전체

    val followingTeamId: StateFlow<Int?> = repository.getFollowingTeamId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allReviews: StateFlow<List<Review>> = repository.getAllReviews()
        .combine(followingTeamId) { list, teamId ->
            if (teamId == null) list
            else list.filter { it.followingTeamId == teamId }
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
        .combine(sortField.combine(sortDirection) { f, d -> f to d }) { list, (field, dir) ->
            when {
                field == ReviewSortField.MATCH_DATE   && dir == ReviewSortDirection.DESC -> list.sortedByDescending { it.utcDate }
                field == ReviewSortField.MATCH_DATE   && dir == ReviewSortDirection.ASC  -> list.sortedBy { it.utcDate }
                field == ReviewSortField.WRITTEN_DATE && dir == ReviewSortDirection.DESC -> list.sortedByDescending { it.createdAt }
                else                                                                     -> list.sortedBy { it.createdAt }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // StateFlow 초기값(emptyList)을 건너뛰고 DB 첫 로드 결과를 사용
            allReviews.drop(1).first()
                .filter { it.homeScore == null && it.awayScore == null }
                .filter { Instant.parse(it.utcDate).isBefore(Instant.now()) }
                .forEach { review ->
                    launch {
                        repository.getMatchDetail(review.matchId)
                            .getOrNull()?.match
                            ?.let { match ->
                                if (!match.isFinished()) return@let
                                val home = match.homeScore ?: return@let
                                val away = match.awayScore ?: return@let
                                repository.updateReviewScore(review.matchId, home, away)
                            }
                    }
                }
        }
    }

    fun setSortField(field: ReviewSortField) { sortField.value = field }
    fun setSortDirection(dir: ReviewSortDirection) { sortDirection.value = dir }

    fun setSelectedSeason(season: String?) {
        selectedSeason.value = season
    }
}
