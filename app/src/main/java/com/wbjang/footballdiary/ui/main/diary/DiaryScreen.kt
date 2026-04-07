package com.wbjang.footballdiary.ui.main.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.Review
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryScreen(
    onReviewClick: (Review) -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()

    if (reviews.isEmpty()) {
        EmptyDiary()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small))) }
            items(reviews, key = { it.id }) { review ->
                ReviewCard(
                    review = review,
                    onClick = { onReviewClick(review) }
                )
            }
            item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium))) }
        }
    }
}

@Composable
private fun EmptyDiary() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(R.string.diary_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.diary_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewCard(review: Review, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern(
        stringResource(R.string.date_format_review_written), Locale.KOREAN
    )
    val writtenAt = Instant.ofEpochMilli(review.createdAt)
        .atZone(ZoneId.systemDefault())
        .format(dateFormatter)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // 스코어
            val scoreText = if (review.homeScore != null && review.awayScore != null) {
                "${review.homeTeamName}  ${review.homeScore} ${stringResource(R.string.write_review_score_separator)} ${review.awayScore}  ${review.awayTeamName}"
            } else {
                "${review.homeTeamName}  ${stringResource(R.string.match_status_vs)}  ${review.awayTeamName}"
            }
            Text(
                text = scoreText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 대회명 · 작성 시각
            val metaText = listOfNotNull(review.competition, writtenAt).joinToString("  ·  ")
            Text(
                text = metaText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 별점
            if (review.rating > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.match_detail_review_star_gap)
                    )
                ) {
                    for (star in 1..5) {
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Filled.Star
                            else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (star <= review.rating) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimensionResource(R.dimen.match_detail_review_star_size))
                        )
                    }
                }
            }

            // 감정 태그
            if (review.emotionTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding_xsmall)
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.padding_xsmall)
                    )
                ) {
                    review.emotionTags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(text = tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // 소감 내용 미리보기
            if (review.content.isNotBlank()) {
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
