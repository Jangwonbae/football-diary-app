package com.wbjang.footballdiary.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wbjang.footballdiary.util.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MatchScheduleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.d(TAG, "일정 동기화 시작")
        return try {
            scheduler.refreshScheduling()
            AppLogger.d(TAG, "동기화 완료")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "동기화 실패 → 재시도", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "MatchScheduleSyncWorker"
        const val WORK_NAME = "match_schedule_sync"
    }
}
