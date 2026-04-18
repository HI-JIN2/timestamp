package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap
import com.yujin.timestamp.core.model.TimestampAspectRatio

object TimestampEditorUiContract {
    data class State(
        val selectedImageBase64: String? = null,
        val previewImage: ImageBitmap? = null,
        val isCropEditorVisible: Boolean = false,
        val hasSelectedPhoto: Boolean = false,
        val defaultTimestamp: String = "04.01.26  03:42",
        val timestamp: String = "04.01.26  03:42",
        val location: String = "SEOUL, KR",
        val exportMessage: String? = null,
        val overlayTone: TimestampOverlayTone = TimestampOverlayTone.ClassicAmber,
        val overlayAlignment: TimestampOverlayAlignment = TimestampOverlayAlignment.BottomEnd,
        val overlayScale: TimestampOverlayScale = TimestampOverlayScale.Small,
        val overlayInset: TimestampOverlayInset = TimestampOverlayInset.Tight,
        val overlaySafeArea: TimestampOverlaySafeArea = TimestampOverlaySafeArea.Off,
        val overlayOffsetXStep: Int = 1,
        val overlayOffsetYStep: Int = -1,
        val aspectRatioPreset: TimestampAspectRatio = TimestampAspectRatio.ThreeFour,
        val cropLeftRatio: Float = 0f,
        val cropTopRatio: Float = 0f,
        val cropWidthRatio: Float = 1f,
        val cropHeightRatio: Float = 1f,
    ) {
        val isExportEnabled: Boolean
            get() = hasSelectedPhoto && timestamp.isNotBlank()
    }

    sealed interface Intent {
        data class SyncExternal(
            val selectedImageBase64: String?,
            val previewImage: ImageBitmap?,
            val metadataTimestampLabel: String?,
            val selectedTimestampLabel: String?,
            val exportMessage: String?,
        ) : Intent

        data object ResetTimestamp : Intent
        data class ToneChanged(val value: TimestampOverlayTone) : Intent
        data object OpenCropEditor : Intent
        data object CloseCropEditor : Intent
        data class AspectRatioChanged(val value: TimestampAspectRatio) : Intent
        data class CropRectChanged(
            val leftRatio: Float,
            val topRatio: Float,
            val widthRatio: Float,
            val heightRatio: Float,
        ) : Intent
        data object ResetCrop : Intent
    }
}
