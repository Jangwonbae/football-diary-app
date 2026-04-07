package com.wbjang.footballdiary.ui.main.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    repository: FootballRepository
) : ViewModel() {

    val reviews: StateFlow<List<Review>> = repository.getAllReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followingTeamId: StateFlow<Int?> = repository.getFollowingTeamId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
