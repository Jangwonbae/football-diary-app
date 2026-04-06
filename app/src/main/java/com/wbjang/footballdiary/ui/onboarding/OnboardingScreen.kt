package com.wbjang.footballdiary.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.League
import com.wbjang.footballdiary.domain.model.Team
import com.wbjang.footballdiary.ui.theme.FootballDiaryTheme

@Composable
fun OnboardingScreen(
    onTeamSelected: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingContent(
        uiState = uiState,
        onTeamSelected = onTeamSelected,
        onExpandClick = { viewModel.toggleLeagueExpand(it) },
        onFollowingClick = { viewModel.onFollowingClick(it) },
        onConfirmFollowing = { viewModel.confirmFollowing(onTeamSelected) },
        onDismissDialog = { viewModel.dismissDialog() }
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    onTeamSelected: () -> Unit,
    onExpandClick: (String) -> Unit,
    onFollowingClick: (Team) -> Unit,
    onConfirmFollowing: () -> Unit,
    onDismissDialog: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.leagues, key = { it.code }) { league ->
                    LeagueItem(
                        league = league,
                        onExpandClick = { onExpandClick(league.code) },
                        onFollowingClick = onFollowingClick
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        uiState.pendingTeam?.let { team ->
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = {
                    Text(
                        text = stringResource(R.string.dialog_following_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(stringResource(R.string.dialog_following_message_format, team.name))
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmFollowing,
                        enabled = !uiState.isSaving
                    ) {
                        Text(stringResource(R.string.dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun OnboardingHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(
                horizontal = dimensionResource(R.dimen.padding_large),
                vertical = dimensionResource(R.dimen.padding_xlarge)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LeagueItem(
    league: League,
    onExpandClick: () -> Unit,
    onFollowingClick: (Team) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_medium),
                    vertical = dimensionResource(R.dimen.padding_small)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = league.emblemUrl,
                contentDescription = league.name,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.emblem_league))
//                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = league.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = league.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onExpandClick) {
                Icon(
                    imageVector = if (league.isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = league.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                when {
                    league.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_large)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    league.error != null -> {
                        Text(
                            text = stringResource(R.string.error_load_teams),
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        league.teams.forEach { team ->
                            TeamItem(
                                team = team,
                                onFollowingClick = { onFollowingClick(team) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = dimensionResource(R.dimen.padding_xlarge)
                                ),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamItem(
    team: Team,
    onFollowingClick: () -> Unit
) {
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
            model = team.crestUrl,
            contentDescription = team.name,
            modifier = Modifier
                .size(dimensionResource(R.dimen.emblem_team))
//                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        Text(
            text = team.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = onFollowingClick,
            shape = RoundedCornerShape(dimensionResource(R.dimen.btn_corner_radius))
        ) {
            Text(
                text = stringResource(R.string.btn_following),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingHeader() {
    FootballDiaryTheme {
        OnboardingHeader()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeagueItem() {
    FootballDiaryTheme {
        LeagueItem(
            league = League(
                code = "PL",
                name = "프리미어리그",
                country = "잉글랜드",
                emblemUrl = ""
            ),
            onExpandClick = {},
            onFollowingClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTeamItem() {
    FootballDiaryTheme {
        TeamItem(
            team = Team(id = 1, name = "아스널 FC", shortName = "Arsenal", crestUrl = ""),
            onFollowingClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewOnboardingScreen() {
    FootballDiaryTheme {
        OnboardingContent(
            uiState = OnboardingUiState(
                leagues = listOf(
                    League(
                        code = "PL",
                        name = "프리미어리그",
                        country = "잉글랜드",
                        emblemUrl = "",
                        isExpanded = true,
                        teams = listOf(
                            Team(id = 1, name = "아스널 FC", shortName = "Arsenal", crestUrl = ""),
                            Team(id = 2, name = "맨체스터 시티 FC", shortName = "Man City", crestUrl = "")
                        )
                    ),
                    League(
                        code = "PD",
                        name = "라리가",
                        country = "스페인",
                        emblemUrl = ""
                    )
                )
            ),
            onTeamSelected = {},
            onExpandClick = {},
            onFollowingClick = {},
            onConfirmFollowing = {},
            onDismissDialog = {}
        )
    }
}
