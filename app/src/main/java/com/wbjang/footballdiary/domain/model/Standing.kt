package com.wbjang.footballdiary.domain.model

data class StandingEntry(
    val position: Int,
    val team: StandingTeam,
    val playedGames: Int,
    val won: Int,
    val draw: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int
)

data class StandingTeam(
    val id: Int,
    val name: String,
    val shortName: String,
    val crestUrl: String
)
