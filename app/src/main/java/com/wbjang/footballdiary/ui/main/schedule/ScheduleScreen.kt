package com.wbjang.footballdiary.ui.main.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.annotation.DimenRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchResult
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam
import com.wbjang.footballdiary.domain.model.resultFor
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme
import com.wbjang.footballdiary.ui.theme.ResultDraw
import com.wbjang.footballdiary.ui.theme.ResultWin
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleScreen(
    onMatchClick: (Match) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reviewedMatchIds by viewModel.reviewedMatchIds.collectAsStateWithLifecycle()

    // 뷰 전환과 무관하게 리스트 상태 유지
    val listState = rememberLazyListState()
    // 재클릭 시 다음 경기로 스크롤 트리거 (값이 바뀔 때마다 스크롤 실행)
    var scrollTrigger by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()
    val upcomingIndex = remember(uiState.matches) {
        uiState.matches.indexOfFirst { match ->
            match.localDate() >= today && (match.isUpcoming() || match.isLive())
        }.takeIf { it != -1 } ?: 0
    }

    // 경기 목록이 처음 로드될 때 한 번만 다음 경기로 스크롤
    LaunchedEffect(uiState.matches.isNotEmpty()) {
        if (uiState.matches.isNotEmpty() && !viewModel.hasScrolledToUpcoming) {
            listState.animateScrollToItem((upcomingIndex - 1).coerceAtLeast(0))
            viewModel.markScrolled()
        }
    }

    // 리스트 버튼 재클릭 시 다음 경기로 스크롤
    LaunchedEffect(scrollTrigger) {
        if (scrollTrigger > 0) {
            listState.animateScrollToItem((upcomingIndex - 1).coerceAtLeast(0))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 팀 헤더
        TeamHeader(
            teamName = uiState.followingTeamName ?: "",
            teamCrestUrl = uiState.followingTeamCrestUrl ?: ""
        )

        HorizontalDivider()

        // 뷰 모드 토글
        ViewModeToggle(
            isCalendarMode = uiState.isCalendarMode,
            onToggle = { viewModel.toggleViewMode() },
            onCalendarReClick = { viewModel.resetToCurrentMonth() },
            onListReClick = { scrollTrigger++ }
        )

        // 콘텐츠
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.error_load_teams),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.isCalendarMode -> {
                CalendarView(
                    yearMonth = uiState.currentYearMonth,
                    matches = uiState.matches,
                    followingTeamId = uiState.followingTeamId,
                    onPreviousMonth = { viewModel.goToPreviousMonth() },
                    onNextMonth = { viewModel.goToNextMonth() },
                    onMatchClick = onMatchClick
                )
            }
            else -> {
                MatchListView(
                    matches = uiState.matches,
                    followingTeamId = uiState.followingTeamId,
                    reviewedMatchIds = reviewedMatchIds,
                    listState = listState,
                    upcomingIndex = upcomingIndex,
                    onMatchClick = onMatchClick
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 팀 헤더
// ──────────────────────────────────────────────
@Composable
private fun TeamHeader(teamName: String, teamCrestUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = dimensionResource(R.dimen.padding_small)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = teamCrestUrl,
            contentDescription = teamName,
            modifier = Modifier
                .size(dimensionResource(R.dimen.emblem_schedule_header))
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// ──────────────────────────────────────────────
// 뷰 모드 토글
// ──────────────────────────────────────────────
@Composable
private fun ViewModeToggle(
    isCalendarMode: Boolean,
    onToggle: () -> Unit,
    onCalendarReClick: () -> Unit,
    onListReClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = dimensionResource(R.dimen.padding_small)
            ),
        horizontalArrangement = Arrangement.Center
    ) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = !isCalendarMode,
                onClick = { if (!isCalendarMode) onListReClick() else onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(dimensionResource(R.dimen.icon_toggle_button)))
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_xsmall)))
//                Text(stringResource(R.string.tab_schedule_list))
            }
            SegmentedButton(
                selected = isCalendarMode,
                onClick = { if (isCalendarMode) onCalendarReClick() else onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(dimensionResource(R.dimen.icon_toggle_button)))
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_xsmall)))
//                Text(stringResource(R.string.tab_schedule_calendar))
            }
        }
    }
}

// ──────────────────────────────────────────────
// 캘린더 뷰
// ──────────────────────────────────────────────
@Composable
private fun CalendarView(
    yearMonth: YearMonth,
    matches: List<Match>,
    followingTeamId: Int?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMatchClick: (Match) -> Unit
) {
    val matchesByDate = matches.groupBy { it.localDate() }
    val today = LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        // 월 네비게이션 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern(stringResource(R.string.date_format_year_month))),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        // 요일 헤더 (월~일)
        Row(modifier = Modifier.fillMaxWidth()) {
            val dayNames = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            dayNames.forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.NARROW, Locale.KOREAN),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (dow) {
                        DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary
                        DayOfWeek.SUNDAY   -> MaterialTheme.colorScheme.error
                        else               -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.calendar_divider_padding)))

        // 날짜 그리드
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1 // 월=0, 일=6
        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col
                    val day = dayIndex - firstDayOffset + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(0.8f))
                    } else {
                        val date = yearMonth.atDay(day)
                        CalendarDayCell(
                            day = day,
                            isToday = date == today,
                            isPast = date < today,
                            dayOfWeek = date.dayOfWeek,
                            matches = matchesByDate[date] ?: emptyList(),
                            followingTeamId = followingTeamId,
                            onMatchClick = onMatchClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isPast: Boolean,
    dayOfWeek: DayOfWeek,
    matches: List<Match>,
    followingTeamId: Int?,
    onMatchClick: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(0.8f)
            .then(
                if (matches.isNotEmpty())
                    Modifier.clickable { onMatchClick(matches.first()) }
                else Modifier
            )
            .padding(dimensionResource(R.dimen.calendar_day_cell_padding)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 날짜 숫자
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.calendar_day_number_size))
                .then(
                    if (isToday) Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    isPast  -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    dayOfWeek == DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary
                    dayOfWeek == DayOfWeek.SUNDAY   -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // 경기 엠블렘 (있을 경우)
        if (matches.isNotEmpty()) {
            val match = matches.first()
            Row(
                modifier = Modifier.padding(top = dimensionResource(R.dimen.calendar_crest_top_padding)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = match.homeTeam.crestUrl,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.emblem_calendar_crest))
                )
                Text(
                    text = stringResource(R.string.match_vs_separator),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.calendar_crest_vs_padding)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AsyncImage(
                    model = match.awayTeam.crestUrl,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.emblem_calendar_crest))
                )
            }
            // 종료된 경기 결과 뱃지
            val result = followingTeamId?.let { match.resultFor(it) }
            if (result != null && match.homeScore != null && match.awayScore != null) {
                MatchResultBadge(
                    homeScore = match.homeScore,
                    awayScore = match.awayScore,
                    result = result,
                    compact = true
                )
            }
            // 2경기 이상이면 +N 표시
            if (matches.size > 1) {
                Text(
                    text = "+${matches.size - 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 리스트 뷰
// ──────────────────────────────────────────────
@Composable
private fun MatchListView(
    matches: List<Match>,
    followingTeamId: Int?,
    reviewedMatchIds: Set<Int>,
    listState: LazyListState,
    upcomingIndex: Int,
    onMatchClick: (Match) -> Unit
) {
    if (matches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.schedule_no_matches),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(matches) { index, match ->
            MatchListCard(
                match = match,
                followingTeamId = followingTeamId,
                hasReview = match.id in reviewedMatchIds,
                isUpcoming = index == upcomingIndex,
                onClick = { onMatchClick(match) }
            )
        }
        item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium))) }
    }
}

@Composable
private fun MatchListCard(match: Match, followingTeamId: Int?, hasReview: Boolean, isUpcoming: Boolean, onClick: () -> Unit) {
    val dateFormatStr = stringResource(R.string.date_format_match_datetime)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormatStr, Locale.KOREAN)
    val formattedDate = match.localDateTime().format(dateFormatter)

    val matchResult = followingTeamId?.let { match.resultFor(it) }

    val statusLabel = when (match.status) {
        MatchStatus.FINISHED   -> null  // 뱃지로 표시
        MatchStatus.IN_PLAY    -> stringResource(R.string.match_status_in_play)
        MatchStatus.PAUSED     -> stringResource(R.string.match_status_paused)
        MatchStatus.POSTPONED  -> stringResource(R.string.match_status_postponed)
        MatchStatus.CANCELLED  -> stringResource(R.string.match_status_cancelled)
        else                   -> stringResource(R.string.match_status_vs)
    }

    val borderMod = if (isUpcoming) {
        Modifier.border(dimensionResource(R.dimen.match_card_border_width), MaterialTheme.colorScheme.primary, RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)))
    } else Modifier

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.match_card_vertical_padding))
            .then(borderMod),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = if (isUpcoming)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            // 날짜/시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isUpcoming)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xsmall)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasReview) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_toggle_button)),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isUpcoming) {
                        Text(
                            text = stringResource(R.string.schedule_next_match),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            // 홈 팀 vs 원정 팀
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 홈 팀
                TeamBlock(
                    crestUrl = match.homeTeam.crestUrl,
                    shortName = match.homeTeam.shortName,
                    modifier = Modifier.weight(1f)
                )

                // 대회 아이콘 + 스코어 뱃지 또는 상태 텍스트
                Column(
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    match.competition?.emblemUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = match.competition.name,
                            modifier = Modifier.size(dimensionResource(R.dimen.emblem_competition))
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_xsmall)))
                    }
                    if (matchResult != null && match.homeScore != null && match.awayScore != null) {
                        MatchResultBadge(
                            homeScore = match.homeScore,
                            awayScore = match.awayScore,
                            result = matchResult,
                            compact = false
                        )
                    } else {
                        Text(
                            text = statusLabel ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (match.status) {
                                MatchStatus.IN_PLAY -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                // 원정 팀
                TeamBlock(
                    crestUrl = match.awayTeam.crestUrl,
                    shortName = match.awayTeam.shortName,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 승/패/무 결과 뱃지
// ──────────────────────────────────────────────
@Composable
private fun MatchResultBadge(
    homeScore: Int,
    awayScore: Int,
    result: MatchResult,
    compact: Boolean
) {
    val bgColor = when (result) {
        MatchResult.WIN  -> ResultWin
        MatchResult.LOSS -> MaterialTheme.colorScheme.error
        MatchResult.DRAW -> ResultDraw
    }
    val horizontalPad = dimensionResource(if (compact) R.dimen.badge_horizontal_padding_compact else R.dimen.badge_horizontal_padding)
    val verticalPad   = dimensionResource(if (compact) R.dimen.badge_vertical_padding_compact else R.dimen.badge_vertical_padding)
    val fontSize      = textSizeResource(if (compact) R.dimen.text_size_badge_compact else R.dimen.text_size_badge)

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)))
            .padding(horizontal = horizontalPad, vertical = verticalPad),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$homeScore - $awayScore",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            lineHeight = if (compact) fontSize else TextUnit.Unspecified
        )
    }
}

@Composable
private fun TeamBlock(crestUrl: String, shortName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = crestUrl,
            contentDescription = shortName,
            modifier = Modifier.size(dimensionResource(R.dimen.emblem_match_card))
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.match_card_team_spacer)))
        Text(
            text = shortName,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun textSizeResource(@DimenRes id: Int): TextUnit {
    val sizePx = LocalContext.current.resources.getDimension(id)
    return with(LocalDensity.current) { sizePx.toSp() }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun PreviewTeamHeader() {
    FootballDiaryTheme {
        TeamHeader(teamName = "아스널 FC", teamCrestUrl = "")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewViewModeToggle() {
    FootballDiaryTheme {
        ViewModeToggle(isCalendarMode = true, onToggle = {}, onCalendarReClick = {}, onListReClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMatchListCard() {
    FootballDiaryTheme {
        MatchListCard(
            match = Match(
                id = 1,
                utcDate = "2026-04-10T15:00:00Z",
                status = MatchStatus.FINISHED,
                matchday = 32,
                competition = null,
                homeTeam = MatchTeam(1, "Arsenal FC", "Arsenal", ""),
                awayTeam = MatchTeam(2, "Chelsea FC", "Chelsea", ""),
                homeScore = 2,
                awayScore = 1
            ),
            followingTeamId = 1,
            hasReview = true,
            isUpcoming = false,
            onClick = {}
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun PreviewCalendarView() {
//    FootballDiaryTheme {
//        CalendarView(
//            yearMonth = YearMonth.now(),
//            matches = emptyList(),
//            followingTeamId = 1,
//            onPreviousMonth = {},
//            onNextMonth = {}
//        )
//    }
//}
