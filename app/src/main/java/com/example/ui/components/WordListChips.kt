package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FoundWord

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordListChips(
    words: List<String>,
    foundWords: List<FoundWord>,
    hintedWord: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("word_list_container"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEach { word ->
                val found = foundWords.firstOrNull { it.word.equals(word, ignoreCase = true) }
                val isFound = found != null
                val isHinted = hintedWord.equals(word, ignoreCase = true)

                val targetBg = when {
                    isFound -> found.color.copy(alpha = 0.22f)
                    isHinted -> Color(0xFFFEF3C7) // soft amber
                    else -> MaterialTheme.colorScheme.surface
                }
                val bgColor by animateColorAsState(targetValue = targetBg, animationSpec = tween(300), label = "chip_bg")

                val targetBorder = when {
                    isFound -> found.color.copy(alpha = 0.85f)
                    isHinted -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                }
                val borderColor by animateColorAsState(targetValue = targetBorder, animationSpec = tween(300), label = "chip_border")

                Row(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("word_chip_$word"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isFound) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Found",
                            tint = found.color,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isHinted) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Hinted",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = word,
                        fontSize = 14.sp,
                        fontWeight = if (isFound || isHinted) FontWeight.Bold else FontWeight.SemiBold,
                        color = when {
                            isFound -> found.color
                            isHinted -> Color(0xFF92400E)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }
    }
}
