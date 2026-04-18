package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap

object TimestampEditorContract {
    data class State(
        val selectedImageBase64: String? = null,
        val previewImage: ImageBitmap? = null,
        val hasSelectedPhoto: Boolean = false,
        val defaultTimestamp: String = "04.01.26  03:42",
        val timestamp: String = "04.01.26  03:42",
        val metadataDescription: String = "기본값: 샘플 타임스탬프",
        val location: String = "SEOUL, KR",
        val helperText: String = "사진을 고르면 실제 프리뷰를 표시하고, 메타데이터 날짜를 기본 타임스탬프로 불러옵니다.",
        val exportMessage: String? = null,
        val overlayTone: TimestampOverlayTone = TimestampOverlayTone.ClassicAmber,
        val overlayAlignment: TimestampOverlayAlignment = TimestampOverlayAlignment.BottomStart,
        val overlayScale: TimestampOverlayScale = TimestampOverlayScale.Medium,
        val overlayInset: TimestampOverlayInset = TimestampOverlayInset.Balanced,
        val overlaySafeArea: TimestampOverlaySafeArea = TimestampOverlaySafeArea.Standard,
        val overlayOffsetXStep: Int = 0,
        val overlayOffsetYStep: Int = 0,
        val aspectRatioPreset: TimestampAspectRatioPreset = TimestampAspectRatioPreset.FourThree,
        val cropZoomPreset: TimestampCropZoomPreset = TimestampCropZoomPreset.Fit,
        val cropOffsetXStep: Int = 0,
        val cropOffsetYStep: Int = 0,
    ) {
        val isExportEnabled: Boolean
            get() = hasSelectedPhoto && timestamp.isNotBlank()
    }

    sealed interface Intent {
        data class SyncExternal(
            val selectedImageBase64: String?,
            val previewImage: ImageBitmap?,
            val metadataTimestampLabel: String?,
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
        data class AspectRatioChanged(val value: TimestampAspectRatioPreset) : Intent
        data class CropZoomChanged(val value: TimestampCropZoomPreset) : Intent
        data class CropOffsetXChanged(val value: Int) : Intent
        data class CropOffsetYChanged(val value: Int) : Intent
    }
}
