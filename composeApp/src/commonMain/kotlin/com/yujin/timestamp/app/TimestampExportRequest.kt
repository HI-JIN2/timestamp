package com.yujin.timestamp.app

data class TimestampExportRequest(
    val imageBase64: String,
    val timestamp: String,
    val location: String,
    val tone: TimestampOverlayTone,
    val alignment: TimestampOverlayAlignment,
)
