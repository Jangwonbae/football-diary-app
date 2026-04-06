package com.wbjang.footballdiary.ui.main

import androidx.lifecycle.ViewModel
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: FootballRepository
) : ViewModel() {

    val followingTeamName: Flow<String?> = repository.getFollowingTeamName()
}
