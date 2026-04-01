package com.yujin.timestamp.app

import androidx.compose.ui.graphics.ImageBitmap

internal expect fun decodeSelectedImage(base64: String): ImageBitmap?
