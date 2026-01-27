package com.example.nfcdailycheckin.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

object TaskRules {
    fun isDueOn(task: TaskEntity, date: LocalDate): Boolean {
        if (!task.isActive) return false
        return when (task.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKLY -> task.weeklyDay == date.dayOfWeek.value
            ScheduleType.MONTHLY -> {
                val day = task.monthlyDay ?: return false
                val ym = YearMonth.from(date)
                val last = ym.lengthOfMonth()
                val effective = if (day > last) last else day
                date.dayOfMonth == effective
            }
            ScheduleType.ONCE -> task.onceDateIso == date.toString()
        }
    }

    fun reminderHour(task: TaskEntity): Int = task.reminderTimeMinutes / 60
    fun reminderMinute(task: TaskEntity): Int = task.reminderTimeMinutes % 60
}
