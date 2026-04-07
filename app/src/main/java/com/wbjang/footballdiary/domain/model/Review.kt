package com.wbjang.footballdiary.domain.model

data class Review(
    val id: Int = 0,
    val matchId: Int,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val competition: String?,
    val venue: String?,
    val rating: Float,
    val emotionTags: List<String>,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
