package com.wbjang.footballdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
    val emotionTags: String, // JSON 배열 문자열 (예: ["승리","짜릿함"])
    val content: String,
    val followingTeamId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
