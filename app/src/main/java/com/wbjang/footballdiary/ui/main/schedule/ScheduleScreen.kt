package com.wbjang.footballdiary.ui.main.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            onToggle = { viewModel.toggleViewMode() }
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
                    onPreviousMonth = { viewModel.goToPreviousMonth() },
                    onNextMonth = { viewModel.goToNextMonth() }
                )
            }
            else -> {
                MatchListView(matches = uiState.matches)
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
                .size(48.dp)
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
private fun ViewModeToggle(isCalendarMode: Boolean, onToggle: () -> Unit) {
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
                selected = isCalendarMode,
                onClick = { if (!isCalendarMode) onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.tab_schedule_calendar))
            }
            SegmentedButton(
                selected = !isCalendarMode,
                onClick = { if (isCalendarMode) onToggle() },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.tab_schedule_list))
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
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
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
                text = yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월")),
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(0.8f)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 날짜 숫자
        Box(
            modifier = Modifier
                .size(26.dp)
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
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = match.homeTeam.crestUrl,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "v",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7,
                    modifier = Modifier.padding(horizontal = 1.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AsyncImage(
                    model = match.awayTeam.crestUrl,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
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
private fun MatchListView(matches: List<Match>) {
    val today = LocalDate.now()

    // 다가오는 가장 빠른 경기 인덱스
    val upcomingIndex = matches.indexOfFirst { match ->
        match.localDate() >= today && (match.isUpcoming() || match.isLive())
    }.takeIf { it != -1 } ?: 0

    val listState = rememberLazyListState()

    LaunchedEffect(upcomingIndex) {
        if (upcomingIndex > 0) {
            listState.animateScrollToItem((upcomingIndex - 1).coerceAtLeast(0))
        }
    }

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
                isUpcoming = index == upcomingIndex
            )
        }
        item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium))) }
    }
}

@Composable
private fun MatchListCard(match: Match, isUpcoming: Boolean) {
    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E) HH:mm", Locale.KOREAN)
    val formattedDate = match.localDateTime().format(dateFormatter)

    val statusLabel = when (match.status) {
        MatchStatus.FINISHED   -> if (match.homeScore != null) "${match.homeScore} - ${match.awayScore}" else "종료"
        MatchStatus.IN_PLAY    -> "진행중"
        MatchStatus.PAUSED     -> "휴식"
        MatchStatus.POSTPONED  -> "연기"
        MatchStatus.CANCELLED  -> "취소"
        else                   -> "VS"
    }

    val borderMod = if (isUpcoming) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = 4.dp)
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
                if (isUpcoming) {
                    Text(
                        text = stringResource(R.string.schedule_next_match),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
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

                // 스코어 또는 VS
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small)),
                    color = when (match.status) {
                        MatchStatus.IN_PLAY -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

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
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = shortName,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
