package com.wbjang.footballdiary.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FootballRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val teamId = repository.getFollowingTeamId().first() ?: return Result.success()
        val teamName = repository.getFollowingTeamName().first() ?: ""

        val today = LocalDate.now()
        val match = repository.getTeamMatches(
            teamId = teamId,
            dateFrom = today.toString(),
            dateTo = today.plusDays(30).toString()
        ).onFailure { return Result.retry() }
         .getOrNull()
            ?.filter { it.isUpcoming() }
            ?.minByOrNull { it.utcDate }
            ?.let { m ->
                WidgetMatch(
                    homeTeamShortName = m.homeTeam.shortName,
                    awayTeamShortName = m.awayTeam.shortName,
                    utcDate = m.utcDate,
                    competitionName = m.competition?.name
                )
            }

        WidgetPreferences.saveData(context, teamName, match)
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(MatchWidget::class.java)
        glanceIds.forEach { MatchWidget().update(context, it) }
        return Result.success()
    }
}
