package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LevelDefinitions
import com.example.ui.GameUiState
import com.example.ui.WordSearchViewModel
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.PuzzleBoard
import com.example.ui.components.WordListChips

@Composable
fun GameScreen(
    viewModel: WordSearchViewModel,
    state: GameUiState
) {
    val scrollState = rememberScrollState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val level = state.currentLevel
    val puzzle = state.puzzle
    val foundCount = state.foundWords.size
    val totalCount = level.words.size
    val progress = if (totalCount > 0) foundCount.toFloat() / totalCount else 0f

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding, bottom = navBarPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Top Bar: Home, Level & Category, Timer, Hint, Restart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home / Back Button
                FilledTonalIconButton(
                    onClick = { viewModel.navigateToLevelSelect() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Home"
                    )
                }

                // Level title and Theme name
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Level ${level.levelNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = level.theme,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Action Controls: Timer, Hint, Restart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Restart Button
                    FilledTonalIconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("restart_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Hint Button with remaining count
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = if (state.hintsRemaining > 0) Color(0xFFF59E0B) else Color.Gray,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${state.hintsRemaining}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        FilledTonalIconButton(
                            onClick = { viewModel.useHint() },
                            enabled = state.hintsRemaining > 0 && !state.isLevelComplete,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("hint_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (state.hintsRemaining > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (state.hintsRemaining > 0) Color(0xFFD97706) else Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Progress and Timer Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Words Found Progress
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Words Found",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$foundCount / $totalCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Timer Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Word List Chips (Above Puzzle)
            WordListChips(
                words = level.words,
                foundWords = state.foundWords,
                hintedWord = state.hintedWord,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Live Drag Selection Word Preview Pill
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(max = 600.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePreviewText.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -10 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -10 })
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = state.activePreviewText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. Large Rounded Puzzle Board
            if (puzzle != null) {
                PuzzleBoard(
                    puzzle = puzzle,
                    foundWords = state.foundWords,
                    selectedCells = state.selectedCells,
                    hintedCells = state.hintedCells,
                    onDragStart = { viewModel.onDragStart(it) },
                    onDragMove = { viewModel.onDragMove(it) },
                    onDragEnd = { viewModel.onDragEnd() },
                    modifier = Modifier.widthIn(max = 500.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Level Complete Dialog
        if (state.isLevelComplete) {
            LevelCompleteDialog(
                levelNumber = level.levelNumber,
                theme = level.theme,
                totalWords = totalCount,
                timeSeconds = state.elapsedSeconds,
                hasNextLevel = level.levelNumber < LevelDefinitions.levels.size,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartLevel() },
                onHome = { viewModel.navigateToLevelSelect() }
            )
        }
    }
}
