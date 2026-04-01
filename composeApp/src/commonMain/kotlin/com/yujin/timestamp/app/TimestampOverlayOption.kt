package com.yujin.timestamp.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

enum class TimestampOverlayTone(
    val label: String,
    val timestampColor: Color,
    val locationColor: Color,
    val shadowColor: Color,
    val timestampColorHex: String,
    val locationColorHex: String,
    val shadowColorHex: String,
) {
    ClassicAmber(
        label = "클래식",
        timestampColor = Color(0xFFFFA347),
        locationColor = Color(0xFFF7E7C6),
        shadowColor = Color(0xA6000000),
        timestampColorHex = "#FFA347",
        locationColorHex = "#F7E7C6",
        shadowColorHex = "#A6000000",
    ),
    BurntOrange(
        label = "번트",
        timestampColor = Color(0xFFFF7A2F),
        locationColor = Color(0xFFFFD4B0),
        shadowColor = Color(0x99000000),
        timestampColorHex = "#FF7A2F",
        locationColorHex = "#FFD4B0",
        shadowColorHex = "#99000000",
    ),
    FilmRed(
        label = "필름",
        timestampColor = Color(0xFFFF6B5E),
        locationColor = Color(0xFFFFD7D1),
        shadowColor = Color(0x99000000),
        timestampColorHex = "#FF6B5E",
        locationColorHex = "#FFD7D1",
        shadowColorHex = "#99000000",
    ),
}

enum class TimestampOverlayAlignment(
    val label: String,
    val containerAlignment: Alignment,
    val exportKey: String,
) {
    BottomStart(
        label = "좌하단",
        containerAlignment = Alignment.BottomStart,
        exportKey = "bottom_start",
    ),
    BottomEnd(
        label = "우하단",
        containerAlignment = Alignment.BottomEnd,
        exportKey = "bottom_end",
    ),
}
