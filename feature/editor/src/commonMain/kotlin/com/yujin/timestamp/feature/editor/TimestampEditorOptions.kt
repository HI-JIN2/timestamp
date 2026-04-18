package com.yujin.timestamp.feature.editor

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
    BottomStart("좌하단", Alignment.BottomStart, "bottom_start"),
    BottomEnd("우하단", Alignment.BottomEnd, "bottom_end"),
}

enum class TimestampOverlayScale(
    val label: String,
    val timestampFontSp: Int,
    val locationFontSp: Int,
    val exportKey: String,
) {
    Small("작게", 11, 6, "small"),
    Medium("보통", 28, 14, "medium"),
    Large("크게", 32, 16, "large"),
}

enum class TimestampOverlayInset(
    val label: String,
    val previewPaddingDp: Int,
    val exportKey: String,
) {
    Tight("가깝게", 14, "tight"),
    Balanced("기본", 18, "balanced"),
    Spacious("여유", 24, "spacious"),
}

enum class TimestampOverlaySafeArea(
    val label: String,
    val extraPreviewBottomDp: Int,
    val exportExtraBottomRatio: Float,
    val exportKey: String,
) {
    Off("없음", 0, 0f, "off"),
    Standard("기본", 10, 0.025f, "standard"),
    Strong("넓게", 20, 0.05f, "strong"),
}

enum class TimestampAspectRatioPreset(
    val label: String,
    val ratio: Float,
    val exportKey: String,
) {
    FourThree("4:3", 4f / 3f, "4_3"),
    SixteenNine("16:9", 16f / 9f, "16_9"),
}
