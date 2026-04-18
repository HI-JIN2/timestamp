package com.yujin.timestamp.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetEditorInitialStateUseCase

@Composable
fun TimestampEditorRoute(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    selectedTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onEditDateRequest: (String) -> Unit = {},
    onEditTimeRequest: (String) -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
    onExportMessageConsumed: () -> Unit = {},
) {
    val previewImage = remember(selectedImageBase64) {
        selectedImageBase64?.let(::decodeSelectedImage)
    }
    val viewModel = remember {
        TimestampEditorViewModel(GetEditorInitialStateUseCase())
    }

    LaunchedEffect(selectedImageBase64, metadataTimestampLabel, selectedTimestampLabel, exportMessage, previewImage) {
        viewModel.dispatch(
            TimestampEditorUiContract.Intent.SyncExternal(
                selectedImageBase64 = selectedImageBase64,
                previewImage = previewImage,
                metadataTimestampLabel = metadataTimestampLabel,
                selectedTimestampLabel = selectedTimestampLabel,
                exportMessage = exportMessage,
            ),
        )
    }

    val state by remember { derivedStateOf { viewModel.state } }

    TimestampEditorScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onPickPhoto = onPickPhoto,
        onEditDateRequest = onEditDateRequest,
        onEditTimeRequest = onEditTimeRequest,
        onExport = { viewModel.buildExportRequest()?.let(onExport) },
        onExportMessageConsumed = onExportMessageConsumed,
    )
}
