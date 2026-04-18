package com.yujin.timestamp.feature.crop

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
internal fun cropActionButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

internal data class TimestampCropPalette(
    val background: Color,
    val guide: Color,
    val frame: Color,
    val grid: Color,
    val shade: Color,
)

@Composable
internal fun rememberTimestampCropPalette(isDarkTheme: Boolean): TimestampCropPalette {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            TimestampCropPalette(
                background = Color(0xFF050505),
                guide = Color.White.copy(alpha = 0.22f),
                frame = Color.White.copy(alpha = 0.9f),
                grid = Color.White.copy(alpha = 0.45f),
                shade = Color.Black.copy(alpha = 0.42f),
            )
        } else {
            TimestampCropPalette(
                background = Color(0xFFF2F2F2),
                guide = Color.Black.copy(alpha = 0.18f),
                frame = Color.Black.copy(alpha = 0.92f),
                grid = Color.Black.copy(alpha = 0.26f),
                shade = Color.Black.copy(alpha = 0.18f),
            )
        }
    }
}
