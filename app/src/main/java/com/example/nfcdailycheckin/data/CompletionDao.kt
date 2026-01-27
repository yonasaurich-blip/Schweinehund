package com.example.nfcdailycheckin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE dateIso BETWEEN :fromIso AND :toIso")
    fun observeCompletionsInRange(fromIso: String, toIso: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE taskId = :taskId AND dateIso = :dateIso")
    suspend fun get(taskId: Long, dateIso: String): CompletionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: CompletionEntity): Long

    @Query("DELETE FROM completions WHERE taskId = :taskId AND dateIso = :dateIso")
    suspend fun deleteForDay(taskId: Long, dateIso: String)

    @Query("DELETE FROM completions WHERE dateIso = :dateIso")
    suspend fun deleteAllForDay(dateIso: String)
}
