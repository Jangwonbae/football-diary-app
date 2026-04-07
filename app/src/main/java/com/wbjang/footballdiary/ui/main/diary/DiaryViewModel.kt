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

    val reviews: StateFlow<List<Review>> = repository.getAllReviews()
        .combine(sortOrder) { list, order ->
            when (order) {
                ReviewSortOrder.MATCH_DATE   -> list.sortedByDescending { it.utcDate }
                ReviewSortOrder.WRITTEN_DATE -> list.sortedByDescending { it.createdAt }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followingTeamId: StateFlow<Int?> = repository.getFollowingTeamId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSortOrder(order: ReviewSortOrder) {
        sortOrder.value = order
    }
}
