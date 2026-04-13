package com.wbjang.footballdiary.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.util.AppLogger
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
        AppLogger.d(TAG, "Worker 시작")

        val teamId = repository.getFollowingTeamId().first()
        if (teamId == null) {
            AppLogger.w(TAG, "팀 ID 없음 → 종료")
            return Result.success()
        }
        val teamName = repository.getFollowingTeamName().first() ?: ""

        val today = LocalDate.now()
        val matchesResult = repository.getTeamMatches(
            teamId = teamId,
            dateFrom = today.toString(),
            dateTo = today.plusDays(30).toString()
        )

        matchesResult.onFailure { e ->
            AppLogger.e(TAG, "API 호출 실패 → 재시도", e)
            return Result.retry()
        }

        val match = matchesResult.getOrNull()
            ?.filter { it.isUpcoming() }
            ?.minByOrNull { it.utcDate }
            ?.let { m ->
                AppLogger.d(TAG, "다음 경기: matchId=${m.id}, ${m.homeTeam.shortName} vs ${m.awayTeam.shortName}, utcDate=${m.utcDate}")
                WidgetMatch(
                    homeTeamShortName = m.homeTeam.shortName,
                    awayTeamShortName = m.awayTeam.shortName,
                    utcDate = m.utcDate,
                    competitionName = m.competition?.name
                )
            }

        if (match == null) AppLogger.w(TAG, "다가오는 경기 없음")

        WidgetPreferences.saveData(context, teamName, match)
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(MatchWidget::class.java)
        glanceIds.forEach { MatchWidget().update(context, it) }

        AppLogger.d(TAG, "Worker 완료 (위젯 ${glanceIds.size}개 업데이트)")
        return Result.success()
    }

    companion object {
        private const val TAG = "WidgetUpdateWorker"
    }
}
