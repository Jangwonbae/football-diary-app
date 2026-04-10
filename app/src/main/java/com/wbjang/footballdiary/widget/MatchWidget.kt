package com.wbjang.footballdiary.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wbjang.footballdiary.MainActivity
import com.wbjang.footballdiary.R
import java.time.format.DateTimeFormatter
import java.util.Locale

class MatchWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val match = WidgetPreferences.getMatch(context)
        val teamName = WidgetPreferences.getTeamName(context)

        provideContent {
            GlanceTheme {
                WidgetContent(match = match, teamName = teamName)
            }
        }
    }
}


@SuppressLint("RestrictedApi")
@Composable
fun WidgetContent(match: WidgetMatch?, teamName: String) {
    val context = LocalContext.current
    val textPrimary = ColorProvider(R.color.widget_text_primary)
    val textSecondary = ColorProvider(R.color.widget_text_secondary)

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            style = TextStyle(
                color = textSecondary,
                fontSize = 10.sp
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        if (match != null) {
            val formattedDate = try {
                val dateFormatStr = context.getString(R.string.date_format_match_datetime)
                val formatter = DateTimeFormatter.ofPattern(dateFormatStr, Locale.KOREAN)
                match.localDateTime().format(formatter)
            } catch (e: Exception) { "-" }

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = match.homeTeamShortName,
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = context.getString(R.string.widget_vs),
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = match.awayTeamShortName,
                    style = TextStyle(
                        color = textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = context.getString(R.string.widget_scheduled_format, formattedDate),
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 9.sp
                )
            )
        } else {
            Text(
                text = context.getString(R.string.widget_no_matches),
                style = TextStyle(
                    color = textSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 80)
@Composable
fun WidgetContentPreview() {
    GlanceTheme {
        WidgetContent(
            match = WidgetMatch(
                homeTeamShortName = "MCI",
                awayTeamShortName = "ARS",
                utcDate = "2026-04-15T19:00:00Z",
                competitionName = "Premier League"
            ),
            teamName = "Manchester City"
        )
    }
}
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 80)
@Composable
fun WidgetNullContentPreview() {
    GlanceTheme {
        WidgetContent(
            match = null,
            teamName = "Manchester City"
        )
    }
}
