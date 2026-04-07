package com.wbjang.footballdiary.domain.model

data class MatchDetail(
    val match: Match,
    val seasonLabel: String?,
    val venue: String?,
    val attendance: Int?,
    val goals: List<GoalEvent>,
    val bookings: List<BookingEvent>,
    val substitutions: List<SubstitutionEvent>,
    val lineups: List<TeamLineup>
)

// ── 타임라인 이벤트 ──────────────────────────────
data class GoalEvent(
    val minute: Int,
    val injuryTime: Int?,
    val type: GoalType,
    val teamId: Int?,
    val scorerName: String,
    val assistName: String?
)

enum class GoalType { REGULAR, OWN_GOAL, PENALTY }

data class BookingEvent(
    val minute: Int,
    val teamId: Int?,
    val playerName: String,
    val card: CardType
)

enum class CardType { YELLOW, RED, YELLOW_RED }

data class SubstitutionEvent(
    val minute: Int,
    val teamId: Int?,
    val playerOutName: String,
    val playerInName: String
)

// ── 라인업 ──────────────────────────────────────
data class TeamLineup(
    val teamId: Int,
    val teamName: String,
    val formation: String?,
    val startingEleven: List<LineupPlayer>,
    val bench: List<LineupPlayer>
)

data class LineupPlayer(
    val name: String,
    val position: String?,
    val shirtNumber: Int?
)
