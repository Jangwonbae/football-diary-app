package com.wbjang.footballdiary.data.repository

import com.wbjang.footballdiary.data.api.FootballApiService
import com.wbjang.footballdiary.data.datastore.UserPreferencesDataStore
import com.wbjang.footballdiary.data.local.dao.ReviewDao
import com.wbjang.footballdiary.data.local.entity.ReviewEntity
import com.wbjang.footballdiary.domain.model.BookingEvent
import com.wbjang.footballdiary.domain.model.CardType
import com.wbjang.footballdiary.domain.model.GoalEvent
import com.wbjang.footballdiary.domain.model.GoalType
import com.wbjang.footballdiary.domain.model.LineupPlayer
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchCompetition
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.model.SubstitutionEvent
import com.wbjang.footballdiary.ui.theme.ThemeMode
import com.wbjang.footballdiary.domain.model.TeamLineup
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FootballRepositoryImpl @Inject constructor(
    private val apiService: FootballApiService,
    private val dataStore: UserPreferencesDataStore,
    private val reviewDao: ReviewDao
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

    override suspend fun getMatchDetail(matchId: Int): Result<MatchDetail> {
        return runCatching {
            val dto = apiService.getMatchDetail(matchId)
            MatchDetail(
                match = Match(
                    id = dto.id,
                    utcDate = dto.utcDate,
                    status = MatchStatus.from(dto.status),
                    matchday = dto.matchday,
                    competition = dto.competition?.let {
                        MatchCompetition(id = it.id, name = it.name, emblemUrl = it.emblem)
                    },
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
                ),
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
        }
    }

    override suspend fun saveReview(review: Review) {
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
                rating = review.rating,
                emotionTags = review.emotionTags.joinToString(","),
                content = review.content
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

    override fun getThemeMode(): Flow<ThemeMode> = dataStore.themeMode
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
        rating = rating,
        emotionTags = if (emotionTags.isBlank()) emptyList() else emotionTags.split(","),
        content = content,
        createdAt = createdAt
    )

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
                        competition = dto.competition?.let {
                            MatchCompetition(id = it.id, name = it.name, emblemUrl = it.emblem)
                        },
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
