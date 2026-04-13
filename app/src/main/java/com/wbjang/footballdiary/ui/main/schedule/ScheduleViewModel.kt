package com.wbjang.footballdiary.ui.main.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.StandingEntry
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ScheduleTab { SCHEDULE, STANDINGS }

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val isCalendarMode: Boolean = false,
    val followingTeamId: Int? = null,
    val followingTeamName: String? = null,
    val followingTeamCrestUrl: String? = null,
    val matches: List<Match> = emptyList(),
    val currentYearMonth: YearMonth = YearMonth.now(),
    val error: String? = null,
    val selectedTab: ScheduleTab = ScheduleTab.SCHEDULE,
    val standings: List<StandingEntry> = emptyList(),
    val standingsLoading: Boolean = false,
    val standingsError: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    var hasScrolledToUpcoming = false
        private set
    val reviewedMatchIds: StateFlow<Set<Int>> = repository.getAllReviews()
        .map { reviews -> reviews.map { it.matchId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        // 팀 정보 UI 업데이트 — 세 값 통합 구독
        viewModelScope.launch {
            combine(
                repository.getFollowingTeamId(),
                repository.getFollowingTeamName(),
                repository.getFollowingTeamCrestUrl()
            ) { id, name, url -> Triple(id, name, url) }
                .collect { (id, name, url) ->
                    _uiState.update {
                        it.copy(
                            followingTeamId = id,
                            followingTeamName = name,
                            followingTeamCrestUrl = url
                        )
                    }
                }
        }

        // 경기 로드 — teamId 실제 변경 시에만 API 호출
        viewModelScope.launch {
            repository.getFollowingTeamId()
                .distinctUntilChanged()
                .collect { teamId -> teamId?.let { loadMatches(it) } }
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
                    if (_uiState.value.selectedTab == ScheduleTab.STANDINGS && _uiState.value.standings.isEmpty()) {
                        loadStandings(matches)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun selectTab(tab: ScheduleTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == ScheduleTab.STANDINGS && _uiState.value.standings.isEmpty() && !_uiState.value.standingsLoading) {
            loadStandings(_uiState.value.matches)
        }
    }

    private fun loadStandings(matches: List<Match>) {
        val competitionId = matches
            .groupBy { it.competition?.id }
            .filterKeys { it != null }
            .maxByOrNull { it.value.size }
            ?.key ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(standingsLoading = true, standingsError = null) }
            repository.getStandings(competitionId)
                .onSuccess { standings ->
                    _uiState.update { it.copy(standings = standings, standingsLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(standingsLoading = false, standingsError = e.message) }
                }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isCalendarMode = !it.isCalendarMode) }
    }

    fun resetToCurrentMonth() {
        _uiState.update { it.copy(currentYearMonth = YearMonth.now()) }
    }

    fun goToPreviousMonth() {
        _uiState.update { it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1)) }
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1)) }
    }
    fun markScrolled() { hasScrolledToUpcoming = true }

    fun setYearMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentYearMonth = yearMonth) }
    }
}
