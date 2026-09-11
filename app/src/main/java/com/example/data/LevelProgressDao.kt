package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun getProgressByLevel(levelId: Int): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    suspend fun getProgressByLevelDirect(levelId: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllInitial(list: List<LevelProgressEntity>)

    @Update
    suspend fun update(progress: LevelProgressEntity)

    @Query("UPDATE level_progress SET hintsRemaining = :hints")
    suspend fun updateAllHints(hints: Int)
}
