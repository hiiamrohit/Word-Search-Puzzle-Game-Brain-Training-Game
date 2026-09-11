package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val bestTimeSeconds: Int = 0,
    val hintsRemaining: Int = 3
)
