package com.wbjang.footballdiary.ui.main.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.MatchResult
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.domain.model.resultFor
import com.wbjang.footballdiary.domain.model.toMatch
import com.wbjang.footballdiary.ui.components.ExpandableTagRow
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme
import com.wbjang.footballdiary.ui.theme.ResultDraw
import com.wbjang.footballdiary.ui.theme.ResultWin
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryScreen(
    onReviewClick: (Review) -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val followingTeamId by viewModel.followingTeamId.collectAsStateWithLifecycle()
    val sortField by viewModel.sortField.collectAsStateWithLifecycle()
    val sortDirection by viewModel.sortDirection.collectAsStateWithLifecycle()
    val selectedSeason by viewModel.selectedSeason.collectAsStateWithLifecycle()
    val availableSeasons by viewModel.availableSeasons.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // 정렬 + 시즌 필터
        val chipPadding = dimensionResource(R.dimen.padding_small)
        val horizontalPadding = dimensionResource(R.dimen.padding_medium)
        val topPadding = dimensionResource(R.dimen.padding_small)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 40.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(chipPadding)) {
                    FilterChip(
                        selected = sortField == ReviewSortField.MATCH_DATE,
                        onClick = { viewModel.setSortField(ReviewSortField.MATCH_DATE) },
                        label = { Text(stringResource(R.string.diary_sort_field_match_date)) }
                    )
                    FilterChip(
                        selected = sortField == ReviewSortField.WRITTEN_DATE,
                        onClick = { viewModel.setSortField(ReviewSortField.WRITTEN_DATE) },
                        label = { Text(stringResource(R.string.diary_sort_field_written_date)) }
                    )
                }
                if (availableSeasons.isNotEmpty()) {
                    SeasonDropdown(
                        selectedSeason = selectedSeason,
                        availableSeasons = availableSeasons,
                        onSeasonSelected = { viewModel.setSelectedSeason(it) }
                    )
                }
            }

        }
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 40.dp) {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(chipPadding)
            ) {
                FilterChip(
                    selected = sortDirection == ReviewSortDirection.DESC,
                    onClick = { viewModel.setSortDirection(ReviewSortDirection.DESC) },
                    label = { Text(stringResource(R.string.diary_sort_dir_desc)) }
                )
                FilterChip(
                    selected = sortDirection == ReviewSortDirection.ASC,
                    onClick = { viewModel.setSortDirection(ReviewSortDirection.ASC) },
                    label = { Text(stringResource(R.string.diary_sort_dir_asc)) }
                )
            }
        }

        if (reviews.isEmpty()) {
            EmptyDiary()
        } else {
            val listState = rememberLazyListState()
            val resetKey = Triple(sortField, sortDirection, selectedSeason)

            LaunchedEffect(resetKey) {
                listState.scrollToItem(0)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small))) }
                items(reviews, key = { it.id }) { review ->
                    ReviewCard(
                        review = review,
                        followingTeamId = followingTeamId,
                        resetKey = resetKey,
                        onClick = { onReviewClick(review) }
                    )
                }
                item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium))) }
            }
        }
    }
}

@Composable
private fun SeasonDropdown(
    selectedSeason: String?,
    availableSeasons: List<String>,
    onSeasonSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResource(R.string.diary_season_all)
    Box {
        FilterChip(
            selected = selectedSeason != null,
            onClick = { expanded = true },
            label = { Text(text = selectedSeason ?: allLabel) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = allLabel) },
                onClick = { onSeasonSelected(null); expanded = false }
            )
            availableSeasons.forEach { season ->
                DropdownMenuItem(
                    text = { Text(text = season) },
                    onClick = { onSeasonSelected(season); expanded = false }
                )
            }
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
private fun ReviewCard(review: Review, followingTeamId: Int?, resetKey: Any, onClick: () -> Unit) {
    val writtenDateFormat = stringResource(R.string.date_format_review_written)
    val writtenFormatter = remember(writtenDateFormat) {
        DateTimeFormatter.ofPattern(writtenDateFormat, Locale.KOREAN)
    }
    val writtenAt = Instant.ofEpochMilli(review.createdAt)
        .atZone(ZoneId.systemDefault())
        .format(writtenFormatter)

    val matchDateFormat = stringResource(R.string.date_format_match_datetime)
    val matchFormatter = remember(matchDateFormat) {
        DateTimeFormatter.ofPattern(matchDateFormat, Locale.KOREAN)
    }
    val matchStartTime = remember(review.utcDate) {
        ZonedDateTime.parse(review.utcDate)
            .withZoneSameInstant(ZoneId.systemDefault())
            .format(matchFormatter)
    }

    val matchResult = remember(followingTeamId, review) {
        followingTeamId?.let { review.toMatch().resultFor(it) }
    }

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
            // 날짜/시간
            Text(
                text = matchStartTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 홈팀 · 대회+스코어 · 어웨이팀
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 홈팀
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = review.homeTeamCrestUrl,
                        contentDescription = review.homeTeamShortName,
                        modifier = Modifier.size(dimensionResource(R.dimen.emblem_match_card))
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.match_card_team_spacer)))
                    Text(
                        text = review.homeTeamShortName,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 대회 아이콘 + 스코어
                Column(
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!review.competitionEmblemUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = review.competitionEmblemUrl,
                            contentDescription = review.competition,
                            modifier = Modifier.size(dimensionResource(R.dimen.emblem_competition))
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_xsmall)))
                    }
                    if (review.homeScore != null && review.awayScore != null) {
                        if (matchResult != null) {
                            val bgColor = when (matchResult) {
                                MatchResult.WIN  -> ResultWin
                                MatchResult.LOSS -> MaterialTheme.colorScheme.error
                                MatchResult.DRAW -> ResultDraw
                            }
                            Box(
                                modifier = Modifier
                                    .background(bgColor, RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)))
                                    .padding(
                                        horizontal = dimensionResource(R.dimen.badge_horizontal_padding),
                                        vertical = dimensionResource(R.dimen.badge_vertical_padding)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${review.homeScore} - ${review.awayScore}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "${review.homeScore} - ${review.awayScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.match_status_vs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 어웨이팀
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = review.awayTeamCrestUrl,
                        contentDescription = review.awayTeamShortName,
                        modifier = Modifier.size(dimensionResource(R.dimen.emblem_match_card))
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.match_card_team_spacer)))
                    Text(
                        text = review.awayTeamShortName,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 별점
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
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides dimensionResource(R.dimen.tag_chip_min_touch_target)) {
                // 감정 태그
                if (review.emotionTags.isNotEmpty()) {
                    ExpandableTagRow(tags = review.emotionTags, resetKey = resetKey)
                }
            }


            // 소감 내용 미리보기
            if (review.content.isNotBlank()) {
                var isExpanded by rememberSaveable(resetKey) { mutableStateOf(false) }
                var isOverflowing by remember { mutableStateOf(false) }

                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                    onTextLayout = { if (!isExpanded) isOverflowing = it.hasVisualOverflow }
                )
                if (isOverflowing && !isExpanded) {
                    Text(
                        text = stringResource(R.string.content_expand),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { isExpanded = true }
                    )
                } else if (isExpanded) {
                    Text(
                        text = stringResource(R.string.tag_collapse),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { isExpanded = false }
                    )
                }
            }
            Text(
                text = writtenAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun PreviewReviewCard() {
    FootballDiaryTheme {
        ReviewCard(
            review = Review(
                id = 1,
                matchId = 101,
                utcDate = "2024-03-20T20:00:00Z",
                homeTeamId = 1,
                homeTeamName = "아스널 FC",
                homeTeamShortName = "아스널",
                homeTeamCrestUrl = "",
                awayTeamId = 2,
                awayTeamName = "맨체스터 시티 FC",
                awayTeamShortName = "맨시티",
                awayTeamCrestUrl = "",
                homeScore = 2,
                awayScore = 1,
                matchday = 28,
                competition = "Premier League",
                competitionEmblemUrl = "",
                venue = "Emirates Stadium",
                seasonLabel = "2025/2026",
                rating = 5f,
                emotionTags = listOf("승리", "역전승", "짜릿함"),
                content = "정말 환상적인 경기였습니다! 마지막 분에 터진 결승골은 잊을 수 없을 거예요.",
                followingTeamId = 1,
                createdAt = System.currentTimeMillis()
            ),
            followingTeamId = 1,
            resetKey = Unit,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEmptyDiary() {
    FootballDiaryTheme {
        EmptyDiary()
    }
}
