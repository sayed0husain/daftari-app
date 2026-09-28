package com.daftari.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.daftari.app.data.AppDatabase
import com.daftari.app.repository.AppRepository
import com.daftari.app.settings.SettingsDataStore

class DaftariApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AppRepository by lazy {
        AppRepository(
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao(),
            examDao = database.examDao(),
            homeworkDao = database.homeworkDao(),
            notebookCheckDao = database.notebookCheckDao(),
            attendanceDao = database.attendanceDao(),
            behaviorGradeDao = database.behaviorGradeDao(),
            scheduleSlotDao = database.scheduleSlotDao()
        )
    }
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "daftari_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
