package com.wbjang.footballdiary.domain.repository

import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.model.StandingEntry
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface FootballRepository {
    // 온보딩 - 리그 팀 목록
    suspend fun getTeamsByLeague(leagueCode: String): Result<List<Team>>

    // 팔로잉 팀 저장 / 조회
    fun getFollowingTeamId(): Flow<Int?>
    fun getFollowingTeamName(): Flow<String?>
    fun getFollowingTeamCrestUrl(): Flow<String?>
    suspend fun saveFollowingTeam(teamId: Int, teamName: String, teamCrestUrl: String)

    // 경기 일정
    suspend fun getTeamMatches(teamId: Int, dateFrom: String, dateTo: String): Result<List<Match>>

    // 순위표
    suspend fun getStandings(competitionId: Int): Result<List<StandingEntry>>

    // 경기 상세
    suspend fun getMatchDetail(matchId: Int): Result<MatchDetail>

    // 소감
    suspend fun saveReview(review: Review)
    fun getReviewByMatchId(matchId: Int): Flow<Review?>
    fun getAllReviews(): Flow<List<Review>>
    suspend fun deleteReview(matchId: Int)

    // 테마
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun saveThemeMode(mode: ThemeMode)
}
