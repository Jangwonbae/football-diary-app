package com.wbjang.footballdiary.ui.main.schedule

import com.wbjang.footballdiary.domain.model.BookingEvent
import com.wbjang.footballdiary.domain.model.CardType
import com.wbjang.footballdiary.domain.model.GoalEvent
import com.wbjang.footballdiary.domain.model.GoalType
import com.wbjang.footballdiary.domain.model.LineupPlayer
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.SubstitutionEvent
import com.wbjang.footballdiary.domain.model.TeamLineup

// ── 샘플 데이터 (API 미제공 항목을 대체) ──────────────
object SampleMatchData {

    const val SAMPLE_VENUE = "샘플 경기장"
    const val SAMPLE_ATTENDANCE = 45_000

    fun sampleGoals(match: Match): List<GoalEvent> = listOf(
        GoalEvent(23, null,  GoalType.REGULAR, match.homeTeam.id, "샘플 선수", "샘플 도움"),
        GoalEvent(67, null,  GoalType.REGULAR, match.awayTeam.id, "샘플 선수", null),
        GoalEvent(89, 2,     GoalType.PENALTY, match.homeTeam.id, "샘플 선수", null)
    )

    fun sampleBookings(match: Match): List<BookingEvent> = listOf(
        BookingEvent(35, match.homeTeam.id, "샘플 선수", CardType.YELLOW),
        BookingEvent(78, match.awayTeam.id, "샘플 선수", CardType.YELLOW),
        BookingEvent(90, match.awayTeam.id, "샘플 선수", CardType.RED)
    )

    fun sampleSubstitutions(match: Match): List<SubstitutionEvent> = listOf(
        SubstitutionEvent(60, match.homeTeam.id, "샘플 아웃 A", "샘플 인 A"),
        SubstitutionEvent(72, match.awayTeam.id, "샘플 아웃 B", "샘플 인 B"),
        SubstitutionEvent(80, match.homeTeam.id, "샘플 아웃 C", "샘플 인 C")
    )

    fun sampleLineups(match: Match): List<TeamLineup> = listOf(
        buildLineup(match.homeTeam.id, match.homeTeam.name),
        buildLineup(match.awayTeam.id, match.awayTeam.name)
    )

    private fun buildLineup(teamId: Int, teamName: String): TeamLineup = TeamLineup(
        teamId = teamId,
        teamName = teamName,
        formation = "4-3-3",
        startingEleven = listOf(
            LineupPlayer("샘플 GK", "Goalkeeper",          1),
            LineupPlayer("샘플 RB", "Right Back",           2),
            LineupPlayer("샘플 CB", "Centre-Back",          5),
            LineupPlayer("샘플 CB", "Centre-Back",          6),
            LineupPlayer("샘플 LB", "Left Back",            3),
            LineupPlayer("샘플 DM", "Defensive Midfield",   4),
            LineupPlayer("샘플 CM", "Central Midfield",     8),
            LineupPlayer("샘플 AM", "Attacking Midfield",  10),
            LineupPlayer("샘플 RW", "Right Winger",         7),
            LineupPlayer("샘플 ST", "Centre-Forward",       9),
            LineupPlayer("샘플 LW", "Left Winger",         11)
        ),
        bench = listOf(
            LineupPlayer("샘플 GK", "Goalkeeper",          12),
            LineupPlayer("샘플 DF", "Centre-Back",         13),
            LineupPlayer("샘플 DF", "Right Back",          14),
            LineupPlayer("샘플 MF", "Central Midfield",    15),
            LineupPlayer("샘플 MF", "Attacking Midfield",  16),
            LineupPlayer("샘플 FW", "Left Winger",         17),
            LineupPlayer("샘플 FW", "Centre-Forward",      18)
        )
    )

    // 통계는 항상 샘플 (API 미지원)
    val SAMPLE_STATISTICS = listOf(
        StatItem(StatType.POSSESSION,       "55%", "45%"),
        StatItem(StatType.SHOTS,             "14",   "9"),
        StatItem(StatType.SHOTS_ON_TARGET,    "5",   "3"),
        StatItem(StatType.YELLOW_CARDS,       "2",   "1"),
        StatItem(StatType.CORNER_KICKS,       "6",   "4")
    )
}

enum class StatType { POSSESSION, SHOTS, SHOTS_ON_TARGET, YELLOW_CARDS, CORNER_KICKS }

data class StatItem(val type: StatType, val homeValue: String, val awayValue: String)
