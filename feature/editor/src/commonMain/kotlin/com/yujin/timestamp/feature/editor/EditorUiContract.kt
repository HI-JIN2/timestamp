package com.yujin.timestamp.feature.editor

import androidx.compose.ui.graphics.ImageBitmap
import com.yujin.timestamp.core.model.TimestampAspectRatio

object EditorUiContract {
    data class State(
        val selectedImageBase64: String? = null,
        val previewImage: ImageBitmap? = null,
        val isCropEditorVisible: Boolean = false,
        val hasSelectedPhoto: Boolean = false,
        val defaultTimestamp: String = "04.01.26  03:42",
        val timestamp: String = "04.01.26  03:42",
        val location: String = "SEOUL, KR",
        val exportMessage: String? = null,
        val overlayTone: OverlayTone = OverlayTone.ClassicAmber,
        val overlayAlignment: OverlayAlignment = OverlayAlignment.BottomEnd,
        val overlayScale: OverlayScale = OverlayScale.Small,
        val overlayInset: OverlayInset = OverlayInset.Tight,
        val overlaySafeArea: OverlaySafeArea = OverlaySafeArea.Off,
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

    sealed interface Action {
        data class SyncExternal(
            val selectedImageBase64: String?,
            val previewImage: ImageBitmap?,
            val metadataTimestampLabel: String?,
            val selectedTimestampLabel: String?,
            val exportMessage: String?,
        ) : Action

        data object PickPhoto : Action
        data class EditDateRequested(val value: String) : Action
        data class EditTimeRequested(val value: String) : Action
        data object Export : Action
        data object ExportMessageShown : Action
        data object ResetTimestamp : Action
        data class ToneChanged(val value: OverlayTone) : Action
        data object OpenCropEditor : Action
        data object CloseCropEditor : Action
        data class AspectRatioChanged(val value: TimestampAspectRatio) : Action
        data class CropRectChanged(
            val leftRatio: Float,
            val topRatio: Float,
            val widthRatio: Float,
            val heightRatio: Float,
        ) : Action
        data object ResetCrop : Action
    }
}
