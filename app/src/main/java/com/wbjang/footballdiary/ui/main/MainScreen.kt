package com.wbjang.footballdiary.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wbjang.footballdiary.R

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val teamName by viewModel.followingTeamName.collectAsStateWithLifecycle(initialValue = null)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.main_following_team_format, teamName ?: ""),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
