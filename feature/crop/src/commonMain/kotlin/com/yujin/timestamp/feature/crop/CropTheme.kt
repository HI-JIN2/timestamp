package com.yujin.timestamp.feature.crop

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

internal data class CropPalette(
    val background: Color,
    val guide: Color,
    val frame: Color,
    val grid: Color,
    val shade: Color,
)

@Composable
internal fun rememberCropPalette(isDarkTheme: Boolean): CropPalette {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            CropPalette(
                background = Color(0xFF050505),
                guide = Color.White.copy(alpha = 0.22f),
                frame = Color.White.copy(alpha = 0.9f),
                grid = Color.White.copy(alpha = 0.45f),
                shade = Color.Black.copy(alpha = 0.42f),
            )
        } else {
            CropPalette(
                background = Color(0xFFF2F2F2),
                guide = Color.Black.copy(alpha = 0.18f),
                frame = Color.Black.copy(alpha = 0.92f),
                grid = Color.Black.copy(alpha = 0.26f),
                shade = Color.Black.copy(alpha = 0.18f),
            )
        }
    }
}

internal fun timestampCropColorScheme(isDarkTheme: Boolean) = if (isDarkTheme) {
    darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        secondary = Color(0xFFD0D0D0),
        onSecondary = Color.Black,
        background = Color(0xFF000000),
        onBackground = Color(0xFFF5F5F5),
        surface = Color(0xFF0E0E0E),
        onSurface = Color(0xFFF5F5F5),
        surfaceVariant = Color(0xFF181818),
        onSurfaceVariant = Color(0xFFB8B8B8),
        outline = Color(0xFF3A3A3A),
    )
} else {
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = Color(0xFF3A3A3A),
        onSecondary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFF7F7F7),
        onSurface = Color(0xFF111111),
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = Color(0xFF5A5A5A),
        outline = Color(0xFFBDBDBD),
    )
}
