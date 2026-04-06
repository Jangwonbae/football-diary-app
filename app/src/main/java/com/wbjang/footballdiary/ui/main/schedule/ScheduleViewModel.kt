package com.wbjang.footballdiary.ui.main.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val isCalendarMode: Boolean = true,
    val followingTeamId: Int? = null,
    val followingTeamName: String? = null,
    val followingTeamCrestUrl: String? = null,
    val matches: List<Match> = emptyList(),
    val currentYearMonth: YearMonth = YearMonth.now(),
    val error: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFollowingTeamId().collect { teamId ->
                _uiState.update { it.copy(followingTeamId = teamId) }
                teamId?.let { loadMatches(it) }
            }
        }
        viewModelScope.launch {
            repository.getFollowingTeamName().collect { name ->
                _uiState.update { it.copy(followingTeamName = name) }
            }
        }
        viewModelScope.launch {
            repository.getFollowingTeamCrestUrl().collect { url ->
                _uiState.update { it.copy(followingTeamCrestUrl = url) }
            }
        }
    }

    private fun loadMatches(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // 현재 시즌 기준: 7월~다음 해 7월 범위 조회
            val now = YearMonth.now()
            val seasonStart = if (now.monthValue >= 7) now.year else now.year - 1
            val dateFrom = "${seasonStart}-07-01"
            val dateTo   = "${seasonStart + 1}-06-30"

            repository.getTeamMatches(teamId, dateFrom, dateTo)
                .onSuccess { matches ->
                    _uiState.update { it.copy(matches = matches, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isCalendarMode = !it.isCalendarMode) }
    }

    fun goToPreviousMonth() {
        _uiState.update { it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1)) }
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1)) }
    }
}
