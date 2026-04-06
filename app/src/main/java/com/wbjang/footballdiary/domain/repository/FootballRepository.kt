package com.wbjang.footballdiary.domain.repository

import com.wbjang.footballdiary.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface FootballRepository {
    suspend fun getTeamsByLeague(leagueCode: String): Result<List<Team>>
    fun getFollowingTeamId(): Flow<Int?>
    fun getFollowingTeamName(): Flow<String?>
    suspend fun saveFollowingTeam(teamId: Int, teamName: String)
}
