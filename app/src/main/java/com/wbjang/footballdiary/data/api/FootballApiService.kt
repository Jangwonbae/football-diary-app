package com.wbjang.footballdiary.data.api

import com.wbjang.footballdiary.data.api.dto.CompetitionTeamsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FootballApiService {

    @GET("v4/competitions/{leagueCode}/teams")
    suspend fun getTeamsByLeague(
        @Path("leagueCode") leagueCode: String
    ): CompetitionTeamsResponse
}
