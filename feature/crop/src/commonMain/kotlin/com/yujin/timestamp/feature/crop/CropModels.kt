package com.yujin.timestamp.feature.crop

import androidx.compose.ui.graphics.ImageBitmap
import com.yujin.timestamp.core.model.TimestampAspectRatio

data class CropRect(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
)

fun defaultCropRect(
    previewImage: ImageBitmap?,
    aspectRatio: TimestampAspectRatio,
): CropRect {
    if (previewImage == null) {
        return CropRect(0f, 0f, 1f, 1f)
    }

    val imageWidth = previewImage.width.toFloat().coerceAtLeast(1f)
    val imageHeight = previewImage.height.toFloat().coerceAtLeast(1f)
    val imageAspectRatio = imageWidth / imageHeight

    val cropWidth: Float
    val cropHeight: Float
    if (imageAspectRatio > aspectRatio.ratio) {
        cropHeight = imageHeight
        cropWidth = cropHeight * aspectRatio.ratio
    } else {
        cropWidth = imageWidth
        cropHeight = cropWidth / aspectRatio.ratio
    }

    val left = (imageWidth - cropWidth) / 2f
    val top = (imageHeight - cropHeight) / 2f

    return CropRect(
        leftRatio = left / imageWidth,
        topRatio = top / imageHeight,
        widthRatio = cropWidth / imageWidth,
        heightRatio = cropHeight / imageHeight,
    )
}
