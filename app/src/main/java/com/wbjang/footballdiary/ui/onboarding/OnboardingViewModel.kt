package com.wbjang.footballdiary.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.League
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val leagues: List<League> = initialLeagues(),
    val pendingTeam: Team? = null,        // 다이얼로그에 표시할 선택 팀
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun toggleLeagueExpand(leagueCode: String) {
        val league = _uiState.value.leagues.find { it.code == leagueCode } ?: return
        val willExpand = !league.isExpanded

        _uiState.update { state ->
            state.copy(
                leagues = state.leagues.map {
                    if (it.code == leagueCode) it.copy(isExpanded = willExpand) else it
                }
            )
        }

        if (willExpand && league.teams.isEmpty()) {
            loadTeams(leagueCode)
        }
    }

    private fun loadTeams(leagueCode: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    leagues = state.leagues.map {
                        if (it.code == leagueCode) it.copy(isLoading = true, error = null) else it
                    }
                )
            }

            repository.getTeamsByLeague(leagueCode)
                .onSuccess { teams ->
                    _uiState.update { state ->
                        state.copy(
                            leagues = state.leagues.map {
                                if (it.code == leagueCode) {
                                    it.copy(teams = teams, isLoading = false)
                                } else it
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            leagues = state.leagues.map {
                                if (it.code == leagueCode) {
                                    it.copy(isLoading = false, error = error.message)
                                } else it
                            }
                        )
                    }
                }
        }
    }

    fun onFollowingClick(team: Team) {
        _uiState.update { it.copy(pendingTeam = team) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(pendingTeam = null) }
    }

    fun confirmFollowing(onComplete: () -> Unit) {
        val team = _uiState.value.pendingTeam ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.saveFollowingTeam(team.id, team.name)
            _uiState.update { it.copy(isSaving = false, pendingTeam = null) }
            onComplete()
        }
    }
}

private fun initialLeagues(): List<League> = listOf(
    League(
        code = "PL",
        name = "프리미어리그",
        country = "잉글랜드",
        emblemUrl = "https://crests.football-data.org/PL.png"
    ),
    League(
        code = "PD",
        name = "라리가",
        country = "스페인",
        emblemUrl = "https://crests.football-data.org/PD.png"
    ),
    League(
        code = "BL1",
        name = "분데스리가",
        country = "독일",
        emblemUrl = "https://crests.football-data.org/BL1.png"
    ),
    League(
        code = "SA",
        name = "세리에 A",
        country = "이탈리아",
        emblemUrl = "https://crests.football-data.org/SA.png"
    ),
    League(
        code = "FL1",
        name = "리그 1",
        country = "프랑스",
        emblemUrl = "https://crests.football-data.org/FL1.png"
    )
)
