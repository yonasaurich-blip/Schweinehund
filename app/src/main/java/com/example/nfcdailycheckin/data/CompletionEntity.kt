package com.example.nfcdailycheckin.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completions",
    indices = [Index(value = ["taskId", "dateIso"], unique = true)]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    /** ISO date (YYYY-MM-DD) */
    val dateIso: String,
    val completedAtEpochMs: Long,
    val method: String = "NFC",
)
