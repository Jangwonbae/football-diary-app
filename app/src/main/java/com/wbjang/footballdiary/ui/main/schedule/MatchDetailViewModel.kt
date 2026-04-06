package com.wbjang.footballdiary.ui.main.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SampleSection { VENUE, TIMELINE, LINEUP, STATISTICS }

data class MatchDetailUiState(
    val isLoading: Boolean = false,
    val matchDetail: MatchDetail? = null,
    val sampleSections: Set<SampleSection> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    fun loadMatchDetail(matchId: Int, match: Match) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val apiDetail = repository.getMatchDetail(matchId).getOrNull()
            val sampleSections = mutableSetOf(SampleSection.STATISTICS) // 통계는 항상 샘플

            // 경기장 / 관중 — API 미제공 시 샘플
            val venue = apiDetail?.venue ?: run {
                sampleSections.add(SampleSection.VENUE)
                SampleMatchData.SAMPLE_VENUE
            }
            val attendance = apiDetail?.attendance ?: SampleMatchData.SAMPLE_ATTENDANCE

            // 타임라인 — API 미제공 또는 종료 경기에 빈 데이터 시 샘플
            val hasApiTimeline = apiDetail != null &&
                (apiDetail.goals.isNotEmpty() || apiDetail.bookings.isNotEmpty() || apiDetail.substitutions.isNotEmpty())
            val goals: List<com.wbjang.footballdiary.domain.model.GoalEvent>
            val bookings: List<com.wbjang.footballdiary.domain.model.BookingEvent>
            val substitutions: List<com.wbjang.footballdiary.domain.model.SubstitutionEvent>
            if (hasApiTimeline) {
                goals         = apiDetail!!.goals
                bookings      = apiDetail.bookings
                substitutions = apiDetail.substitutions
            } else {
                sampleSections.add(SampleSection.TIMELINE)
                goals         = if (match.isFinished()) SampleMatchData.sampleGoals(match) else emptyList()
                bookings      = if (match.isFinished()) SampleMatchData.sampleBookings(match) else emptyList()
                substitutions = if (match.isFinished()) SampleMatchData.sampleSubstitutions(match) else emptyList()
            }

            // 라인업 — API 미제공 시 샘플
            val lineups = apiDetail?.lineups?.takeIf { it.isNotEmpty() } ?: run {
                sampleSections.add(SampleSection.LINEUP)
                SampleMatchData.sampleLineups(match)
            }

            val detail = MatchDetail(
                match         = apiDetail?.match ?: match,
                venue         = venue,
                attendance    = attendance,
                goals         = goals,
                bookings      = bookings,
                substitutions = substitutions,
                lineups       = lineups
            )

            _uiState.update {
                it.copy(
                    matchDetail    = detail,
                    sampleSections = sampleSections,
                    isLoading      = false
                )
            }
        }
    }
}
