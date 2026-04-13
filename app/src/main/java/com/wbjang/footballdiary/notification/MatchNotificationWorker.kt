package com.wbjang.footballdiary.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.util.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@HiltWorker
class MatchNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FootballRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.d(TAG, "Worker 시작")

        if (!repository.getNotificationEnabled().first()) {
            AppLogger.d(TAG, "알림 비활성화 → 종료")
            return Result.success()
        }

        val teamId = repository.getFollowingTeamId().first()
        if (teamId == null) {
            AppLogger.w(TAG, "팀 ID 없음 → 종료")
            return Result.success()
        }
        val lastNotifiedMatchId = repository.getLastNotifiedMatchId().first()

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

        val nextMatch = matchesResult.getOrNull()
            ?.filter { it.isUpcoming() }
            ?.minByOrNull { it.utcDate }

        if (nextMatch == null) {
            AppLogger.d(TAG, "다가오는 경기 없음 → 종료")
            return Result.success()
        }

        val daysUntilMatch = ChronoUnit.DAYS.between(LocalDate.now(), nextMatch.localDate())
        if (daysUntilMatch > 0) {
            AppLogger.d(TAG, "경기 당일 아님 (${daysUntilMatch}일 남음) → 스킵")
            return Result.success()
        }

        val minutesUntilMatch = Duration.between(Instant.now(), Instant.parse(nextMatch.utcDate)).toMinutes()
        AppLogger.d(TAG, "다음 경기까지 ${minutesUntilMatch}분 남음 (matchId=${nextMatch.id})")

        if (minutesUntilMatch in 14..16) {
            if (nextMatch.id == lastNotifiedMatchId) {
                AppLogger.d(TAG, "이미 알림 발송한 경기 → 스킵 (matchId=${nextMatch.id})")
            } else {
                AppLogger.d(TAG, "알림 발송: ${nextMatch.homeTeam.shortName} vs ${nextMatch.awayTeam.shortName}")
                if (showNotification(nextMatch)) {
                    repository.saveLastNotifiedMatchId(nextMatch.id)
                }
            }
        } else {
            AppLogger.d(TAG, "알림 발송 조건 미충족 (14~16분 범위 아님: ${minutesUntilMatch}분)")
        }

        return Result.success()
    }

    private fun showNotification(match: Match): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            AppLogger.w(TAG, "시스템 알림 권한 없음 → 알림 발송 불가")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                context.getString(
                    R.string.notification_body_format,
                    match.homeTeam.shortName,
                    match.awayTeam.shortName
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(match.id, notification)
        return true
    }

    companion object {
        const val CHANNEL_ID = "match_notification"
        private const val TAG = "MatchNotificationWorker"
    }
}
