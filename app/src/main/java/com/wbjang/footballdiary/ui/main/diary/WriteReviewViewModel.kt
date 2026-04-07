package com.wbjang.footballdiary.ui.main.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteReviewUiState(
    val rating: Int = 0,
    val selectedTags: List<String> = emptyList(),
    val content: String = "",
    val isSaved: Boolean = false
)

@HiltViewModel
class WriteReviewViewModel @Inject constructor(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun initWithReview(review: Review) {
        _uiState.update {
            it.copy(
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
        homeTeamName: String,
        awayTeamName: String,
        homeScore: Int?,
        awayScore: Int?,
        competition: String?,
        venue: String?
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            repository.saveReview(
                Review(
                    matchId = matchId,
                    homeTeamName = homeTeamName,
                    awayTeamName = awayTeamName,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    competition = competition,
                    venue = venue,
                    rating = state.rating.toFloat(),
                    emotionTags = state.selectedTags,
                    content = state.content
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
