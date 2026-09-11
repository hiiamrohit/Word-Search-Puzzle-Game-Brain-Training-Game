package com.example.model

import androidx.compose.ui.graphics.Color

data class CellCoordinate(
    val row: Int,
    val col: Int
)

data class WordPlacement(
    val word: String,
    val start: CellCoordinate,
    val end: CellCoordinate,
    val cells: List<CellCoordinate>
)

data class FoundWord(
    val word: String,
    val cells: List<CellCoordinate>,
    val color: Color
)

data class WordSearchLevel(
    val levelNumber: Int,
    val theme: String,
    val iconName: String,
    val words: List<String>,
    val gridSize: Int,
    val allowDiagonal: Boolean = false,
    val allowReverse: Boolean = false
)

val HighlightColors = listOf(
    Color(0xFFEF4444), // Coral Red
    Color(0xFF3B82F6), // Ocean Blue
    Color(0xFF10B981), // Emerald Green
    Color(0xFFF59E0B), // Amber Gold
    Color(0xFF8B5CF6), // Royal Purple
    Color(0xFFEC4899), // Neon Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF14B8A6), // Teal
    Color(0xFFF97316), // Bright Orange
    Color(0xFF6366F1)  // Indigo
)
