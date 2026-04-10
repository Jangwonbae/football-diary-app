package com.wbjang.footballdiary.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.wbjang.footballdiary.MainActivity

class MatchWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val matches = WidgetPreferences.getMatches(context)
        val teamName = WidgetPreferences.getTeamName(context)

        provideContent {
            GlanceTheme {
                WidgetContent(matches = matches, teamName = teamName)
            }
        }
    }
}

@Composable
private fun WidgetContent(matches: List<WidgetMatch>, teamName: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Text(
            text = if (teamName.isNotEmpty()) teamName else "다가오는 경기",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (matches.isEmpty()) {
            Text(
                text = "다가오는 경기가 없습니다",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        } else {
            matches.forEach { match ->
                MatchRow(match = match)
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MatchRow(match: WidgetMatch) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = match.formattedDate,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp
            ),
            modifier = GlanceModifier.width(72.dp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "${match.homeTeamShortName} vs ${match.awayTeamShortName}",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 12.sp
            )
        )
    }
}
