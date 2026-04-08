package com.wbjang.footballdiary.ui.main.diary

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchCompetition
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam
import com.wbjang.footballdiary.domain.model.Review
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WriteReviewScreen(
    match: Match,
    matchDetail: MatchDetail?,
    existingReview: Review?,
    onBack: () -> Unit,
    viewModel: WriteReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        existingReview?.let { viewModel.initWithReview(it) }
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WriteReviewUiEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                WriteReviewUiEvent.NavigateBack -> onBack()
            }
        }
    }

    val initialContent = existingReview?.content ?: ""
    val initialTags = existingReview?.emotionTags ?: emptyList()
    val initialRating = existingReview?.rating?.toInt() ?: 0

    val isDirty = uiState.content != initialContent ||
                  uiState.selectedTags != initialTags ||
                  uiState.rating != initialRating

    val onBackPressed = {
        if (isDirty) showDiscardDialog = true else onBack()
    }

    val contentRequiredMessage = stringResource(R.string.write_review_content_required)
    val onSave = {
        viewModel.saveReview(
            matchId = match.id,
            utcDate = match.utcDate,
            homeTeamId = match.homeTeam.id,
            homeTeamName = match.homeTeam.name,
            homeTeamShortName = match.homeTeam.shortName,
            homeTeamCrestUrl = match.homeTeam.crestUrl,
            awayTeamId = match.awayTeam.id,
            awayTeamName = match.awayTeam.name,
            awayTeamShortName = match.awayTeam.shortName,
            awayTeamCrestUrl = match.awayTeam.crestUrl,
            homeScore = match.homeScore,
            awayScore = match.awayScore,
            matchday = match.matchday,
            competition = match.competition?.name,
            competitionEmblemUrl = match.competition?.emblemUrl,
            venue = matchDetail?.venue,
            seasonLabel = matchDetail?.seasonLabel,
            contentRequiredMessage = contentRequiredMessage
        )
    }

    BackHandler(onBack = onBackPressed)

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(text = stringResource(R.string.write_review_discard_title)) },
            text = { Text(text = stringResource(R.string.write_review_discard_message)) },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text(
                        text = stringResource(R.string.write_review_discard_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(text = stringResource(R.string.write_review_discard_cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.write_review_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            actions = {
                TextButton(onClick = onSave) {
                    Text(
                        text = stringResource(R.string.write_review_save_action),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            windowInsets = WindowInsets(0)
        )

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            // 경기 정보
            MatchInfoSection(match = match, matchDetail = matchDetail)

            // 평점
            RatingSection(
                rating = uiState.rating,
                onRatingChange = viewModel::setRating
            )

            HorizontalDivider()

            // 감정 태그
            EmotionTagSection(
                selectedTags = uiState.selectedTags,
                onTagToggle = viewModel::toggleTag,
                onCustomTagAdd = viewModel::addCustomTag,
                onTagRemove = viewModel::toggleTag
            )

            HorizontalDivider()

            // 소감 작성
            ContentSection(
                content = uiState.content,
                onContentChange = viewModel::setContent
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        }
    }
}

@Composable
private fun MatchInfoSection(match: Match, matchDetail: MatchDetail?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_small)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // 스코어
            val scoreText = if (match.homeScore != null && match.awayScore != null) {
                "${match.homeTeam.name}  ${match.homeScore} ${stringResource(R.string.write_review_score_separator)} ${match.awayScore}  ${match.awayTeam.name}"
            } else {
                "${match.homeTeam.name}  ${stringResource(R.string.match_status_vs)}  ${match.awayTeam.name}"
            }
            Text(
                text = scoreText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // 대회명
            match.competition?.name?.let { competitionName ->
                val venueText = matchDetail?.venue
                val infoText = if (venueText != null) {
                    stringResource(R.string.write_review_competition_venue_format, competitionName, venueText)
                } else {
                    competitionName
                }
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
}

@Composable
private fun RatingSection(rating: Int, onRatingChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Text(
            text = stringResource(R.string.write_review_rating_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.write_review_star_gap))) {
            for (star in 1..5) {
                IconButton(
                    onClick = { onRatingChange(star) },
                    modifier = Modifier.size(dimensionResource(R.dimen.write_review_star_button_size))
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (star <= rating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensionResource(R.dimen.write_review_star_icon_size))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun EmotionTagSection(
    selectedTags: List<String>,
    onTagToggle: (String) -> Unit,
    onCustomTagAdd: (String) -> Unit,
    onTagRemove: (String) -> Unit
) {
    val customTagInputState = rememberTextFieldState()
    val presetTags = stringArrayResource(R.array.preset_emotion_tags).toList()
    val customTags = selectedTags.filter { it !in presetTags }

    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Text(
            text = stringResource(R.string.write_review_emotion_tags_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides dimensionResource(R.dimen.tag_chip_min_touch_target)) {
        // 프리셋 태그
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.write_review_tag_vertical_gap))
            ) {
                presetTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggle(tag) },
                        label = { Text(text = tag) }
                    )
                }
            }
        }


        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides dimensionResource(R.dimen.tag_chip_min_touch_target)) {
            // 커스텀 태그
            if (customTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.write_review_tag_vertical_gap))
                ) {
                    customTags.forEach { tag ->
                        InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text(text = tag) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onTagRemove(tag) },
                                    modifier = Modifier.size(dimensionResource(R.dimen.write_review_chip_close_button_size))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(dimensionResource(R.dimen.write_review_chip_close_icon_size))
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }


        // 커스텀 태그 입력
        val fontScale = LocalDensity.current.fontScale
        val compactHeight = dimensionResource(R.dimen.text_field_compact_height)
        val fieldHeight = remember(fontScale, compactHeight) {
            (compactHeight.value * fontScale.coerceAtLeast(1f)).dp
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            OutlinedTextField(
                state = customTagInputState,
                placeholder = { Text(text = stringResource(R.string.write_review_tag_input_hint)) },
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(R.dimen.padding_medium),
                    vertical = dimensionResource(R.dimen.padding_small)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(fieldHeight)
            )
            TextButton(
                onClick = {
                    val input = customTagInputState.text.toString()
                    if (input.isNotBlank()) {
                        onCustomTagAdd(input.trim())
                        customTagInputState.clearText()
                    }
                }
            ) {
                Text(text = stringResource(R.string.write_review_tag_add))
            }
        }
    }
}

@Composable
private fun ContentSection(content: String, onContentChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Text(
            text = stringResource(R.string.write_review_content_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            placeholder = { Text(text = stringResource(R.string.write_review_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.write_review_content_height)),
            maxLines = 10
        )
    }
}

// ──────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewMatchInfoSection() {
    val sampleMatch = Match(
        id = 1,
        utcDate = "2024-03-20T20:00:00Z",
        status = MatchStatus.FINISHED,
        matchday = 28,
        competition = MatchCompetition(1, "Premier League", null),
        homeTeam = MatchTeam(1, "Arsenal FC", "Arsenal", ""),
        awayTeam = MatchTeam(2, "Manchester City FC", "Man City", ""),
        homeScore = 2,
        awayScore = 1
    )
    val sampleDetail = MatchDetail(
        match = sampleMatch,
        seasonLabel = "2023/24",
        venue = "Emirates Stadium",
        attendance = 60000,
        goals = emptyList(),
        bookings = emptyList(),
        substitutions = emptyList(),
        lineups = emptyList()
    )

    FootballDiaryTheme {
        Column(modifier = Modifier/*.padding(dimensionResource(R.dimen.padding_medium))*/) {
            MatchInfoSection(match = sampleMatch, matchDetail = sampleDetail)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingSection() {
    var rating by remember { mutableStateOf(3) }
    FootballDiaryTheme {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            RatingSection(rating = rating, onRatingChange = { rating = it })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEmotionTagSection() {
    var selectedTags by remember { mutableStateOf(listOf("승리", "짜릿함")) }
    FootballDiaryTheme {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            EmotionTagSection(
                selectedTags = selectedTags,
                onTagToggle = { tag ->
                    selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                },
                onCustomTagAdd = { tag -> selectedTags = selectedTags + tag },
                onTagRemove = { tag -> selectedTags = selectedTags - tag }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewContentSection() {
    var content by remember { mutableStateOf("") }
    com.wbjang.footballdiary.ui.theme.FootballDiaryTheme {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            ContentSection(content = content, onContentChange = { content = it })
        }
    }
}
