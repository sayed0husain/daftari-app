package com.daftari.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Schedules a one-off reminder. [triggerAtMillis] is the absolute time the notification
     * should fire (event time minus the user-chosen offset), already computed by the caller.
     * [tag] uniquely identifies this reminder (e.g. "exam_12") so it can be replaced or cancelled.
     */
    fun schedule(context: Context, tag: String, title: String, triggerAtMillis: Long, notifId: Int) {
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putInt(ReminderWorker.KEY_ID, notifId)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelUniqueWork(tag)
    }
}
