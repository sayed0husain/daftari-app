package com.daftari.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * The daily summary fires at a fixed local time every day. It is implemented as a
 * self-rescheduling one-time WorkManager request rather than PeriodicWorkRequest,
 * since PeriodicWorkRequest cannot guarantee a specific time of day.
 */
object DailySummaryScheduler {

    private const val UNIQUE_WORK_NAME = "daily_summary"
    private const val HOUR_OF_DAY = 7
    private const val MINUTE = 0

    fun scheduleNext(context: Context, policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HOUR_OF_DAY)
            set(Calendar.MINUTE, MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delay = next.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(UNIQUE_WORK_NAME)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, policy, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
