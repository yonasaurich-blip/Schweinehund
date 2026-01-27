package com.example.nfcdailycheckin.ui

import com.example.nfcdailycheckin.data.ScheduleType

data class TaskDraft(
    val title: String,
    val scheduleType: ScheduleType,
    val weeklyDay: Int? = null,
    val monthlyDay: Int? = null,
    val onceDateIso: String? = null,
    val reminderMinutes: Int,
    val nfcText: String,
)
