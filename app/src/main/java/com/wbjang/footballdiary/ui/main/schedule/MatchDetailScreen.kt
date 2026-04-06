package com.wbjang.footballdiary.ui.main.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.BookingEvent
import com.wbjang.footballdiary.domain.model.CardType
import com.wbjang.footballdiary.domain.model.GoalEvent
import com.wbjang.footballdiary.domain.model.GoalType
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchDetail
import com.wbjang.footballdiary.domain.model.MatchResult
import com.wbjang.footballdiary.domain.model.SubstitutionEvent
import com.wbjang.footballdiary.domain.model.TeamLineup
import com.wbjang.footballdiary.domain.model.resultFor
import com.wbjang.footballdiary.ui.theme.ResultDraw
import com.wbjang.footballdiary.ui.theme.ResultWin
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    match: Match,
    followingTeamId: Int?,
    onBack: () -> Unit,
    onWriteReview: () -> Unit,
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.matchDetail
    val matchResult = followingTeamId?.let { match.resultFor(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.match_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            windowInsets = WindowInsets(0)
        )

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
        ) {
            // 경기 카드
            MatchSummaryCard(
                match = match,
                matchResult = matchResult,
                venue = detail?.venue,
                attendance = detail?.attendance
            )

            // 소감 섹션
            ReviewSection(onWriteReview = onWriteReview)

            // 경기 상세 섹션
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)))
                }
            } else {
                // 라인업
                DetailSection(title = stringResource(R.string.match_detail_section_lineup)) {
                    LineupContent(lineups = detail?.lineups.orEmpty(), match = match)
                }

                // 타임라인
                DetailSection(title = stringResource(R.string.match_detail_section_timeline)) {
                    TimelineContent(
                        goals = detail?.goals.orEmpty(),
                        bookings = detail?.bookings.orEmpty(),
                        substitutions = detail?.substitutions.orEmpty(),
                        match = match
                    )
                }

                // 경기 통계
                DetailSection(title = stringResource(R.string.match_detail_section_statistics)) {
                    StatisticsContent()
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        }
    }
}

// ──────────────────────────────────────────────
// 경기 요약 카드
// ──────────────────────────────────────────────
@Composable
private fun MatchSummaryCard(
    match: Match,
    matchResult: MatchResult?,
    venue: String?,
    attendance: Int?
) {
    val dateFormatter = DateTimeFormatter.ofPattern(
        stringResource(R.string.date_format_match_detail), Locale.KOREAN
    )
    val formattedDate = match.localDateTime().format(dateFormatter)

    Card(
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // 대회 이름 · 라운드 · 날짜
            val headerText = if (match.matchday != null) {
                stringResource(
                    R.string.match_detail_header_format,
                    match.competition?.name ?: "",
                    match.matchday,
                    formattedDate
                )
            } else {
                stringResource(
                    R.string.match_detail_header_no_round_format,
                    match.competition?.name ?: "",
                    formattedDate
                )
            }
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 팀 + 스코어 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamDetailBlock(
                    crestUrl = match.homeTeam.crestUrl,
                    name = match.homeTeam.name,
                    modifier = Modifier.weight(1f)
                )
                ScoreBlock(match = match, matchResult = matchResult)
                TeamDetailBlock(
                    crestUrl = match.awayTeam.crestUrl,
                    name = match.awayTeam.name,
                    modifier = Modifier.weight(1f)
                )
            }

            // 경기장 / 관중
            val venueText = when {
                venue != null && attendance != null ->
                    stringResource(R.string.match_detail_venue_attendance_format, venue, attendance)
                venue != null ->
                    stringResource(R.string.match_detail_venue_only_format, venue)
                attendance != null ->
                    stringResource(R.string.match_detail_attendance_only_format, attendance)
                else -> null
            }
            if (venueText != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = venueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // 결과 뱃지
            if (matchResult != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val (bgColor, label) = when (matchResult) {
                    MatchResult.WIN  -> ResultWin to stringResource(R.string.match_result_win)
                    MatchResult.LOSS -> MaterialTheme.colorScheme.error to stringResource(R.string.match_result_loss)
                    MatchResult.DRAW -> ResultDraw to stringResource(R.string.match_result_draw)
                }
                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)))
                        .padding(
                            horizontal = dimensionResource(R.dimen.padding_medium),
                            vertical = dimensionResource(R.dimen.badge_vertical_padding)
                        )
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamDetailBlock(crestUrl: String, name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = crestUrl,
            contentDescription = name,
            modifier = Modifier.size(dimensionResource(R.dimen.emblem_match_detail_team))
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScoreBlock(match: Match, matchResult: MatchResult?) {
    val scoreColor = when (matchResult) {
        MatchResult.WIN  -> ResultWin
        MatchResult.LOSS -> MaterialTheme.colorScheme.error
        MatchResult.DRAW -> ResultDraw
        null             -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
    ) {
        when {
            match.isFinished() && match.homeScore != null && match.awayScore != null -> {
                Text(
                    text = "${match.homeScore} - ${match.awayScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
            match.isLive() -> {
                Text(
                    text = stringResource(R.string.match_status_in_play),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.match_status_vs),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 소감 섹션
// ──────────────────────────────────────────────
@Composable
private fun ReviewSection(onWriteReview: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Text(
            text = stringResource(R.string.match_detail_section_review),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_large)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Text(
                    text = stringResource(R.string.match_detail_no_review),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onWriteReview) {
                    Text(text = stringResource(R.string.match_detail_write_review))
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// 상세 섹션 공통 래퍼
// ──────────────────────────────────────────────
@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

// ──────────────────────────────────────────────
// 라인업
// ──────────────────────────────────────────────
@Composable
private fun LineupContent(lineups: List<TeamLineup>, match: Match) {
    if (lineups.isEmpty()) {
        EmptyContent(message = stringResource(R.string.match_detail_no_lineup))
        return
    }
    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        lineups.forEachIndexed { index, lineup ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            // 팀 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                val team = if (lineup.teamId == match.homeTeam.id) match.homeTeam else match.awayTeam
                AsyncImage(
                    model = team.crestUrl,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                Text(
                    text = lineup.teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                lineup.formation?.let {
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Text(
                        text = stringResource(R.string.match_detail_formation_format, it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_xsmall)))

            // 선발
            if (lineup.startingEleven.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.match_detail_starting_eleven),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                lineup.startingEleven.forEach { player ->
                    PlayerRow(player = player)
                }
            }

            // 후보
            if (lineup.bench.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_xsmall)))
                Text(
                    text = stringResource(R.string.match_detail_bench),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                lineup.bench.forEach { player ->
                    PlayerRow(player = player)
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(player: com.wbjang.footballdiary.domain.model.LineupPlayer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        player.shirtNumber?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(dimensionResource(R.dimen.icon_toggle_button)),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        player.position?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ──────────────────────────────────────────────
// 타임라인
// ──────────────────────────────────────────────
private sealed class TimelineItem(open val minute: Int, open val injuryTime: Int?) {
    data class Goal(val event: GoalEvent) : TimelineItem(event.minute, event.injuryTime)
    data class Booking(val event: BookingEvent) : TimelineItem(event.minute, null)
    data class Substitution(val event: SubstitutionEvent) : TimelineItem(event.minute, null)
}

@Composable
private fun TimelineContent(
    goals: List<GoalEvent>,
    bookings: List<BookingEvent>,
    substitutions: List<SubstitutionEvent>,
    match: Match
) {
    val items = buildList {
        goals.forEach { add(TimelineItem.Goal(it)) }
        bookings.forEach { add(TimelineItem.Booking(it)) }
        substitutions.forEach { add(TimelineItem.Substitution(it)) }
    }.sortedWith(compareBy({ it.minute }, { it.injuryTime ?: 0 }))

    if (items.isEmpty()) {
        EmptyContent(message = stringResource(R.string.match_detail_no_timeline))
        return
    }

    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            when (item) {
                is TimelineItem.Goal -> GoalRow(event = item.event, match = match)
                is TimelineItem.Booking -> BookingRow(event = item.event, match = match)
                is TimelineItem.Substitution -> SubstitutionRow(event = item.event, match = match)
            }
        }
    }
}

@Composable
private fun GoalRow(event: GoalEvent, match: Match) {
    val teamName = teamNameById(event.teamId, match)
    val minuteText = minuteText(event.minute, event.injuryTime)
    val typeLabel = when (event.type) {
        GoalType.OWN_GOAL -> " (${stringResource(R.string.match_detail_own_goal)})"
        GoalType.PENALTY  -> " (${stringResource(R.string.match_detail_penalty)})"
        else              -> ""
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = minuteText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(dimensionResource(R.dimen.padding_xlarge))
        )
        Icon(
            imageVector = Icons.Filled.SportsSoccer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
        Column {
            Text(
                text = "${event.scorerName}$typeLabel",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            teamName?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            event.assistName?.let {
                Text(
                    text = stringResource(R.string.match_detail_goal_assist_format, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookingRow(event: BookingEvent, match: Match) {
    val teamName = teamNameById(event.teamId, match)
    val minuteText = minuteText(event.minute, null)
    val cardColor = when (event.card) {
        CardType.YELLOW     -> Color(0xFFFFD600)
        CardType.RED        -> MaterialTheme.colorScheme.error
        CardType.YELLOW_RED -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = minuteText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(dimensionResource(R.dimen.padding_xlarge))
        )
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.icon_toggle_button), dimensionResource(R.dimen.icon_size_small))
                .background(cardColor, RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)))
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
        Column {
            Text(text = event.playerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            teamName?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SubstitutionRow(event: SubstitutionEvent, match: Match) {
    val teamName = teamNameById(event.teamId, match)
    val minuteText = minuteText(event.minute, null)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_xsmall)),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = minuteText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(dimensionResource(R.dimen.padding_xlarge))
        )
        Icon(
            imageVector = Icons.Filled.SwapVert,
            contentDescription = null,
            tint = ResultWin,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
        Column {
            Row {
                Text(
                    text = "▲ ${event.playerInName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ResultWin,
                    fontWeight = FontWeight.Medium
                )
            }
            Row {
                Text(
                    text = "▼ ${event.playerOutName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            teamName?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ──────────────────────────────────────────────
// 경기 통계
// ──────────────────────────────────────────────
@Composable
private fun StatisticsContent() {
    EmptyContent(message = stringResource(R.string.match_detail_no_statistics))
}

// ──────────────────────────────────────────────
// 공통 헬퍼
// ──────────────────────────────────────────────
@Composable
private fun EmptyContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_large)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun teamNameById(teamId: Int?, match: Match): String? = when (teamId) {
    match.homeTeam.id -> match.homeTeam.shortName
    match.awayTeam.id -> match.awayTeam.shortName
    else -> null
}

private fun minuteText(minute: Int, injuryTime: Int?): String =
    if (injuryTime != null && injuryTime > 0) "$minute+$injuryTime'" else "$minute'"
