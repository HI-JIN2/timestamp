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
            is TimestampEditorUiContract.Intent.AspectRatioChanged -> {
                val cropRect = defaultCropRect(
                    previewImage = state.previewImage,
                    aspectRatio = intent.value.ratio,
                )
                state.copy(
                    aspectRatioPreset = intent.value,
                    cropLeftRatio = cropRect.leftRatio,
                    cropTopRatio = cropRect.topRatio,
                    cropWidthRatio = cropRect.widthRatio,
                    cropHeightRatio = cropRect.heightRatio,
                )
            }
            is TimestampEditorUiContract.Intent.CropRectChanged -> state.copy(
                cropLeftRatio = intent.leftRatio,
                cropTopRatio = intent.topRatio,
                cropWidthRatio = intent.widthRatio,
                cropHeightRatio = intent.heightRatio,
            )
            TimestampEditorUiContract.Intent.ResetCrop -> resetCrop()
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
            cropLeftRatio = state.cropLeftRatio,
            cropTopRatio = state.cropTopRatio,
            cropWidthRatio = state.cropWidthRatio,
            cropHeightRatio = state.cropHeightRatio,
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
        val shouldResetCrop = state.selectedImageBase64 != intent.selectedImageBase64
        val defaultCropRect = defaultCropRect(
            previewImage = intent.previewImage,
            aspectRatio = state.aspectRatioPreset.ratio,
        )

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
            cropLeftRatio = if (shouldResetCrop) defaultCropRect.leftRatio else state.cropLeftRatio,
            cropTopRatio = if (shouldResetCrop) defaultCropRect.topRatio else state.cropTopRatio,
            cropWidthRatio = if (shouldResetCrop) defaultCropRect.widthRatio else state.cropWidthRatio,
            cropHeightRatio = if (shouldResetCrop) defaultCropRect.heightRatio else state.cropHeightRatio,
        )
    }

    private fun resetCrop(): TimestampEditorUiContract.State {
        val cropRect = defaultCropRect(
            previewImage = state.previewImage,
            aspectRatio = state.aspectRatioPreset.ratio,
        )
        return state.copy(
            cropLeftRatio = cropRect.leftRatio,
            cropTopRatio = cropRect.topRatio,
            cropWidthRatio = cropRect.widthRatio,
            cropHeightRatio = cropRect.heightRatio,
        )
    }
}
