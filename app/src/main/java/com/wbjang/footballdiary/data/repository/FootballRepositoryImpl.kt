package com.wbjang.footballdiary.data.repository

import com.wbjang.footballdiary.data.api.FootballApiService
import com.wbjang.footballdiary.data.datastore.UserPreferencesDataStore
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

    override suspend fun saveFollowingTeam(teamId: Int, teamName: String) {
        dataStore.saveFollowingTeam(teamId, teamName)
    }
}
