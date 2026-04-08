package com.wbjang.footballdiary.ui.main.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteReviewUiState(
    val existingReviewId: Int = 0,
    val rating: Int = 0,
    val selectedTags: List<String> = emptyList(),
    val content: String = ""
)

sealed class WriteReviewUiEvent {
    data class ShowToast(val message: String) : WriteReviewUiEvent()
    object NavigateBack : WriteReviewUiEvent()
}

@HiltViewModel
class WriteReviewViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WriteReviewUiEvent>(replay = 0)
    val uiEvent: SharedFlow<WriteReviewUiEvent> = _uiEvent.asSharedFlow()

    fun initWithReview(review: Review) {
        _uiState.update {
            it.copy(
                existingReviewId = review.id,
                rating = review.rating.toInt(),
                selectedTags = review.emotionTags,
                content = review.content
            )
        }
    }

    fun setRating(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun toggleTag(tag: String) {
        _uiState.update { state ->
            if (tag in state.selectedTags) {
                state.copy(selectedTags = state.selectedTags - tag)
            } else {
                state.copy(selectedTags = state.selectedTags + tag)
            }
        }
    }

    fun addCustomTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isNotEmpty() && trimmed !in _uiState.value.selectedTags) {
            _uiState.update { it.copy(selectedTags = it.selectedTags + trimmed) }
        }
    }

    fun setContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun saveReview(
        matchId: Int,
        utcDate: String,
        homeTeamId: Int,
        homeTeamName: String,
        homeTeamShortName: String,
        homeTeamCrestUrl: String,
        awayTeamId: Int,
        awayTeamName: String,
        awayTeamShortName: String,
        awayTeamCrestUrl: String,
        homeScore: Int?,
        awayScore: Int?,
        matchday: Int?,
        competition: String?,
        competitionEmblemUrl: String?,
        venue: String?,
        seasonLabel: String?,
        contentRequiredMessage: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.content.isBlank()) {
                _uiEvent.emit(WriteReviewUiEvent.ShowToast(contentRequiredMessage))
                return@launch
            }
            repository.saveReview(
                Review(
                    id = state.existingReviewId,
                    matchId = matchId,
                    utcDate = utcDate,
                    homeTeamId = homeTeamId,
                    homeTeamName = homeTeamName,
                    homeTeamShortName = homeTeamShortName,
                    homeTeamCrestUrl = homeTeamCrestUrl,
                    awayTeamId = awayTeamId,
                    awayTeamName = awayTeamName,
                    awayTeamShortName = awayTeamShortName,
                    awayTeamCrestUrl = awayTeamCrestUrl,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    matchday = matchday,
                    competition = competition,
                    competitionEmblemUrl = competitionEmblemUrl,
                    venue = venue,
                    seasonLabel = seasonLabel,
                    rating = state.rating.toFloat(),
                    emotionTags = state.selectedTags,
                    content = state.content
                )
            )
            _uiEvent.emit(WriteReviewUiEvent.NavigateBack)
        }
    }
}
