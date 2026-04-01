package com.yujin.timestamp.app

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
    val cropZoomKey: String,
    val cropOffsetXStep: Int,
    val cropOffsetYStep: Int,
)
