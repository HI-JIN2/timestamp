package com.yujin.timestamp.feature.crop

import androidx.compose.ui.graphics.ImageBitmap
import com.yujin.timestamp.core.model.TimestampAspectRatio

object TimestampCropUiContract {
    data class State(
        val previewImage: ImageBitmap,
        val aspectRatio: TimestampAspectRatio,
        val cropLeftRatio: Float,
        val cropTopRatio: Float,
        val cropWidthRatio: Float,
        val cropHeightRatio: Float,
    )

    sealed interface Action {
        data class AspectRatioChanged(val value: TimestampAspectRatio) : Action
        data class CropRectChanged(
            val leftRatio: Float,
            val topRatio: Float,
            val widthRatio: Float,
            val heightRatio: Float,
        ) : Action

        data object Reset : Action
        data object Done : Action
    }
}
