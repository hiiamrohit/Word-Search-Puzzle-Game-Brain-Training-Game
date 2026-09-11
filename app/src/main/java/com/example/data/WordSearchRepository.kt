package com.example.data

import kotlinx.coroutines.flow.Flow

class WordSearchRepository(private val dao: LevelProgressDao) {

    val allProgress: Flow<List<LevelProgressEntity>> = dao.getAllProgress()

    suspend fun initializeLevelsIfEmpty(totalLevels: Int = 10) {
        val initialList = (1..totalLevels).map { level ->
            LevelProgressEntity(
                levelId = level,
                isUnlocked = (level == 1),
                isCompleted = false,
                bestTimeSeconds = 0,
                hintsRemaining = 3
            )
        }
        dao.insertAllInitial(initialList)
    }

    suspend fun markLevelCompleted(levelId: Int, timeSeconds: Int, totalLevels: Int = 10) {
        val current = dao.getProgressByLevelDirect(levelId)
        val currentBest = current?.bestTimeSeconds ?: 0
        val newBest = if (currentBest == 0 || timeSeconds < currentBest) timeSeconds else currentBest
        val hints = (current?.hintsRemaining ?: 3) + 1 // award +1 hint on completion!

        // Save completion
        dao.insertOrUpdate(
            LevelProgressEntity(
                levelId = levelId,
                isUnlocked = true,
                isCompleted = true,
                bestTimeSeconds = newBest,
                hintsRemaining = hints
            )
        )

        // Unlock next level if available
        if (levelId < totalLevels) {
            val nextLevel = dao.getProgressByLevelDirect(levelId + 1)
            dao.insertOrUpdate(
                LevelProgressEntity(
                    levelId = levelId + 1,
                    isUnlocked = true,
                    isCompleted = nextLevel?.isCompleted ?: false,
                    bestTimeSeconds = nextLevel?.bestTimeSeconds ?: 0,
                    hintsRemaining = hints
                )
            )
        }

        // Sync hints across levels
        dao.updateAllHints(hints)
    }

    suspend fun consumeHint(currentHints: Int) {
        val updated = (currentHints - 1).coerceAtLeast(0)
        dao.updateAllHints(updated)
    }

    suspend fun addHint(count: Int = 1, currentHints: Int) {
        dao.updateAllHints(currentHints + count)
    }
}
