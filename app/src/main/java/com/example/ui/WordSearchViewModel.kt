package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LevelProgressEntity
import com.example.data.WordSearchRepository
import com.example.generator.GeneratedPuzzle
import com.example.generator.WordSearchGenerator
import com.example.model.CellCoordinate
import com.example.model.FoundWord
import com.example.model.HighlightColors
import com.example.model.LevelDefinitions
import com.example.model.WordSearchLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class AppScreen {
    LEVEL_SELECT,
    GAME
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.LEVEL_SELECT,
    val currentLevelNumber: Int = 1,
    val currentLevel: WordSearchLevel = LevelDefinitions.getLevel(1),
    val puzzle: GeneratedPuzzle? = null,
    val foundWords: List<FoundWord> = emptyList(),
    val selectedCells: List<CellCoordinate> = emptyList(),
    val activePreviewText: String = "",
    val hintedWord: String? = null,
    val hintedCells: List<CellCoordinate> = emptyList(),
    val isLevelComplete: Boolean = false,
    val elapsedSeconds: Int = 0,
    val hintsRemaining: Int = 3,
    val isTimerRunning: Boolean = false
)

class WordSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WordSearchRepository = WordSearchRepository(
        AppDatabase.getInstance(application).levelProgressDao()
    )

    val progressList: StateFlow<List<LevelProgressEntity>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hintJob: Job? = null
    private var dragStartCell: CellCoordinate? = null

    init {
        viewModelScope.launch {
            repository.initializeLevelsIfEmpty(LevelDefinitions.levels.size)
            // Synchronize hints remaining
            repository.allProgress.collect { list ->
                val currentHints = list.firstOrNull()?.hintsRemaining ?: 3
                _uiState.value = _uiState.value.copy(hintsRemaining = currentHints)
            }
        }
    }

    fun navigateToLevelSelect() {
        stopTimer()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.LEVEL_SELECT,
            selectedCells = emptyList(),
            activePreviewText = ""
        )
    }

    fun startLevel(levelNumber: Int) {
        val level = LevelDefinitions.getLevel(levelNumber)
        val puzzle = WordSearchGenerator.generate(level)

        stopTimer()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.GAME,
            currentLevelNumber = levelNumber,
            currentLevel = level,
            puzzle = puzzle,
            foundWords = emptyList(),
            selectedCells = emptyList(),
            activePreviewText = "",
            hintedWord = null,
            hintedCells = emptyList(),
            isLevelComplete = false,
            elapsedSeconds = 0,
            isTimerRunning = true
        )
        startTimer()
    }

    fun restartLevel() {
        val level = _uiState.value.currentLevel
        val puzzle = WordSearchGenerator.generate(level)
        stopTimer()
        _uiState.value = _uiState.value.copy(
            puzzle = puzzle,
            foundWords = emptyList(),
            selectedCells = emptyList(),
            activePreviewText = "",
            hintedWord = null,
            hintedCells = emptyList(),
            isLevelComplete = false,
            elapsedSeconds = 0,
            isTimerRunning = true
        )
        startTimer()
    }

    fun nextLevel() {
        val nextLevelNumber = _uiState.value.currentLevelNumber + 1
        if (nextLevelNumber <= LevelDefinitions.levels.size) {
            startLevel(nextLevelNumber)
        } else {
            navigateToLevelSelect()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isTimerRunning && !_uiState.value.isLevelComplete) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(isTimerRunning = false)
    }

    // Touch & Swipe gestures
    fun onDragStart(cell: CellCoordinate) {
        if (_uiState.value.isLevelComplete) return
        val puzzle = _uiState.value.puzzle ?: return
        if (cell.row !in 0 until puzzle.gridSize || cell.col !in 0 until puzzle.gridSize) return

        dragStartCell = cell
        val char = puzzle.grid[cell.row][cell.col]
        _uiState.value = _uiState.value.copy(
            selectedCells = listOf(cell),
            activePreviewText = char.toString()
        )
    }

    fun onDragMove(currentCell: CellCoordinate) {
        if (_uiState.value.isLevelComplete) return
        val start = dragStartCell ?: return
        val puzzle = _uiState.value.puzzle ?: return

        // Clamp to board bounds
        val clampedRow = currentCell.row.coerceIn(0, puzzle.gridSize - 1)
        val clampedCol = currentCell.col.coerceIn(0, puzzle.gridSize - 1)

        val dRow = clampedRow - start.row
        val dCol = clampedCol - start.col

        if (dRow == 0 && dCol == 0) {
            val char = puzzle.grid[start.row][start.col]
            _uiState.value = _uiState.value.copy(
                selectedCells = listOf(start),
                activePreviewText = char.toString()
            )
            return
        }

        // Calculate line snapping
        val absRow = abs(dRow)
        val absCol = abs(dCol)

        val lineCells = mutableListOf<CellCoordinate>()

        // 1. Horizontal
        if (absRow == 0 || (absCol >= 2 * absRow)) {
            val stepCol = if (dCol > 0) 1 else -1
            var col = start.col
            while (true) {
                lineCells.add(CellCoordinate(start.row, col))
                if (col == clampedCol) break
                col += stepCol
            }
        }
        // 2. Vertical
        else if (absCol == 0 || (absRow >= 2 * absCol)) {
            val stepRow = if (dRow > 0) 1 else -1
            var row = start.row
            while (true) {
                lineCells.add(CellCoordinate(row, start.col))
                if (row == clampedRow) break
                row += stepRow
            }
        }
        // 3. Diagonal (Snap to 45 degrees)
        else {
            val length = minOf(absRow, absCol)
            val stepRow = if (dRow > 0) 1 else -1
            val stepCol = if (dCol > 0) 1 else -1
            for (i in 0..length) {
                val r = start.row + i * stepRow
                val c = start.col + i * stepCol
                if (r in 0 until puzzle.gridSize && c in 0 until puzzle.gridSize) {
                    lineCells.add(CellCoordinate(r, c))
                }
            }
        }

        val letters = lineCells.map { puzzle.grid[it.row][it.col] }.joinToString("")
        _uiState.value = _uiState.value.copy(
            selectedCells = lineCells,
            activePreviewText = letters
        )
    }

    fun onDragEnd() {
        if (_uiState.value.isLevelComplete) return
        val currentSelected = _uiState.value.selectedCells
        val puzzle = _uiState.value.puzzle
        val level = _uiState.value.currentLevel

        if (currentSelected.isNotEmpty() && puzzle != null) {
            val forwardWord = currentSelected.map { puzzle.grid[it.row][it.col] }.joinToString("")
            val backwardWord = forwardWord.reversed()

            val alreadyFoundWords = _uiState.value.foundWords.map { it.word }

            val matchedWord = when {
                level.words.contains(forwardWord) && !alreadyFoundWords.contains(forwardWord) -> forwardWord
                level.words.contains(backwardWord) && !alreadyFoundWords.contains(backwardWord) -> backwardWord
                else -> null
            }

            if (matchedWord != null) {
                val colorIndex = _uiState.value.foundWords.size % HighlightColors.size
                val assignedColor = HighlightColors[colorIndex]
                val newFound = FoundWord(
                    word = matchedWord,
                    cells = currentSelected,
                    color = assignedColor
                )
                val updatedFoundList = _uiState.value.foundWords + newFound
                val isNowComplete = updatedFoundList.size >= level.words.size

                _uiState.value = _uiState.value.copy(
                    foundWords = updatedFoundList,
                    selectedCells = emptyList(),
                    activePreviewText = "",
                    isLevelComplete = isNowComplete
                )

                if (isNowComplete) {
                    stopTimer()
                    viewModelScope.launch {
                        repository.markLevelCompleted(
                            levelId = level.levelNumber,
                            timeSeconds = _uiState.value.elapsedSeconds,
                            totalLevels = LevelDefinitions.levels.size
                        )
                    }
                }
            } else {
                // Incorrect selection - reset selection cleanly
                _uiState.value = _uiState.value.copy(
                    selectedCells = emptyList(),
                    activePreviewText = ""
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                selectedCells = emptyList(),
                activePreviewText = ""
            )
        }

        dragStartCell = null
    }

    fun useHint() {
        val state = _uiState.value
        if (state.hintsRemaining <= 0 || state.isLevelComplete) return
        val puzzle = state.puzzle ?: return
        val level = state.currentLevel

        val unfound = level.words.firstOrNull { word ->
            state.foundWords.none { it.word == word }
        } ?: return

        val placement = puzzle.placements[unfound] ?: return

        viewModelScope.launch {
            repository.consumeHint(state.hintsRemaining)
        }

        hintJob?.cancel()
        hintJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                hintedWord = unfound,
                hintedCells = placement.cells
            )
            delay(3000)
            _uiState.value = _uiState.value.copy(
                hintedWord = null,
                hintedCells = emptyList()
            )
        }
    }
}
