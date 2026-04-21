package com.wbjang.footballdiary.ui.main.diary

import app.cash.turbine.test
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteReviewViewModelTest {

    private lateinit var viewModel: WriteReviewViewModel
    private lateinit var repository: FootballRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.getFollowingTeamId() } returns flowOf(1)
        viewModel = WriteReviewViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setRating updates rating in state`() {
        viewModel.setRating(4)
        assertEquals(4, viewModel.uiState.value.rating)
    }

    @Test
    fun `toggleTag adds tag when not present`() {
        viewModel.toggleTag("골")
        assertTrue(viewModel.uiState.value.selectedTags.contains("골"))
    }

    @Test
    fun `toggleTag removes tag when already present`() {
        viewModel.toggleTag("골")
        viewModel.toggleTag("골")
        assertTrue(viewModel.uiState.value.selectedTags.isEmpty())
    }

    @Test
    fun `addCustomTag trims and adds non-duplicate tag`() {
        viewModel.addCustomTag("  역전  ")
        assertEquals(listOf("역전"), viewModel.uiState.value.selectedTags)
    }

    @Test
    fun `addCustomTag ignores duplicate`() {
        viewModel.addCustomTag("역전")
        viewModel.addCustomTag("역전")
        assertEquals(1, viewModel.uiState.value.selectedTags.size)
    }

    @Test
    fun `saveReview emits ShowToast when content is blank`() = runTest {
        viewModel.setContent("")

        viewModel.uiEvent.test {
            viewModel.saveReview(
                matchId = 1, utcDate = "2024-01-01", homeTeamId = 1,
                homeTeamName = "Home", homeTeamShortName = "HOM",
                homeTeamCrestUrl = "", awayTeamId = 2,
                awayTeamName = "Away", awayTeamShortName = "AWY",
                awayTeamCrestUrl = "", homeScore = 1, awayScore = 0,
                matchday = 1, competition = "PL",
                competitionEmblemUrl = "", venue = "Stadium",
                seasonLabel = "24/25", contentRequiredMessage = "내용을 입력해주세요"
            )

            val event = awaitItem()
            assertTrue(event is WriteReviewUiEvent.ShowToast)
            assertEquals("내용을 입력해주세요", (event as WriteReviewUiEvent.ShowToast).message)
        }
    }

    @Test
    fun `saveReview calls repository and emits NavigateBack when content is valid`() = runTest {
        viewModel.setContent("좋은 경기였다")
        viewModel.setRating(5)

        viewModel.uiEvent.test {
            viewModel.saveReview(
                matchId = 1, utcDate = "2024-01-01", homeTeamId = 1,
                homeTeamName = "Home", homeTeamShortName = "HOM",
                homeTeamCrestUrl = "", awayTeamId = 2,
                awayTeamName = "Away", awayTeamShortName = "AWY",
                awayTeamCrestUrl = "", homeScore = 1, awayScore = 0,
                matchday = 1, competition = "PL",
                competitionEmblemUrl = "", venue = "Stadium",
                seasonLabel = "24/25", contentRequiredMessage = "내용을 입력해주세요"
            )

            val event = awaitItem()
            assertTrue(event is WriteReviewUiEvent.NavigateBack)
        }

        coVerify { repository.saveReview(any()) }
    }
}
