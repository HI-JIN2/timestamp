package com.yujin.timestamp.core.model

data class TimestampExportRequest(
    val imageBase64: String,
    val timestamp: String,
    val location: String,
    val timestampColorHex: String,
    val locationColorHex: String,
    val shadowColorHex: String,
    val alignmentKey: String,
    val scaleKey: String,
    val insetKey: String,
    val safeAreaKey: String,
    val offsetXStep: Int,
    val offsetYStep: Int,
    val aspectRatioKey: String,
    val cropLeftRatio: Float,
    val cropTopRatio: Float,
    val cropWidthRatio: Float,
    val cropHeightRatio: Float,
)
