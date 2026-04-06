package com.wbjang.footballdiary.data.api.dto

import com.google.gson.annotations.SerializedName

data class TeamMatchesResponse(
    @SerializedName("matches") val matches: List<MatchDto>
)

data class MatchDto(
    @SerializedName("id") val id: Int,
    @SerializedName("utcDate") val utcDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("matchday") val matchday: Int?,
    @SerializedName("competition") val competition: CompetitionDto?,
    @SerializedName("homeTeam") val homeTeam: MatchTeamDto,
    @SerializedName("awayTeam") val awayTeam: MatchTeamDto,
    @SerializedName("score") val score: MatchScoreDto
)

data class CompetitionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String?,
    @SerializedName("emblem") val emblem: String?
)

data class MatchTeamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("crest") val crest: String
)

data class MatchScoreDto(
    @SerializedName("fullTime") val fullTime: ScoreValueDto
)

data class ScoreValueDto(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)
