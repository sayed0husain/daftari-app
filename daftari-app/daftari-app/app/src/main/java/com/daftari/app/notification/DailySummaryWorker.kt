package com.daftari.app.notification

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import com.daftari.app.DaftariApplication
import com.daftari.app.R
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as DaftariApplication
        val enabled = app.settingsDataStore.dailySummaryEnabled.first()

        if (enabled) {
            postSummaryIfAllowed(app)
        }

        // Always reschedule for tomorrow so the daily reminder keeps running,
        // even if it was just disabled (harmless — doWork() will simply skip next time).
        DailySummaryScheduler.scheduleNext(applicationContext, ExistingWorkPolicy.REPLACE)
        return Result.success()
    }

    private suspend fun postSummaryIfAllowed(app: DaftariApplication) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 24 * 60 * 60 * 1000L

        val repository = app.repository
        val exams = repository.getAllExams().first().filter { it.date in todayStart until todayEnd }
        val homework = repository.getAllHomework().first()
            .filter { !it.completed && it.dueDate in todayStart until todayEnd }

        if (exams.isEmpty() && homework.isEmpty()) return

        val dateLabel = SimpleDateFormat("EEEE dd/MM", Locale.getDefault()).format(todayStart)
        val parts = mutableListOf<String>()
        if (exams.isNotEmpty()) {
            parts.add(applicationContext.getString(R.string.daily_summary_exams_count, exams.size))
        }
        if (homework.isNotEmpty()) {
            parts.add(applicationContext.getString(R.string.daily_summary_homework_count, homework.size))
        }
        val body = parts.joinToString(" — ")

        val notification = NotificationCompat.Builder(
            applicationContext,
            DaftariApplication.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.daily_summary_title, dateLabel))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.notify(DAILY_SUMMARY_NOTIF_ID, notification)
    }

    companion object {
        const val DAILY_SUMMARY_NOTIF_ID = 777001
    }
}
