package com.wbjang.footballdiary.widget

data class WidgetMatch(
    val homeTeamShortName: String,
    val awayTeamShortName: String,
    val utcDate: String,
    val competitionName: String?
) {
    fun localDateTime(): java.time.LocalDateTime = java.time.Instant.parse(utcDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDateTime()
}
