package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.IlmPrefsKeys
import com.example.data.ilmSettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.ilmSettingsDataStore.data.first()
        val userName = prefs[IlmPrefsKeys.USER_NAME].orEmpty()
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        DailyReminderNotificationHelper.showReminderNotification(
            context = applicationContext,
            rawUserName = userName,
            dayIndex = dayOfYear
        )
        return Result.success()
    }
}
