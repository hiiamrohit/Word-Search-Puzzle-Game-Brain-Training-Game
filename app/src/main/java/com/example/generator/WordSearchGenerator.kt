package com.example.generator

import com.example.model.CellCoordinate
import com.example.model.WordPlacement
import com.example.model.WordSearchLevel
import kotlin.random.Random

data class GeneratedPuzzle(
    val levelNumber: Int,
    val gridSize: Int,
    val grid: Array<CharArray>,
    val placements: Map<String, WordPlacement>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GeneratedPuzzle
        if (levelNumber != other.levelNumber) return false
        if (gridSize != other.gridSize) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (placements != other.placements) return false
        return true
    }

    override fun hashCode(): Int {
        var result = levelNumber
        result = 31 * result + gridSize
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + placements.hashCode()
        return result
    }
}

object WordSearchGenerator {

    private data class Direction(val dRow: Int, val dCol: Int)

    fun generate(level: WordSearchLevel, seed: Long? = null): GeneratedPuzzle {
        val random = if (seed != null) Random(seed) else Random.Default
        val words = level.words.sortedByDescending { it.length }
        val size = level.gridSize

        val directions = buildList {
            // Forward horizontal and vertical always allowed
            add(Direction(0, 1))  // Horizontal right
            add(Direction(1, 0))  // Vertical down

            if (level.allowDiagonal) {
                add(Direction(1, 1))   // Diagonal down-right
            }

            if (level.allowReverse) {
                add(Direction(0, -1))  // Horizontal left
                add(Direction(-1, 0))  // Vertical up
                if (level.allowDiagonal) {
                    add(Direction(-1, 1))  // Diagonal up-right
                    add(Direction(-1, -1)) // Diagonal up-left
                    add(Direction(1, -1))  // Diagonal down-left
                }
            }
        }

        // Try to place all words
        var attempts = 0
        while (attempts < 200) {
            attempts++
            val grid = Array(size) { CharArray(size) { '\u0000' } }
            val placements = mutableMapOf<String, WordPlacement>()

            var allPlaced = true
            for (word in words) {
                val placement = tryPlaceWord(word, grid, size, directions, random)
                if (placement != null) {
                    placements[word] = placement
                    // Apply to grid
                    placement.cells.forEachIndexed { index, cell ->
                        grid[cell.row][cell.col] = word[index]
                    }
                } else {
                    allPlaced = false
                    break
                }
            }

            if (allPlaced && placements.size == words.size) {
                // Fill remaining empty cells with random letters
                // Use slightly weighted frequency for realistic letter look
                val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                for (r in 0 until size) {
                    for (c in 0 until size) {
                        if (grid[r][c] == '\u0000') {
                            grid[r][c] = alphabet[random.nextInt(alphabet.length)]
                        }
                    }
                }

                // Verify every word exists
                val verified = placements.all { (word, placement) ->
                    val chars = placement.cells.map { grid[it.row][it.col] }.joinToString("")
                    chars == word
                }

                if (verified) {
                    return GeneratedPuzzle(
                        levelNumber = level.levelNumber,
                        gridSize = size,
                        grid = grid,
                        placements = placements
                    )
                }
            }
        }

        // Fallback: guaranteed placement without overlap if multiple attempts fail
        return generateDeterministicFallback(level, random)
    }

    private fun tryPlaceWord(
        word: String,
        grid: Array<CharArray>,
        size: Int,
        directions: List<Direction>,
        random: Random
    ): WordPlacement? {
        val wordLen = word.length
        val candidatePositions = mutableListOf<Triple<Int, Int, Direction>>()

        // Gather all valid start positions and directions that can physically fit
        for (r in 0 until size) {
            for (c in 0 until size) {
                for (dir in directions) {
                    val endRow = r + dir.dRow * (wordLen - 1)
                    val endCol = c + dir.dCol * (wordLen - 1)
                    if (endRow in 0 until size && endCol in 0 until size) {
                        candidatePositions.add(Triple(r, c, dir))
                    }
                }
            }
        }

        candidatePositions.shuffle(random)

        for ((startRow, startCol, dir) in candidatePositions) {
            var canPlace = true
            val cells = mutableListOf<CellCoordinate>()

            for (i in 0 until wordLen) {
                val r = startRow + dir.dRow * i
                val c = startCol + dir.dCol * i
                val currentLetter = grid[r][c]
                if (currentLetter != '\u0000' && currentLetter != word[i]) {
                    canPlace = false
                    break
                }
                cells.add(CellCoordinate(r, c))
            }

            if (canPlace) {
                return WordPlacement(
                    word = word,
                    start = cells.first(),
                    end = cells.last(),
                    cells = cells
                )
            }
        }

        return null
    }

    private fun generateDeterministicFallback(
        level: WordSearchLevel,
        random: Random
    ): GeneratedPuzzle {
        val size = level.gridSize.coerceAtLeast(8)
        val grid = Array(size) { CharArray(size) { '\u0000' } }
        val placements = mutableMapOf<String, WordPlacement>()
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        // Place row by row
        level.words.forEachIndexed { index, word ->
            val row = (index * (size / level.words.size)).coerceAtMost(size - 1)
            val maxColStart = (size - word.length).coerceAtLeast(0)
            val colStart = if (maxColStart > 0) random.nextInt(maxColStart) else 0
            val cells = (0 until word.length).map { i ->
                val c = colStart + i
                grid[row][c] = word[i]
                CellCoordinate(row, c)
            }
            placements[word] = WordPlacement(
                word = word,
                start = cells.first(),
                end = cells.last(),
                cells = cells
            )
        }

        for (r in 0 until size) {
            for (c in 0 until size) {
                if (grid[r][c] == '\u0000') {
                    grid[r][c] = alphabet[random.nextInt(alphabet.length)]
                }
            }
        }

        return GeneratedPuzzle(
            levelNumber = level.levelNumber,
            gridSize = size,
            grid = grid,
            placements = placements
        )
    }
}
