package com.wbjang.footballdiary.data.repository

import com.wbjang.footballdiary.data.api.FootballApiService
import com.wbjang.footballdiary.data.datastore.UserPreferencesDataStore
import com.wbjang.footballdiary.data.local.dao.ReviewDao
import com.wbjang.footballdiary.data.local.entity.ReviewEntity
import com.google.gson.Gson
import com.wbjang.footballdiary.data.mapper.toMatchDomain
import com.wbjang.footballdiary.data.mapper.toDomain
import com.wbjang.footballdiary.domain.model.BookingEvent
import com.wbjang.footballdiary.domain.model.CardType
import com.wbjang.footballdiary.domain.model.GoalEvent
import com.wbjang.footballdiary.domain.model.GoalType
import com.wbjang.footballdiary.domain.model.LineupPlayer
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.model.StandingEntry
import com.wbjang.footballdiary.domain.model.StandingTeam
import com.wbjang.footballdiary.domain.model.SubstitutionEvent
import com.wbjang.footballdiary.domain.model.ThemeMode
import com.wbjang.footballdiary.domain.model.TeamLineup
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FootballRepositoryImpl @Inject constructor(
    private val apiService: FootballApiService,
    private val dataStore: UserPreferencesDataStore,
    private val reviewDao: ReviewDao
) : FootballRepository {

    override suspend fun getTeamsByLeague(leagueCode: String): Result<List<Team>> {
        AppLogger.d(TAG, "getTeamsByLeague: leagueCode=$leagueCode")
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
                .also { AppLogger.d(TAG, "getTeamsByLeague 성공: ${it.size}개 팀") }
        }.onFailure { AppLogger.e(TAG, "getTeamsByLeague 실패", it) }
    }

    override fun getFollowingTeamId(): Flow<Int?> = dataStore.followingTeamId
    override fun getFollowingTeamName(): Flow<String?> = dataStore.followingTeamName
    override fun getFollowingTeamCrestUrl(): Flow<String?> = dataStore.followingTeamCrestUrl

    override suspend fun saveFollowingTeam(teamId: Int, teamName: String, teamCrestUrl: String) {
        dataStore.saveFollowingTeam(teamId, teamName, teamCrestUrl)
    }

    override suspend fun getStandings(competitionId: Int): Result<List<StandingEntry>> {
        AppLogger.d(TAG, "getStandings: competitionId=$competitionId")
        return runCatching {
            val totalTable = apiService.getStandings(competitionId)
                .standings
                .firstOrNull { it.type == "TOTAL" }
                ?.table ?: emptyList()
            totalTable.map { dto ->
                StandingEntry(
                    position = dto.position,
                    team = StandingTeam(
                        id = dto.team.id,
                        name = dto.team.name,
                        shortName = dto.team.shortName,
                        crestUrl = dto.team.crest
                    ),
                    playedGames = dto.playedGames,
                    won = dto.won,
                    draw = dto.draw,
                    lost = dto.lost,
                    goalsFor = dto.goalsFor,
                    goalsAgainst = dto.goalsAgainst,
                    goalDifference = dto.goalDifference,
                    points = dto.points
                )
            }.also { AppLogger.d(TAG, "getStandings 성공: ${it.size}개 팀") }
        }.onFailure { AppLogger.e(TAG, "getStandings 실패", it) }
    }

    override suspend fun getMatchDetail(matchId: Int): Result<MatchDetail> {
        AppLogger.d(TAG, "getMatchDetail: matchId=$matchId")
        return runCatching {
            val dto = apiService.getMatchDetail(matchId)
            val seasonLabel = run {
                val startYear = dto.season?.startDate?.take(4)
                val endYear = dto.season?.endDate?.take(4)
                if (startYear != null && endYear != null && startYear != endYear)
                    "$startYear/$endYear"
                else
                    startYear
            }
            MatchDetail(
                match = dto.toMatchDomain(),
                seasonLabel = seasonLabel,
                venue = dto.venue,
                attendance = dto.attendance,
                goals = dto.goals.orEmpty().mapNotNull { g ->
                    val minute = g.minute ?: return@mapNotNull null
                    val scorerName = g.scorer?.name ?: return@mapNotNull null
                    GoalEvent(
                        minute = minute,
                        injuryTime = g.injuryTime,
                        type = when (g.type) {
                            "OWN_GOAL" -> GoalType.OWN_GOAL
                            "PENALTY"  -> GoalType.PENALTY
                            else       -> GoalType.REGULAR
                        },
                        teamId = g.team?.id,
                        scorerName = scorerName,
                        assistName = g.assist?.name
                    )
                },
                bookings = dto.bookings.orEmpty().mapNotNull { b ->
                    val minute = b.minute ?: return@mapNotNull null
                    val playerName = b.player?.name ?: return@mapNotNull null
                    BookingEvent(
                        minute = minute,
                        teamId = b.team?.id,
                        playerName = playerName,
                        card = when (b.card) {
                            "RED"         -> CardType.RED
                            "YELLOW_RED"  -> CardType.YELLOW_RED
                            else          -> CardType.YELLOW
                        }
                    )
                },
                substitutions = dto.substitutions.orEmpty().mapNotNull { s ->
                    val minute = s.minute ?: return@mapNotNull null
                    val playerOutName = s.playerOut?.name ?: return@mapNotNull null
                    val playerInName = s.playerIn?.name ?: return@mapNotNull null
                    SubstitutionEvent(
                        minute = minute,
                        teamId = s.team?.id,
                        playerOutName = playerOutName,
                        playerInName = playerInName
                    )
                },
                lineups = dto.lineup.orEmpty().mapNotNull { l ->
                    val teamId = l.id ?: return@mapNotNull null
                    val teamName = l.name ?: return@mapNotNull null
                    TeamLineup(
                        teamId = teamId,
                        teamName = teamName,
                        formation = l.formation,
                        startingEleven = l.startXI.orEmpty().mapNotNull { p ->
                            val name = p.player?.name ?: return@mapNotNull null
                            LineupPlayer(name = name, position = p.position, shirtNumber = p.shirtNumber)
                        },
                        bench = l.bench.orEmpty().mapNotNull { p ->
                            val name = p.player?.name ?: return@mapNotNull null
                            LineupPlayer(name = name, position = p.position, shirtNumber = p.shirtNumber)
                        }
                    )
                }
            )
        }.onSuccess { AppLogger.d(TAG, "getMatchDetail 성공: matchId=$matchId") }
         .onFailure { AppLogger.e(TAG, "getMatchDetail 실패: matchId=$matchId", it) }
    }

    override suspend fun saveReview(review: Review) {
        AppLogger.d(TAG, "saveReview: matchId=${review.matchId}")
        reviewDao.insertReview(
            ReviewEntity(
                id = review.id,
                matchId = review.matchId,
                utcDate = review.utcDate,
                homeTeamId = review.homeTeamId,
                homeTeamName = review.homeTeamName,
                homeTeamShortName = review.homeTeamShortName,
                homeTeamCrestUrl = review.homeTeamCrestUrl,
                awayTeamId = review.awayTeamId,
                awayTeamName = review.awayTeamName,
                awayTeamShortName = review.awayTeamShortName,
                awayTeamCrestUrl = review.awayTeamCrestUrl,
                homeScore = review.homeScore,
                awayScore = review.awayScore,
                matchday = review.matchday,
                competition = review.competition,
                competitionEmblemUrl = review.competitionEmblemUrl,
                venue = review.venue,
                seasonLabel = review.seasonLabel,
                rating = review.rating,
                emotionTags = Gson().toJson(review.emotionTags),
                content = review.content,
                followingTeamId = review.followingTeamId
            )
        )
    }

    override fun getReviewByMatchId(matchId: Int): Flow<Review?> =
        reviewDao.getReviewByMatchId(matchId).map { it?.toDomain() }

    override fun getAllReviews(): Flow<List<Review>> =
        reviewDao.getAllReviews().map { list -> list.map { it.toDomain() } }

    override suspend fun deleteReview(matchId: Int) {
        reviewDao.deleteReviewByMatchId(matchId)
    }

    override suspend fun updateReviewScore(matchId: Int, homeScore: Int, awayScore: Int) {
        reviewDao.updateScore(matchId, homeScore, awayScore)
    }

    override fun getThemeMode(): Flow<ThemeMode> = dataStore.themeMode

    override fun getNotificationEnabled(): Flow<Boolean> = dataStore.notificationEnabled
    override suspend fun saveNotificationEnabled(enabled: Boolean) {
        dataStore.saveNotificationEnabled(enabled)
    }
    override fun getLastNotifiedMatchId(): Flow<Int?> = dataStore.lastNotifiedMatchId
    override suspend fun saveLastNotifiedMatchId(matchId: Int) {
        dataStore.saveLastNotifiedMatchId(matchId)
    }
    override suspend fun saveThemeMode(mode: ThemeMode) = dataStore.saveThemeMode(mode)

    private fun ReviewEntity.toDomain() = Review(
        id = id,
        matchId = matchId,
        utcDate = utcDate,
        homeTeamId = homeTeamId,
        homeTeamName = homeTeamName,
        homeTeamShortName = homeTeamShortName,
        homeTeamCrestUrl = homeTeamCrestUrl,
        awayTeamId = awayTeamId,
        awayTeamName = awayTeamName,
        awayTeamShortName = awayTeamShortName,
        awayTeamCrestUrl = awayTeamCrestUrl,
        homeScore = homeScore,
        awayScore = awayScore,
        matchday = matchday,
        competition = competition,
        competitionEmblemUrl = competitionEmblemUrl,
        venue = venue,
        seasonLabel = seasonLabel,
        rating = rating,
        emotionTags = when {
            emotionTags.isBlank()       -> emptyList()
            emotionTags.startsWith("[") -> Gson().fromJson(emotionTags, Array<String>::class.java).toList()
            else                        -> emptyList()
        },
        content = content,
        followingTeamId = followingTeamId,
        createdAt = createdAt
    )

    override suspend fun getTeamMatches(
        teamId: Int,
        dateFrom: String,
        dateTo: String
    ): Result<List<Match>> {
        AppLogger.d(TAG, "getTeamMatches: teamId=$teamId, $dateFrom ~ $dateTo")
        return runCatching {
            apiService.getTeamMatches(teamId, dateFrom, dateTo).matches
                .sortedBy { it.utcDate }
                .map { it.toDomain() }
                .also { AppLogger.d(TAG, "getTeamMatches 성공: ${it.size}개 경기") }
        }.onFailure { AppLogger.e(TAG, "getTeamMatches 실패: teamId=$teamId", it) }
    }

    companion object {
        private const val TAG = "FootballRepository"
    }
}
