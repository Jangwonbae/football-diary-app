package com.wbjang.footballdiary.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.wbjang.footballdiary.domain.repository.FootballRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class MatchWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = MatchWidget()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun repository(): FootballRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        fetchAndUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        fetchAndUpdate(context)
    }


    private fun fetchAndUpdate(context: Context) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                ).repository()

                val teamId = repository.getFollowingTeamId().first() ?: return@launch
                val teamName = repository.getFollowingTeamName().first() ?: ""

                val today = LocalDate.now()
                val match = repository.getTeamMatches(
                    teamId = teamId,
                    dateFrom = today.toString(),
                    dateTo = today.plusDays(30).toString()
                ).getOrNull()
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
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(MatchWidget::class.java)
                glanceIds.forEach { glanceId ->
                    MatchWidget().update(context, glanceId)
                }
            } catch (e: Exception) {
                // 데이터 갱신 실패 시 기존 데이터 유지
            }
        }
    }
}
