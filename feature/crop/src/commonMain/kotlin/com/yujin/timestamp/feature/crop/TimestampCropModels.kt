package com.yujin.timestamp.feature.crop

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.StringResource
import timestamp.feature.crop.generated.resources.Res
import timestamp.feature.crop.generated.resources.aspect_ratio_four_three
import timestamp.feature.crop.generated.resources.aspect_ratio_sixteen_nine

data class TimestampCropRect(
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
)

enum class TimestampAspectRatioPreset(
    val labelRes: StringResource,
    val ratio: Float,
    val exportKey: String,
) {
    FourThree(Res.string.aspect_ratio_four_three, 4f / 3f, "4_3"),
    SixteenNine(Res.string.aspect_ratio_sixteen_nine, 16f / 9f, "16_9"),
}

fun defaultCropRect(
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
