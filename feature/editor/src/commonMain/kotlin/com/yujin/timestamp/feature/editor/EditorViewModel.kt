package com.yujin.timestamp.feature.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetEditorInitialStateUseCase
import com.yujin.timestamp.feature.crop.defaultCropRect

class EditorViewModel(
    private val getEditorInitialState: GetEditorInitialStateUseCase,
) {
    var state by mutableStateOf(EditorUiContract.State())
        private set

    fun dispatch(intent: EditorUiContract.Action) {
        state = when (intent) {
            is EditorUiContract.Action.SyncExternal -> reduceExternal(intent)
            EditorUiContract.Action.ResetTimestamp -> state.copy(timestamp = state.defaultTimestamp)
            is EditorUiContract.Action.ToneChanged -> state.copy(overlayTone = intent.value)
            EditorUiContract.Action.OpenCropEditor -> state.copy(isCropEditorVisible = true)
            EditorUiContract.Action.CloseCropEditor -> state.copy(isCropEditorVisible = false)
            is EditorUiContract.Action.AspectRatioChanged -> {
                val cropRect = defaultCropRect(
                    previewImage = state.previewImage,
                    aspectRatio = intent.value,
                )
                state.copy(
                    aspectRatioPreset = intent.value,
                    cropLeftRatio = cropRect.leftRatio,
                    cropTopRatio = cropRect.topRatio,
                    cropWidthRatio = cropRect.widthRatio,
                    cropHeightRatio = cropRect.heightRatio,
                )
            }
            is EditorUiContract.Action.CropRectChanged -> state.copy(
                cropLeftRatio = intent.leftRatio,
                cropTopRatio = intent.topRatio,
                cropWidthRatio = intent.widthRatio,
                cropHeightRatio = intent.heightRatio,
            )
            EditorUiContract.Action.ResetCrop -> resetCrop()
            EditorUiContract.Action.PickPhoto,
            is EditorUiContract.Action.EditDateRequested,
            is EditorUiContract.Action.EditTimeRequested,
            EditorUiContract.Action.Export,
            EditorUiContract.Action.ExportMessageShown -> state
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
            aspectRatio = state.aspectRatioPreset,
            cropLeftRatio = state.cropLeftRatio,
            cropTopRatio = state.cropTopRatio,
            cropWidthRatio = state.cropWidthRatio,
            cropHeightRatio = state.cropHeightRatio,
        )
    }

    private fun reduceExternal(
        intent: EditorUiContract.Action.SyncExternal,
    ): EditorUiContract.State {
        val hasSelectedPhoto = intent.previewImage != null || intent.selectedImageBase64 != null
        val initialState = getEditorInitialState(
            metadataTimestampLabel = intent.metadataTimestampLabel,
        )
        val shouldResetTimestamp = state.selectedImageBase64 != intent.selectedImageBase64 ||
            state.defaultTimestamp != initialState.timestampLabel
        val shouldResetCrop = state.selectedImageBase64 != intent.selectedImageBase64
        val defaultCropRect = defaultCropRect(
            previewImage = intent.previewImage,
            aspectRatio = state.aspectRatioPreset,
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

    private fun resetCrop(): EditorUiContract.State {
        val cropRect = defaultCropRect(
            previewImage = state.previewImage,
            aspectRatio = state.aspectRatioPreset,
        )
        return state.copy(
            cropLeftRatio = cropRect.leftRatio,
            cropTopRatio = cropRect.topRatio,
            cropWidthRatio = cropRect.widthRatio,
            cropHeightRatio = cropRect.heightRatio,
        )
    }
}
