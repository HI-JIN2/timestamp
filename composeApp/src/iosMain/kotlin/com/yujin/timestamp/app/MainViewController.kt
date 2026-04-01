package com.yujin.timestamp.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    onPickPhoto: () -> Unit = {},
): UIViewController = ComposeUIViewController {
    TimestampApp(
        selectedImageBase64 = selectedImageBase64,
        onPickPhoto = onPickPhoto,
    )
}
