package com.wbjang.footballdiary.ui.main.schedule

import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchCompetition
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam
import com.wbjang.footballdiary.domain.repository.FootballRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private lateinit var viewModel: ScheduleViewModel
    private lateinit var repository: FootballRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.getFollowingTeamId() } returns flowOf(1)
        coEvery { repository.getFollowingTeamName() } returns flowOf("Arsenal")
        coEvery { repository.getFollowingTeamCrestUrl() } returns flowOf("https://example.com/crest.png")
        coEvery { repository.getAllReviews() } returns flowOf(emptyList())
        coEvery { repository.getTeamMatches(any(), any(), any()) } returns Result.success(emptyList())
        viewModel = ScheduleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- 팀 정보 로드 ---

    @Test
    fun `init loads following team info into state`() {
        assertEquals(1, viewModel.uiState.value.followingTeamId)
        assertEquals("Arsenal", viewModel.uiState.value.followingTeamName)
        assertEquals("https://example.com/crest.png", viewModel.uiState.value.followingTeamCrestUrl)
    }

    @Test
    fun `init calls getTeamMatches when team id is available`() = runTest {
        coVerify { repository.getTeamMatches(1, any(), any()) }
    }

    // --- 뷰 모드 토글 ---

    @Test
    fun `toggleViewMode switches between list and calendar`() {
        assertFalse(viewModel.uiState.value.isCalendarMode)

        viewModel.toggleViewMode()
        assertTrue(viewModel.uiState.value.isCalendarMode)

        viewModel.toggleViewMode()
        assertFalse(viewModel.uiState.value.isCalendarMode)
    }

    // --- 월 이동 ---

    @Test
    fun `goToNextMonth advances currentYearMonth by 1`() {
        val before = viewModel.uiState.value.currentYearMonth

        viewModel.goToNextMonth()

        assertEquals(before.plusMonths(1), viewModel.uiState.value.currentYearMonth)
    }

    @Test
    fun `goToPreviousMonth decreases currentYearMonth by 1`() {
        val before = viewModel.uiState.value.currentYearMonth

        viewModel.goToPreviousMonth()

        assertEquals(before.minusMonths(1), viewModel.uiState.value.currentYearMonth)
    }

    @Test
    fun `resetToCurrentMonth sets currentYearMonth to now`() {
        viewModel.goToNextMonth()
        viewModel.goToNextMonth()

        viewModel.resetToCurrentMonth()

        assertEquals(YearMonth.now(), viewModel.uiState.value.currentYearMonth)
    }

    // --- 탭 전환 ---

    @Test
    fun `selectTab changes selectedTab`() {
        assertEquals(ScheduleTab.SCHEDULE, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(ScheduleTab.STANDINGS)

        assertEquals(ScheduleTab.STANDINGS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTab STANDINGS triggers loadStandings when standings empty`() = runTest {
        val match = Match(
            id = 1,
            utcDate = "2026-04-10T15:00:00Z",
            status = MatchStatus.FINISHED,
            matchday = 1,
            competition = MatchCompetition(id = 2021, name = "Premier League", emblemUrl = null),
            homeTeam = MatchTeam(id = 1, name = "Arsenal", shortName = "ARS", crestUrl = ""),
            awayTeam = MatchTeam(id = 2, name = "Chelsea", shortName = "CHE", crestUrl = ""),
            homeScore = 2,
            awayScore = 1
        )
        coEvery { repository.getTeamMatches(any(), any(), any()) } returns Result.success(listOf(match))
        coEvery { repository.getStandings(2021) } returns Result.success(emptyList())

        viewModel = ScheduleViewModel(repository)
        viewModel.selectTab(ScheduleTab.STANDINGS)

        coVerify { repository.getStandings(2021) }
    }

    // --- 에러 처리 ---

    @Test
    fun `loadMatches failure sets error message`() = runTest {
        coEvery { repository.getTeamMatches(any(), any(), any()) } returns Result.failure(
            RuntimeException("네트워크 오류")
        )

        viewModel = ScheduleViewModel(repository)

        assertEquals("네트워크 오류", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
