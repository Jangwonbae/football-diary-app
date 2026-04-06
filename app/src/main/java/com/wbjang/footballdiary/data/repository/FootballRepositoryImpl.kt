package com.wbjang.footballdiary.data.repository

import com.wbjang.footballdiary.data.api.FootballApiService
import com.wbjang.footballdiary.data.datastore.UserPreferencesDataStore
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FootballRepositoryImpl @Inject constructor(
    private val apiService: FootballApiService,
    private val dataStore: UserPreferencesDataStore
) : FootballRepository {

    override suspend fun getTeamsByLeague(leagueCode: String): Result<List<Team>> {
        return runCatching {
            apiService.getTeamsByLeague(leagueCode).teams
                .sortedBy { it.name }
                .map { dto ->
                    Team(
                        id = dto.id,
                        name = dto.name,
                        shortName = dto.shortName,
                        crestUrl = dto.crest
                    )
                }
        }
    }

    override fun getFollowingTeamId(): Flow<Int?> = dataStore.followingTeamId
    override fun getFollowingTeamName(): Flow<String?> = dataStore.followingTeamName
    override fun getFollowingTeamCrestUrl(): Flow<String?> = dataStore.followingTeamCrestUrl

    override suspend fun saveFollowingTeam(teamId: Int, teamName: String, teamCrestUrl: String) {
        dataStore.saveFollowingTeam(teamId, teamName, teamCrestUrl)
    }

    override suspend fun getTeamMatches(
        teamId: Int,
        dateFrom: String,
        dateTo: String
    ): Result<List<Match>> {
        return runCatching {
            apiService.getTeamMatches(teamId, dateFrom, dateTo).matches
                .sortedBy { it.utcDate }
                .map { dto ->
                    Match(
                        id = dto.id,
                        utcDate = dto.utcDate,
                        status = MatchStatus.from(dto.status),
                        matchday = dto.matchday,
                        homeTeam = MatchTeam(
                            id = dto.homeTeam.id,
                            name = dto.homeTeam.name,
                            shortName = dto.homeTeam.shortName,
                            crestUrl = dto.homeTeam.crest
                        ),
                        awayTeam = MatchTeam(
                            id = dto.awayTeam.id,
                            name = dto.awayTeam.name,
                            shortName = dto.awayTeam.shortName,
                            crestUrl = dto.awayTeam.crest
                        ),
                        homeScore = dto.score.fullTime.home,
                        awayScore = dto.score.fullTime.away
                    )
                }
        }
    }
}
