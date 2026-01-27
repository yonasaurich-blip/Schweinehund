package com.example.nfcdailycheckin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScheduleType { DAILY, WEEKLY, MONTHLY, ONCE }

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scheduleType: ScheduleType,
    /** 1=Monday ... 7=Sunday */
    val weeklyDay: Int? = null,
    /** 1..31 */
    val monthlyDay: Int? = null,
    /** ISO date (YYYY-MM-DD) */
    val onceDateIso: String? = null,
    /** Minutes since 00:00, e.g. 20:30 = 1230 */
    val reminderTimeMinutes: Int,
    /** TEXT part of nfctext::TEXT */
    val nfcText: String,
    val isActive: Boolean = true,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
