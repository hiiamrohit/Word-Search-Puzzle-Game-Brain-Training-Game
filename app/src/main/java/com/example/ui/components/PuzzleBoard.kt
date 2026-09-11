package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.GeneratedPuzzle
import com.example.model.CellCoordinate
import com.example.model.FoundWord

@Composable
fun PuzzleBoard(
    puzzle: GeneratedPuzzle,
    foundWords: List<FoundWord>,
    selectedCells: List<CellCoordinate>,
    hintedCells: List<CellCoordinate>,
    onDragStart: (CellCoordinate) -> Unit,
    onDragMove: (CellCoordinate) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val size = puzzle.gridSize

    // Pulsing alpha for hint highlight
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(10.dp)
            .testTag("puzzle_board")
    ) {
        val boardWidth = maxWidth
        val cellSize = constraints.maxWidth.toFloat() / size

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val col = (offset.x / cellSize).toInt().coerceIn(0, size - 1)
                            val row = (offset.y / cellSize).toInt().coerceIn(0, size - 1)
                            onDragStart(CellCoordinate(row, col))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val col = (change.position.x / cellSize).toInt().coerceIn(0, size - 1)
                            val row = (change.position.y / cellSize).toInt().coerceIn(0, size - 1)
                            onDragMove(CellCoordinate(row, col))
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
        ) {
            // 1. Highlight Overlay Canvas (Renders under letters)
            val primaryColor = MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw found words rounded capsules
                foundWords.forEach { found ->
                    if (found.cells.isNotEmpty()) {
                        val first = found.cells.first()
                        val last = found.cells.last()
                        val startOffset = Offset(
                            x = first.col * cellSize + cellSize / 2f,
                            y = first.row * cellSize + cellSize / 2f
                        )
                        val endOffset = Offset(
                            x = last.col * cellSize + cellSize / 2f,
                            y = last.row * cellSize + cellSize / 2f
                        )

                        // Broad colorful translucent rounded stroke
                        drawLine(
                            color = found.color.copy(alpha = 0.38f),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = cellSize * 0.78f,
                            cap = StrokeCap.Round
                        )

                        // Inner vibrant accent spine
                        drawLine(
                            color = found.color.copy(alpha = 0.85f),
                            start = startOffset,
                            end = endOffset,
                            strokeWidth = cellSize * 0.14f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw active dragging selection capsule
                if (selectedCells.isNotEmpty()) {
                    val first = selectedCells.first()
                    val last = selectedCells.last()
                    val startOffset = Offset(
                        x = first.col * cellSize + cellSize / 2f,
                        y = first.row * cellSize + cellSize / 2f
                    )
                    val endOffset = Offset(
                        x = last.col * cellSize + cellSize / 2f,
                        y = last.row * cellSize + cellSize / 2f
                    )

                    drawLine(
                        color = primaryColor.copy(alpha = 0.32f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = cellSize * 0.76f,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = primaryColor.copy(alpha = 0.75f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = cellSize * 0.12f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw Hint highlight if active
                if (hintedCells.isNotEmpty()) {
                    val first = hintedCells.first()
                    val last = hintedCells.last()
                    val startOffset = Offset(
                        x = first.col * cellSize + cellSize / 2f,
                        y = first.row * cellSize + cellSize / 2f
                    )
                    val endOffset = Offset(
                        x = last.col * cellSize + cellSize / 2f,
                        y = last.row * cellSize + cellSize / 2f
                    )

                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = hintPulseAlpha * 0.55f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = cellSize * 0.85f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = hintPulseAlpha),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = cellSize * 0.16f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Letters Grid
            // Responsive font size based on grid size
            val letterFontSize = when {
                size <= 6 -> 24.sp
                size <= 7 -> 21.sp
                size <= 8 -> 18.sp
                else -> 16.sp
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                for (r in 0 until size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (c in 0 until size) {
                            val char = puzzle.grid[r][c]
                            val isDragSelected = selectedCells.any { it.row == r && it.col == c }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    fontSize = letterFontSize,
                                    fontWeight = if (isDragSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (isDragSelected) {
                                        primaryColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
