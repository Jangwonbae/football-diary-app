package com.wbjang.footballdiary.domain.model

data class Review(
    val id: Int = 0,
    val matchId: Int,
    val utcDate: String,
    val homeTeamId: Int,
    val homeTeamName: String,
    val homeTeamCrestUrl: String,
    val awayTeamId: Int,
    val awayTeamName: String,
    val awayTeamCrestUrl: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val matchday: Int?,
    val competition: String?,
    val competitionEmblemUrl: String?,
    val venue: String?,
    val rating: Float,
    val emotionTags: List<String>,
    val content: String,
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
    homeTeam = MatchTeam(id = homeTeamId, name = homeTeamName, shortName = homeTeamName, crestUrl = homeTeamCrestUrl),
    awayTeam = MatchTeam(id = awayTeamId, name = awayTeamName, shortName = awayTeamName, crestUrl = awayTeamCrestUrl),
    homeScore = homeScore,
    awayScore = awayScore
)
