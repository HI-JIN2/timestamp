package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap

internal data class TimestampCropRect(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
)

internal fun defaultCropRect(
    previewImage: ImageBitmap?,
    aspectRatio: Float,
): TimestampCropRect {
    if (previewImage == null) {
        return TimestampCropRect(0f, 0f, 1f, 1f)
    }

    val imageWidth = previewImage.width.toFloat().coerceAtLeast(1f)
    val imageHeight = previewImage.height.toFloat().coerceAtLeast(1f)
    val imageAspectRatio = imageWidth / imageHeight

    val cropWidth: Float
    val cropHeight: Float
    if (imageAspectRatio > aspectRatio) {
        cropHeight = imageHeight
        cropWidth = cropHeight * aspectRatio
    } else {
        cropWidth = imageWidth
        cropHeight = cropWidth / aspectRatio
    }

    val left = (imageWidth - cropWidth) / 2f
    val top = (imageHeight - cropHeight) / 2f

    return TimestampCropRect(
        leftRatio = left / imageWidth,
        topRatio = top / imageHeight,
        widthRatio = cropWidth / imageWidth,
        heightRatio = cropHeight / imageHeight,
    )
}
