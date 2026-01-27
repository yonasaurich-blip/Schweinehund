package com.example.nfcdailycheckin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY id ASC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Query("UPDATE tasks SET isActive = 0, updatedAtEpochMs = :nowMs WHERE id = :id")
    suspend fun softDelete(id: Long, nowMs: Long)
}
