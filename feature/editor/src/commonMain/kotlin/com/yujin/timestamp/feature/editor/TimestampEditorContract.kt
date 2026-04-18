package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap

object TimestampEditorContract {
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
        val aspectRatioPreset: TimestampAspectRatioPreset = TimestampAspectRatioPreset.FourThree,
        val cropScale: Float = 1f,
        val cropOffsetXRatio: Float = 0f,
        val cropOffsetYRatio: Float = 0f,
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

        data class TimestampChanged(val value: String) : Intent
        data object ResetTimestamp : Intent
        data class ToneChanged(val value: TimestampOverlayTone) : Intent
        data class AlignmentChanged(val value: TimestampOverlayAlignment) : Intent
        data class ScaleChanged(val value: TimestampOverlayScale) : Intent
        data class InsetChanged(val value: TimestampOverlayInset) : Intent
        data class SafeAreaChanged(val value: TimestampOverlaySafeArea) : Intent
        data class OffsetXChanged(val value: Int) : Intent
        data class OffsetYChanged(val value: Int) : Intent
        data object OpenCropEditor : Intent
        data object CloseCropEditor : Intent
        data class AspectRatioChanged(val value: TimestampAspectRatioPreset) : Intent
        data class CropFrameDragged(
            val deltaXRatio: Float,
            val deltaYRatio: Float,
        ) : Intent
        data class CropFrameScaled(val scaleDelta: Float) : Intent
        data object ResetCrop : Intent
    }
}
