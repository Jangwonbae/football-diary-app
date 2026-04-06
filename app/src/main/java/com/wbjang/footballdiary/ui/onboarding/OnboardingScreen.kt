package com.wbjang.footballdiary.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.League
import com.wbjang.footballdiary.domain.model.Team

@Composable
fun OnboardingScreen(
    onTeamSelected: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 헤더
            OnboardingHeader()

            // 리그 리스트
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.leagues, key = { it.code }) { league ->
                    LeagueItem(
                        league = league,
                        onExpandClick = { viewModel.toggleLeagueExpand(league.code) },
                        onFollowingClick = { team -> viewModel.onFollowingClick(team) }
                    )
                    Divider(color = colorResource(R.color.divider))
                }
            }
        }

        // 팔로잉 확인 다이얼로그
        uiState.pendingTeam?.let { team ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = {
                    Text(
                        text = stringResource(R.string.dialog_following_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.dialog_following_message_format, team.name)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmFollowing(onTeamSelected) },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.green_primary)
                        )
                    ) {
                        Text(stringResource(R.string.dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) {
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
            .background(colorResource(R.color.green_primary))
            .padding(
                horizontal = dimensionResource(R.dimen.padding_large),
                vertical = dimensionResource(R.dimen.padding_xlarge)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colorResource(R.color.text_on_primary),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.text_on_primary).copy(alpha = 0.85f),
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
        // 리그 행
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
            // 리그 엠블렘
            AsyncImage(
                model = league.emblemUrl,
                contentDescription = league.name,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.emblem_league))
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            // 리그 이름 + 국가
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = league.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = league.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.text_secondary)
                )
            }
            // 화살표 아이콘
            IconButton(onClick = onExpandClick) {
                Icon(
                    imageVector = if (league.isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = colorResource(R.color.text_secondary)
                )
            }
        }

        // 팀 목록 (확장 시)
        AnimatedVisibility(
            visible = league.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.background_light))
            ) {
                when {
                    league.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.padding_large)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorResource(R.color.green_primary)
                            )
                        }
                    }
                    league.error != null -> {
                        Text(
                            text = stringResource(R.string.error_load_teams),
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                            color = colorResource(R.color.error),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        league.teams.forEach { team ->
                            TeamItem(
                                team = team,
                                onFollowingClick = { onFollowingClick(team) }
                            )
                            Divider(
                                modifier = Modifier.padding(
                                    start = dimensionResource(R.dimen.padding_xlarge)
                                ),
                                color = colorResource(R.color.divider)
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
        // 팀 엠블렘
        AsyncImage(
            model = team.crestUrl,
            contentDescription = team.name,
            modifier = Modifier
                .size(dimensionResource(R.dimen.emblem_team))
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        // 팀 이름
        Text(
            text = team.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        // 팔로잉 버튼
        OutlinedButton(
            onClick = onFollowingClick,
            shape = RoundedCornerShape(dimensionResource(R.dimen.btn_corner_radius)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(R.color.green_primary)
            )
        ) {
            Text(
                text = stringResource(R.string.btn_following),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
