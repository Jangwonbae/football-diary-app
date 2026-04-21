package com.wbjang.footballdiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchTest {

    private fun createMatch(
        homeTeamId: Int = 1,
        awayTeamId: Int = 2,
        homeScore: Int? = null,
        awayScore: Int? = null,
        status: MatchStatus = MatchStatus.FINISHED
    ) = Match(
        id = 100,
        utcDate = "2026-04-10T15:00:00Z",
        status = status,
        matchday = 1,
        competition = MatchCompetition(id = 2021, name = "Premier League", emblemUrl = null),
        homeTeam = MatchTeam(id = homeTeamId, name = "Home", shortName = "HOM", crestUrl = ""),
        awayTeam = MatchTeam(id = awayTeamId, name = "Away", shortName = "AWY", crestUrl = ""),
        homeScore = homeScore,
        awayScore = awayScore
    )

    // --- resultFor ---

    @Test
    fun `resultFor returns WIN when home team wins and teamId is home`() {
        val match = createMatch(homeScore = 3, awayScore = 1)
        assertEquals(MatchResult.WIN, match.resultFor(1))
    }

    @Test
    fun `resultFor returns LOSS when home team wins and teamId is away`() {
        val match = createMatch(homeScore = 3, awayScore = 1)
        assertEquals(MatchResult.LOSS, match.resultFor(2))
    }

    @Test
    fun `resultFor returns DRAW when scores are equal`() {
        val match = createMatch(homeScore = 2, awayScore = 2)
        assertEquals(MatchResult.DRAW, match.resultFor(1))
        assertEquals(MatchResult.DRAW, match.resultFor(2))
    }

    @Test
    fun `resultFor returns null when match is not finished`() {
        val match = createMatch(status = MatchStatus.SCHEDULED)
        assertNull(match.resultFor(1))
    }

    @Test
    fun `resultFor returns null for unrelated teamId`() {
        val match = createMatch(homeScore = 1, awayScore = 0)
        assertNull(match.resultFor(999))
    }

    // --- 상태 판별 ---

    @Test
    fun `isFinished returns true only for FINISHED status`() {
        assertTrue(createMatch(status = MatchStatus.FINISHED).isFinished())
        assertFalse(createMatch(status = MatchStatus.SCHEDULED).isFinished())
    }

    @Test
    fun `isUpcoming returns true for SCHEDULED and TIMED`() {
        assertTrue(createMatch(status = MatchStatus.SCHEDULED).isUpcoming())
        assertTrue(createMatch(status = MatchStatus.TIMED).isUpcoming())
        assertFalse(createMatch(status = MatchStatus.FINISHED).isUpcoming())
    }

    @Test
    fun `isLive returns true for IN_PLAY and PAUSED`() {
        assertTrue(createMatch(status = MatchStatus.IN_PLAY).isLive())
        assertTrue(createMatch(status = MatchStatus.PAUSED).isLive())
        assertFalse(createMatch(status = MatchStatus.FINISHED).isLive())
    }

    // --- MatchStatus.from ---

    @Test
    fun `MatchStatus from parses valid string`() {
        assertEquals(MatchStatus.FINISHED, MatchStatus.from("FINISHED"))
    }

    @Test
    fun `MatchStatus from returns SCHEDULED for unknown string`() {
        assertEquals(MatchStatus.SCHEDULED, MatchStatus.from("UNKNOWN_VALUE"))
    }
}
