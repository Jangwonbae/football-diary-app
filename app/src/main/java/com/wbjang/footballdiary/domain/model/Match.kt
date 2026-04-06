package com.wbjang.footballdiary.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class Match(
    val id: Int,
    val utcDate: String,       // "2026-04-10T15:00:00Z"
    val status: MatchStatus,
    val matchday: Int?,
    val competition: MatchCompetition?,
    val homeTeam: MatchTeam,
    val awayTeam: MatchTeam,
    val homeScore: Int?,
    val awayScore: Int?
) {
    fun localDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.parse(utcDate).atZone(zoneId).toLocalDate()

    fun localDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        Instant.parse(utcDate).atZone(zoneId).toLocalDateTime()

    fun isUpcoming(): Boolean = status == MatchStatus.SCHEDULED || status == MatchStatus.TIMED
    fun isFinished(): Boolean = status == MatchStatus.FINISHED
    fun isLive(): Boolean = status == MatchStatus.IN_PLAY || status == MatchStatus.PAUSED
}

data class MatchCompetition(
    val id: Int,
    val name: String,
    val emblemUrl: String?
)

data class MatchTeam(
    val id: Int,
    val name: String,
    val shortName: String,
    val crestUrl: String
)

enum class MatchResult { WIN, LOSS, DRAW }

fun Match.resultFor(teamId: Int): MatchResult? {
    if (!isFinished()) return null
    val home = homeScore ?: return null
    val away = awayScore ?: return null
    return when {
        homeTeam.id == teamId -> when {
            home > away -> MatchResult.WIN
            home < away -> MatchResult.LOSS
            else        -> MatchResult.DRAW
        }
        awayTeam.id == teamId -> when {
            away > home -> MatchResult.WIN
            away < home -> MatchResult.LOSS
            else        -> MatchResult.DRAW
        }
        else -> null
    }
}

enum class MatchStatus {
    SCHEDULED, TIMED, IN_PLAY, PAUSED, FINISHED,
    SUSPENDED, POSTPONED, CANCELLED, AWARDED;

    companion object {
        fun from(value: String): MatchStatus =
            entries.find { it.name == value } ?: SCHEDULED
    }
}
