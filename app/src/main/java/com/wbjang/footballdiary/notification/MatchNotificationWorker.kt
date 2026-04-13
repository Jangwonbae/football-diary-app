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
        if (!repository.getNotificationEnabled().first()) return Result.success()

        val teamId = repository.getFollowingTeamId().first() ?: return Result.success()
        val lastNotifiedMatchId = repository.getLastNotifiedMatchId().first()

        val today = LocalDate.now()
        val nextMatch = repository.getTeamMatches(
            teamId = teamId,
            dateFrom = today.toString(),
            dateTo = today.plusDays(30).toString()
        ).onFailure { return Result.retry() }
         .getOrNull()
            ?.filter { it.isUpcoming() }
            ?.minByOrNull { it.utcDate }
            ?: return Result.success()

        // 경기 당일이 아니면 스킵
        val daysUntilMatch = ChronoUnit.DAYS.between(LocalDate.now(), nextMatch.localDate())
        if (daysUntilMatch > 0) return Result.success()

        val minutesUntilMatch = Duration.between(Instant.now(), Instant.parse(nextMatch.utcDate)).toMinutes()

        if (minutesUntilMatch in 14..16 && nextMatch.id != lastNotifiedMatchId) {
            showNotification(nextMatch)
            repository.saveLastNotifiedMatchId(nextMatch.id)
        }

        return Result.success()
    }

    private fun showNotification(match: Match) {
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

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(match.id, notification)
        }
    }

    companion object {
        const val CHANNEL_ID = "match_notification"
    }
}
