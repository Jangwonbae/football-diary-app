package com.wbjang.footballdiary.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MatchNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: FootballRepository
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val matchId = intent.getIntExtra(NotificationScheduler.EXTRA_MATCH_ID, -1)
        AppLogger.d(TAG, "알람 수신: matchId=$matchId")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (repository.getNotificationEnabled().first()) {
                    sendNotification(context, matchId)
                } else {
                    AppLogger.d(TAG, "알림 OFF 상태 → 발송 생략")
                }
                scheduler.refreshScheduling()
            } catch (e: Exception) {
                AppLogger.e(TAG, "알림 처리 실패", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sendNotification(context: Context, matchId: Int) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            AppLogger.w(TAG, "시스템 알림 권한 없음 → 발송 불가")
            return
        }

        val teamId = repository.getFollowingTeamId().first() ?: return
        val match = repository.getTeamMatches(
            teamId = teamId,
            dateFrom = java.time.LocalDate.now().toString(),
            dateTo = java.time.LocalDate.now().plusDays(1).toString()
        ).getOrNull()?.firstOrNull { it.id == matchId } ?: run {
            AppLogger.w(TAG, "matchId=$matchId 정보 조회 실패 → 발송 생략")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
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

        NotificationManagerCompat.from(context).notify(matchId, notification)
        AppLogger.d(TAG, "알림 발송 완료: matchId=$matchId")
    }

    companion object {
        private const val TAG = "MatchNotificationReceiver"
        const val CHANNEL_ID = "match_notification"
    }
}
