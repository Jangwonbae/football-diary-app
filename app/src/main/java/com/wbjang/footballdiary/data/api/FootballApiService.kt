package com.wbjang.footballdiary.data.api

import com.wbjang.footballdiary.data.api.dto.CompetitionTeamsResponse
import com.wbjang.footballdiary.data.api.dto.MatchDetailResponseDto
import com.wbjang.footballdiary.data.api.dto.TeamMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FootballApiService {

    @GET("v4/competitions/{leagueCode}/teams")
    suspend fun getTeamsByLeague(
        @Path("leagueCode") leagueCode: String
    ): CompetitionTeamsResponse

    @GET("v4/teams/{teamId}/matches")
    suspend fun getTeamMatches(
        @Path("teamId") teamId: Int,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): TeamMatchesResponse

    @GET("v4/matches/{matchId}")
    suspend fun getMatchDetail(
        @Path("matchId") matchId: Int
    ): MatchDetailResponseDto
}
