package com.wbjang.footballdiary.data.api.dto

import com.google.gson.annotations.SerializedName

data class StandingsResponse(
    @SerializedName("standings") val standings: List<StandingGroupDto>
)

data class StandingGroupDto(
    @SerializedName("type") val type: String,
    @SerializedName("table") val table: List<StandingEntryDto>
)

data class StandingEntryDto(
    @SerializedName("position") val position: Int,
    @SerializedName("team") val team: StandingTeamDto,
    @SerializedName("playedGames") val playedGames: Int,
    @SerializedName("won") val won: Int,
    @SerializedName("draw") val draw: Int,
    @SerializedName("lost") val lost: Int,
    @SerializedName("goalsFor") val goalsFor: Int,
    @SerializedName("goalsAgainst") val goalsAgainst: Int,
    @SerializedName("goalDifference") val goalDifference: Int,
    @SerializedName("points") val points: Int
)

data class StandingTeamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("crest") val crest: String
)
