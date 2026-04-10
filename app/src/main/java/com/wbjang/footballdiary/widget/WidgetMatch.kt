package com.wbjang.footballdiary.widget

data class WidgetMatch(
    val homeTeamShortName: String,
    val awayTeamShortName: String,
    val utcDate: String,
    val competitionName: String?
) {
    val formattedDate: String
        get() = try {
            val local = java.time.Instant.parse(utcDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
            "${local.monthValue}/${local.dayOfMonth} ${local.hour}:${String.format("%02d", local.minute)}"
        } catch (e: Exception) {
            "-"
        }
}
