package com.yujin.timestamp.feature.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetTimestampPreviewStateUseCase

class TimestampEditorViewModel(
    private val getTimestampPreviewState: GetTimestampPreviewStateUseCase,
) {
    var state by mutableStateOf(TimestampEditorContract.State())
        private set

    fun dispatch(intent: TimestampEditorContract.Intent) {
        state = when (intent) {
            is TimestampEditorContract.Intent.SyncExternal -> reduceExternal(intent)
            is TimestampEditorContract.Intent.TimestampChanged -> state.copy(timestamp = intent.value)
            TimestampEditorContract.Intent.ResetTimestamp -> state.copy(timestamp = state.defaultTimestamp)
            is TimestampEditorContract.Intent.ToneChanged -> state.copy(overlayTone = intent.value)
            is TimestampEditorContract.Intent.AlignmentChanged -> state.copy(overlayAlignment = intent.value)
            is TimestampEditorContract.Intent.ScaleChanged -> state.copy(overlayScale = intent.value)
            is TimestampEditorContract.Intent.InsetChanged -> state.copy(overlayInset = intent.value)
            is TimestampEditorContract.Intent.SafeAreaChanged -> state.copy(overlaySafeArea = intent.value)
            is TimestampEditorContract.Intent.OffsetXChanged -> state.copy(overlayOffsetXStep = intent.value.coerceIn(-3, 3))
            is TimestampEditorContract.Intent.OffsetYChanged -> state.copy(overlayOffsetYStep = intent.value.coerceIn(-3, 3))
            TimestampEditorContract.Intent.OpenCropEditor -> state.copy(isCropEditorVisible = true)
            TimestampEditorContract.Intent.CloseCropEditor -> state.copy(isCropEditorVisible = false)
            is TimestampEditorContract.Intent.AspectRatioChanged -> state.copy(aspectRatioPreset = intent.value)
            is TimestampEditorContract.Intent.CropGestureChanged -> state.copy(
                cropScale = (state.cropScale * intent.scaleDelta).coerceIn(1f, 4f),
                cropOffsetXRatio = (state.cropOffsetXRatio + intent.panDeltaXRatio).coerceIn(-1f, 1f),
                cropOffsetYRatio = (state.cropOffsetYRatio + intent.panDeltaYRatio).coerceIn(-1f, 1f),
            )
            TimestampEditorContract.Intent.ResetCrop -> state.copy(
                cropScale = 1f,
                cropOffsetXRatio = 0f,
                cropOffsetYRatio = 0f,
            )
        }
    }

    fun buildExportRequest(): TimestampExportRequest? {
        val imageBase64 = state.selectedImageBase64 ?: return null
        val timestamp = state.timestamp.takeIf { it.isNotBlank() } ?: return null

        return TimestampExportRequest(
            imageBase64 = imageBase64,
            timestamp = timestamp,
            location = state.location,
            timestampColorHex = state.overlayTone.timestampColorHex,
            locationColorHex = state.overlayTone.locationColorHex,
            shadowColorHex = state.overlayTone.shadowColorHex,
            alignmentKey = state.overlayAlignment.exportKey,
            scaleKey = state.overlayScale.exportKey,
            insetKey = state.overlayInset.exportKey,
            safeAreaKey = state.overlaySafeArea.exportKey,
            offsetXStep = state.overlayOffsetXStep,
            offsetYStep = state.overlayOffsetYStep,
            aspectRatioKey = state.aspectRatioPreset.exportKey,
            cropScale = state.cropScale,
            cropOffsetXRatio = state.cropOffsetXRatio,
            cropOffsetYRatio = state.cropOffsetYRatio,
        )
    }

    private fun reduceExternal(
        intent: TimestampEditorContract.Intent.SyncExternal,
    ): TimestampEditorContract.State {
        val hasSelectedPhoto = intent.previewImage != null || intent.selectedImageBase64 != null
        val preview = getTimestampPreviewState(
            hasSelectedPhoto = hasSelectedPhoto,
            metadataTimestampLabel = intent.metadataTimestampLabel,
        )
        val shouldResetTimestamp = state.selectedImageBase64 != intent.selectedImageBase64 ||
            state.defaultTimestamp != preview.timestampLabel

        return state.copy(
            selectedImageBase64 = intent.selectedImageBase64,
            previewImage = intent.previewImage,
            hasSelectedPhoto = hasSelectedPhoto,
            defaultTimestamp = preview.timestampLabel,
            timestamp = if (shouldResetTimestamp) preview.timestampLabel else state.timestamp,
            metadataDescription = preview.metadataDescription,
            location = preview.locationLabel,
            helperText = preview.helperText,
            exportMessage = intent.exportMessage,
        )
    }
}
