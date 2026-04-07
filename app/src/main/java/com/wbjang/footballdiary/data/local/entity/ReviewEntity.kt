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
    val rating: Float,
    val emotionTags: String, // 쉼표로 구분된 태그 목록
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
