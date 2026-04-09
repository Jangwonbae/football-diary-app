package com.wbjang.footballdiary.domain.model

data class Review(
    val id: Int = 0,
    val matchId: Int,
    val utcDate: String,
    val homeTeamId: Int,
    val homeTeamName: String,
    val homeTeamShortName: String,
    val homeTeamCrestUrl: String,
    val awayTeamId: Int,
    val awayTeamName: String,
    val awayTeamShortName: String,
    val awayTeamCrestUrl: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val matchday: Int?,
    val competition: String?,
    val competitionEmblemUrl: String?,
    val venue: String?,
    val seasonLabel: String?,
    val rating: Float,
    val emotionTags: List<String>,
    val content: String,
    val followingTeamId: Int,
    val createdAt: Long = System.currentTimeMillis()
)

fun Review.toMatch() = Match(
    id = matchId,
    utcDate = utcDate,
    status = MatchStatus.FINISHED,
    matchday = matchday,
    competition = competition?.let {
        MatchCompetition(id = 0, name = it, emblemUrl = competitionEmblemUrl ?: "")
    },
    homeTeam = MatchTeam(id = homeTeamId, name = homeTeamName, shortName = homeTeamShortName, crestUrl = homeTeamCrestUrl),
    awayTeam = MatchTeam(id = awayTeamId, name = awayTeamName, shortName = awayTeamShortName, crestUrl = awayTeamCrestUrl),
    homeScore = homeScore,
    awayScore = awayScore
)
