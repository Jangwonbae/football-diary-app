package com.wbjang.footballdiary.data.api.dto

import com.google.gson.annotations.SerializedName

data class MatchDetailResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("utcDate") val utcDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("matchday") val matchday: Int?,
    @SerializedName("competition") val competition: CompetitionDto?,
    @SerializedName("homeTeam") val homeTeam: MatchTeamDto,
    @SerializedName("awayTeam") val awayTeam: MatchTeamDto,
    @SerializedName("score") val score: MatchScoreDto,
    @SerializedName("venue") val venue: String?,
    @SerializedName("attendance") val attendance: Int?,
    @SerializedName("goals") val goals: List<GoalEventDto>?,
    @SerializedName("bookings") val bookings: List<BookingEventDto>?,
    @SerializedName("substitutions") val substitutions: List<SubstitutionEventDto>?,
    @SerializedName("lineup") val lineup: List<TeamLineupDto>?
)

data class GoalEventDto(
    @SerializedName("minute") val minute: Int?,
    @SerializedName("injuryTime") val injuryTime: Int?,
    @SerializedName("type") val type: String?,
    @SerializedName("team") val team: TeamRefDto?,
    @SerializedName("scorer") val scorer: PlayerRefDto?,
    @SerializedName("assist") val assist: PlayerRefDto?
)

data class BookingEventDto(
    @SerializedName("minute") val minute: Int?,
    @SerializedName("team") val team: TeamRefDto?,
    @SerializedName("player") val player: PlayerRefDto?,
    @SerializedName("card") val card: String?
)

data class SubstitutionEventDto(
    @SerializedName("minute") val minute: Int?,
    @SerializedName("team") val team: TeamRefDto?,
    @SerializedName("playerOut") val playerOut: PlayerRefDto?,
    @SerializedName("playerIn") val playerIn: PlayerRefDto?
)

data class TeamRefDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?
)

data class PlayerRefDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?
)

data class TeamLineupDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("formation") val formation: String?,
    @SerializedName("startXI") val startXI: List<LineupPlayerDto>?,
    @SerializedName("bench") val bench: List<LineupPlayerDto>?
)

data class LineupPlayerDto(
    @SerializedName("player") val player: PlayerRefDto?,
    @SerializedName("position") val position: String?,
    @SerializedName("shirtNumber") val shirtNumber: Int?
)
