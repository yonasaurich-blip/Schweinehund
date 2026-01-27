package com.example.nfcdailycheckin.data

import com.example.nfcdailycheckin.util.DateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class Repository(
    private val taskDao: TaskDao,
    private val completionDao: CompletionDao,
) {
    fun observeDashboard(from: LocalDate, to: LocalDate): Flow<DashboardData> {
        val fromIso = from.toString()
        val toIso = to.toString()
        return taskDao.observeActiveTasks().combine(
            completionDao.observeCompletionsInRange(fromIso, toIso)
        ) { tasks, completions ->
            DashboardData(tasks, completions)
        }
    }

    suspend fun markDone(taskId: Long, date: LocalDate): Boolean {
        val dateIso = date.toString()
        val already = completionDao.get(taskId, dateIso)
        if (already != null) return false
        completionDao.insertIgnore(
            CompletionEntity(
                taskId = taskId,
                dateIso = dateIso,
                completedAtEpochMs = DateTime.nowInstant().toEpochMilli(),
            )
        )
        return true
    }

    suspend fun resetToday(taskId: Long, date: LocalDate) {
        completionDao.deleteForDay(taskId, date.toString())
    }

    suspend fun upsertTask(task: TaskEntity): Long = taskDao.upsert(task)

    suspend fun softDeleteTask(id: Long) {
        taskDao.softDelete(id, DateTime.nowInstant().toEpochMilli())
    }
}

data class DashboardData(
    val tasks: List<TaskEntity>,
    val completions: List<CompletionEntity>
)
