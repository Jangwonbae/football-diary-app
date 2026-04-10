package com.wbjang.footballdiary.ui.main.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.wbjang.footballdiary.widget.MatchWidgetReceiver
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateToOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val followingTeamName by viewModel.followingTeamName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showThemeDialog) {
        ThemeDialog(
            currentMode = themeMode,
            onSelect = {
                viewModel.saveThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 제목
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = dimensionResource(R.dimen.padding_large)
            )
        )

        // ── 팀 섹션 ──
        SettingsSectionHeader(stringResource(R.string.settings_section_team))

        SettingsItem(
            icon = Icons.Default.People,
            title = stringResource(R.string.settings_following_team_change),
            subtitle = followingTeamName ?: stringResource(R.string.settings_following_team_desc),
            onClick = onNavigateToOnboarding
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))

        // ── 앱 섹션 ──
        SettingsSectionHeader(stringResource(R.string.settings_section_app))

        SettingsItem(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.settings_notification),
            subtitle = stringResource(R.string.settings_notification_desc),
            onClick = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))

        SettingsItem(
            icon = Icons.Default.Widgets,
            title = stringResource(R.string.settings_widget),
            subtitle = stringResource(R.string.settings_widget_desc),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val provider = ComponentName(context, MatchWidgetReceiver::class.java)
                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        appWidgetManager.requestPinAppWidget(provider, null, null)
                    }
                }
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium)))

        SettingsItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_theme),
            subtitle = stringResource(themeMode.labelRes()),
            onClick = { showThemeDialog = true }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            horizontal = dimensionResource(R.dimen.padding_medium),
            vertical = dimensionResource(R.dimen.padding_small)
        )
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.padding_medium),
                vertical = dimensionResource(R.dimen.padding_medium)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small)),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeDialog(
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_theme_dialog_title)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = dimensionResource(R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
                        Text(
                            text = stringResource(mode.labelRes()),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}

private fun ThemeMode.labelRes() = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT  -> R.string.settings_theme_light
    ThemeMode.DARK   -> R.string.settings_theme_dark
}
