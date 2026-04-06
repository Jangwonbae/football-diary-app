package com.wbjang.footballdiary.data.api.dto

import com.google.gson.annotations.SerializedName

data class CompetitionTeamsResponse(
    @SerializedName("teams") val teams: List<TeamDto>
)

data class TeamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("crest") val crest: String
)
