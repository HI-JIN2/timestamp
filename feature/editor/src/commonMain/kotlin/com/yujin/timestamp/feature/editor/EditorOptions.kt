package com.yujin.timestamp.feature.editor

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import timestamp.feature.editor.generated.resources.*

enum class OverlayTone(
    val labelRes: StringResource,
    val timestampColor: Color,
    val locationColor: Color,
    val shadowColor: Color,
    val timestampColorHex: String,
    val locationColorHex: String,
    val shadowColorHex: String,
) {
    ClassicAmber(
        labelRes = Res.string.tone_classic,
        timestampColor = Color(0xFFFFA347),
        locationColor = Color(0xFFF7E7C6),
        shadowColor = Color(0xA6000000),
        timestampColorHex = "#FFA347",
        locationColorHex = "#F7E7C6",
        shadowColorHex = "#A6000000",
    ),
    BurntOrange(
        labelRes = Res.string.tone_burnt,
        timestampColor = Color(0xFFFF7A2F),
        locationColor = Color(0xFFFFD4B0),
        shadowColor = Color(0x99000000),
        timestampColorHex = "#FF7A2F",
        locationColorHex = "#FFD4B0",
        shadowColorHex = "#99000000",
    ),
    FilmRed(
        labelRes = Res.string.tone_film,
        timestampColor = Color(0xFFFF6B5E),
        locationColor = Color(0xFFFFD7D1),
        shadowColor = Color(0x99000000),
        timestampColorHex = "#FF6B5E",
        locationColorHex = "#FFD7D1",
        shadowColorHex = "#99000000",
    ),
}

enum class OverlayAlignment(
    val labelRes: StringResource,
    val containerAlignment: Alignment,
    val exportKey: String,
) {
    BottomStart(Res.string.overlay_alignment_bottom_start, Alignment.BottomStart, "bottom_start"),
    BottomEnd(Res.string.overlay_alignment_bottom_end, Alignment.BottomEnd, "bottom_end"),
}

enum class OverlayScale(
    val labelRes: StringResource,
    val timestampFontSp: Int,
    val locationFontSp: Int,
    val exportKey: String,
) {
    Small(Res.string.overlay_scale_small, 11, 6, "small"),
    Medium(Res.string.overlay_scale_medium, 28, 14, "medium"),
    Large(Res.string.overlay_scale_large, 32, 16, "large"),
}

enum class OverlayInset(
    val labelRes: StringResource,
    val previewPaddingDp: Int,
    val exportKey: String,
) {
    Tight(Res.string.overlay_inset_tight, 14, "tight"),
    Balanced(Res.string.overlay_inset_balanced, 18, "balanced"),
    Spacious(Res.string.overlay_inset_spacious, 24, "spacious"),
}

enum class OverlaySafeArea(
    val labelRes: StringResource,
    val extraPreviewBottomDp: Int,
    val exportExtraBottomRatio: Float,
    val exportKey: String,
) {
    Off(Res.string.overlay_safe_area_off, 0, 0f, "off"),
    Standard(Res.string.overlay_safe_area_standard, 10, 0.025f, "standard"),
    Strong(Res.string.overlay_safe_area_strong, 20, 0.05f, "strong"),
}
