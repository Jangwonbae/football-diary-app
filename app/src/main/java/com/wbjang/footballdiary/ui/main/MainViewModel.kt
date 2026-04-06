package com.wbjang.footballdiary.ui.main

import androidx.lifecycle.ViewModel
import com.wbjang.footballdiary.domain.model.Match
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

    fun selectMatch(match: Match) {
        _selectedMatch.value = match
    }
}
