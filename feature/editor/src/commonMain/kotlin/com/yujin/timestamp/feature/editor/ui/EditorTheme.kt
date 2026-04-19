package com.yujin.timestamp.feature.editor.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle

@Composable
internal fun retroActionButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

internal fun overlayTextStyle(
    shadowColor: Color,
    blurRadius: Float,
    yOffset: Float,
): TextStyle = TextStyle(
    shadow = Shadow(
        color = shadowColor,
        offset = Offset(0f, yOffset),
        blurRadius = blurRadius,
    ),
)

internal data class EditorPalette(
    val cropBackground: Color,
    val cropGuide: Color,
    val cropFrame: Color,
    val cropGrid: Color,
    val cropShade: Color,
    val previewGradient: List<Color>,
    val previewBorder: Color,
    val placeholderText: Color,
    val divider: Color,
)

@Composable
internal fun rememberEditorPalette(isDarkTheme: Boolean): EditorPalette {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            EditorPalette(
                cropBackground = Color(0xFF050505),
                cropGuide = Color.White.copy(alpha = 0.22f),
                cropFrame = Color.White.copy(alpha = 0.9f),
                cropGrid = Color.White.copy(alpha = 0.45f),
                cropShade = Color.Black.copy(alpha = 0.42f),
                previewGradient = listOf(Color(0xFF2A2A2A), Color(0xFF171717), Color(0xFF080808)),
                previewBorder = Color.White.copy(alpha = 0.12f),
                placeholderText = Color(0xFFD8D8D8),
                divider = Color.White.copy(alpha = 0.08f),
            )
        } else {
            EditorPalette(
                cropBackground = Color(0xFFF2F2F2),
                cropGuide = Color.Black.copy(alpha = 0.18f),
                cropFrame = Color.Black.copy(alpha = 0.92f),
                cropGrid = Color.Black.copy(alpha = 0.26f),
                cropShade = Color.Black.copy(alpha = 0.18f),
                previewGradient = listOf(Color(0xFFF6F6F6), Color(0xFFD9D9D9), Color(0xFFB8B8B8)),
                previewBorder = Color.Black.copy(alpha = 0.12f),
                placeholderText = Color(0xFF4A4A4A),
                divider = Color.Black.copy(alpha = 0.08f),
            )
        }
    }
}

internal fun editorColorScheme(isDarkTheme: Boolean) = if (isDarkTheme) {
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
