package com.wbjang.footballdiary.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wbjang.footballdiary.domain.repository.FootballRepository
import com.wbjang.footballdiary.util.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FootballRepository
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun refreshScheduling() {
        cancel()

        if (!repository.getNotificationEnabled().first()) {
            AppLogger.d(TAG, "알림 OFF → 예약 안 함")
            return
        }
        val teamId = repository.getFollowingTeamId().first() ?: run {
            AppLogger.d(TAG, "팀 미선택 → 예약 안 함")
            return
        }

        if (!canScheduleExact()) {
            AppLogger.w(TAG, "SCHEDULE_EXACT_ALARM 권한 없음 → 예약 불가")
            return
        }

        val today = LocalDate.now()
        val matchesResult = repository.getTeamMatches(
            teamId = teamId,
            dateFrom = today.toString(),
            dateTo = today.plusDays(60).toString()
        )

        val nextMatch = matchesResult.getOrNull()
            ?.filter { it.isUpcoming() }
            ?.minByOrNull { it.utcDate }
            ?: run {
                AppLogger.d(TAG, "다가오는 경기 없음 → 예약 안 함")
                return
            }

        val matchInstant = Instant.parse(nextMatch.utcDate)
        val triggerAtMillis = matchInstant.minus(Duration.ofMinutes(15)).toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) {
            AppLogger.d(TAG, "이미 15분 이내 → 예약 생략")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent(nextMatch.id)
        )
        AppLogger.d(
            TAG,
            "알람 예약: matchId=${nextMatch.id}, triggerAt=${Instant.ofEpochMilli(triggerAtMillis)}"
        )
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent(0))
        AppLogger.d(TAG, "알람 취소")
    }

    private fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

    private fun pendingIntent(matchId: Int): PendingIntent {
        val intent = Intent(context, MatchNotificationReceiver::class.java).apply {
            putExtra(EXTRA_MATCH_ID, matchId)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val REQUEST_CODE = 1001
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}
