package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.WordSearchViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.WordSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordSearchTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WordSearchApp()
                }
            }
        }
    }
}

@Composable
fun WordSearchApp(viewModel: WordSearchViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val progressList by viewModel.progressList.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        AppScreen.LEVEL_SELECT -> {
            LevelSelectScreen(
                progressList = progressList,
                hintsRemaining = uiState.hintsRemaining,
                onSelectLevel = { levelNumber ->
                    viewModel.startLevel(levelNumber)
                }
            )
        }
        AppScreen.GAME -> {
            BackHandler {
                viewModel.navigateToLevelSelect()
            }
            GameScreen(
                viewModel = viewModel,
                state = uiState
            )
        }
    }
}
