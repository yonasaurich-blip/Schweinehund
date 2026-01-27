package com.example.nfcdailycheckin.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nfcdailycheckin.data.AppDatabase
import com.example.nfcdailycheckin.data.TaskRules
import com.example.nfcdailycheckin.util.DateTime
import java.time.LocalDate

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(ReminderScheduler.KEY_TASK_ID, -1L)
        if (taskId <= 0) return Result.failure()

        val db = AppDatabase.get(applicationContext)
        val task = db.taskDao().getById(taskId) ?: return Result.success()

        val today = DateTime.today()
        if (!TaskRules.isDueOn(task, today)) {
            ReminderScheduler.scheduleNext(applicationContext, task)
            return Result.success()
        }

        val done = db.completionDao().get(task.id, today.toString()) != null
        if (!done) {
            NotificationHelper.showReminder(
                context = applicationContext,
                taskTitle = task.title,
                notificationId = task.id.toInt()
            )
        }

        ReminderScheduler.scheduleNext(applicationContext, task)
        return Result.success()
    }
}
