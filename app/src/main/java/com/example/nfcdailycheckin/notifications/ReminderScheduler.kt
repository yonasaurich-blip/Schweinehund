package com.example.nfcdailycheckin.notifications

import android.content.Context
import androidx.work.*
import com.example.nfcdailycheckin.data.TaskEntity
import com.example.nfcdailycheckin.data.TaskRules
import com.example.nfcdailycheckin.util.DateTime
import java.time.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val KEY_TASK_ID = "taskId"

    fun scheduleNext(context: Context, task: TaskEntity) {
        if (!task.isActive) {
            cancel(context, task.id)
            return
        }

        val zone = ZoneId.systemDefault()
        val today = DateTime.today(zone)

        // 1) Nächster fälliger Tag ab heute
        val firstDue = findNextDueDate(task, today) ?: run {
            cancel(context, task.id)
            return
        }

        val now = Instant.now()

        fun triggerInstant(forDate: LocalDate): Instant =
            forDate.atTime(
                TaskRules.reminderHour(task),
                TaskRules.reminderMinute(task)
            ).atZone(zone).toInstant()

        // 2) Trigger für den gefundenen Tag
        val firstTrigger = triggerInstant(firstDue)

        // 3) WICHTIG: Wenn heute fällig, aber Uhrzeit schon vorbei -> ab morgen neu suchen
        val effectiveDue = if (firstDue == today && !firstTrigger.isAfter(now)) {
            findNextDueDate(task, today.plusDays(1))
        } else {
            firstDue
        } ?: run {
            cancel(context, task.id)
            return
        }

        val triggerAt = triggerInstant(effectiveDue)
        val delayMs = Duration.between(now, triggerAt).toMillis().coerceAtLeast(0)

        val data = workDataOf(KEY_TASK_ID to task.id)

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag(task.id))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(task.id), ExistingWorkPolicy.REPLACE, req)
    }


    fun cancel(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: Long) = "reminder_task_$taskId"
    private fun tag(taskId: Long) = "reminder_tag_$taskId"

    private fun findNextDueDate(task: TaskEntity, from: LocalDate): LocalDate? {
        // Start at today (so reminders today still schedule if in future) and search forward.
        for (i in 0..370) {
            val d = from.plusDays(i.toLong())
            if (TaskRules.isDueOn(task, d)) return d
        }
        return null
    }
}
