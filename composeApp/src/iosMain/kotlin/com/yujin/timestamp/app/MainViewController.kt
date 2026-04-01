package com.yujin.timestamp.app

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
): UIViewController = ComposeUIViewController {
    TimestampApp(
        selectedImageBase64 = selectedImageBase64,
        metadataTimestampLabel = metadataTimestampLabel,
        exportMessage = exportMessage,
        onPickPhoto = onPickPhoto,
        onExport = onExport,
    )
}

internal actual fun decodeSelectedImage(base64: String): ImageBitmap? = null
