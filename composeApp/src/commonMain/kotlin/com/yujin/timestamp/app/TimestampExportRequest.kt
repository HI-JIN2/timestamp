package com.yujin.timestamp.app

data class TimestampExportRequest(
    val imageBase64: String,
    val timestamp: String,
    val location: String,
    val timestampColorHex: String,
    val locationColorHex: String,
    val shadowColorHex: String,
    val alignmentKey: String,
)
