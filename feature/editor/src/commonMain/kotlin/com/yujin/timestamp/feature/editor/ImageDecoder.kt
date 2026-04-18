package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeSelectedImage(base64: String): ImageBitmap?
