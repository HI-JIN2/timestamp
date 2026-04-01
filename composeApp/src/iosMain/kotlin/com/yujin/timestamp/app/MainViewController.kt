package com.yujin.timestamp.app

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    onPickPhoto: () -> Unit = {},
): UIViewController = ComposeUIViewController {
    TimestampApp(
        selectedImageBase64 = selectedImageBase64,
        metadataTimestampLabel = metadataTimestampLabel,
        onPickPhoto = onPickPhoto,
    )
}

internal actual fun decodeSelectedImage(base64: String): ImageBitmap? = null
