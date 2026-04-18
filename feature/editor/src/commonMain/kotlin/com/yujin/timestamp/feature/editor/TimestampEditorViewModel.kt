package com.yujin.timestamp.feature.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetEditorInitialStateUseCase

class TimestampEditorViewModel(
    private val getEditorInitialState: GetEditorInitialStateUseCase,
) {
    var state by mutableStateOf(TimestampEditorUiContract.State())
        private set

    fun dispatch(intent: TimestampEditorUiContract.Intent) {
        state = when (intent) {
            is TimestampEditorUiContract.Intent.SyncExternal -> reduceExternal(intent)
            TimestampEditorUiContract.Intent.ResetTimestamp -> state.copy(timestamp = state.defaultTimestamp)
            is TimestampEditorUiContract.Intent.ToneChanged -> state.copy(overlayTone = intent.value)
            TimestampEditorUiContract.Intent.OpenCropEditor -> state.copy(isCropEditorVisible = true)
            TimestampEditorUiContract.Intent.CloseCropEditor -> state.copy(isCropEditorVisible = false)
            is TimestampEditorUiContract.Intent.AspectRatioChanged -> state.copy(aspectRatioPreset = intent.value)
            is TimestampEditorUiContract.Intent.CropFrameDragged -> state.copy(
                cropOffsetXRatio = (state.cropOffsetXRatio + intent.deltaXRatio).coerceIn(-1f, 1f),
                cropOffsetYRatio = (state.cropOffsetYRatio + intent.deltaYRatio).coerceIn(-1f, 1f),
            )
            is TimestampEditorUiContract.Intent.CropFrameScaled -> state.copy(
                cropScale = (state.cropScale * intent.scaleDelta).coerceIn(1f, 4f),
            )
            TimestampEditorUiContract.Intent.ResetCrop -> state.copy(
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
        intent: TimestampEditorUiContract.Intent.SyncExternal,
    ): TimestampEditorUiContract.State {
        val hasSelectedPhoto = intent.previewImage != null || intent.selectedImageBase64 != null
        val initialState = getEditorInitialState(
            metadataTimestampLabel = intent.metadataTimestampLabel,
        )
        val shouldResetTimestamp = state.selectedImageBase64 != intent.selectedImageBase64 ||
            state.defaultTimestamp != initialState.timestampLabel

        return state.copy(
            selectedImageBase64 = intent.selectedImageBase64,
            previewImage = intent.previewImage,
            hasSelectedPhoto = hasSelectedPhoto,
            defaultTimestamp = initialState.timestampLabel,
            timestamp = when {
                intent.selectedTimestampLabel != null -> intent.selectedTimestampLabel
                shouldResetTimestamp -> initialState.timestampLabel
                else -> state.timestamp
            },
            location = initialState.locationLabel,
            exportMessage = intent.exportMessage,
        )
    }
}
