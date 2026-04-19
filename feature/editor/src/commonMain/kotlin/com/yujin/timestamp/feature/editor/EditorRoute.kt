package com.yujin.timestamp.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetEditorInitialStateUseCase

@Composable
fun EditorRoute(
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
        EditorViewModel(GetEditorInitialStateUseCase())
    }

    LaunchedEffect(selectedImageBase64, metadataTimestampLabel, selectedTimestampLabel, exportMessage, previewImage) {
        viewModel.dispatch(
            EditorUiContract.Action.SyncExternal(
                selectedImageBase64 = selectedImageBase64,
                previewImage = previewImage,
                metadataTimestampLabel = metadataTimestampLabel,
                selectedTimestampLabel = selectedTimestampLabel,
                exportMessage = exportMessage,
            ),
        )
    }

    val state by remember { derivedStateOf { viewModel.state } }
    val actions: (EditorUiContract.Action) -> Unit = remember(
        viewModel,
        state,
        onPickPhoto,
        onEditDateRequest,
        onEditTimeRequest,
        onExport,
        onExportMessageConsumed,
    ) {
        { action ->
            when (action) {
                EditorUiContract.Action.PickPhoto -> onPickPhoto()
                is EditorUiContract.Action.EditDateRequested -> onEditDateRequest(action.value)
                is EditorUiContract.Action.EditTimeRequested -> onEditTimeRequest(action.value)
                EditorUiContract.Action.Export -> {
                    viewModel.buildExportRequest()?.let(onExport)
                }
                EditorUiContract.Action.ExportMessageShown -> onExportMessageConsumed()
                else -> viewModel.dispatch(action)
            }
        }
    }

    EditorScreen(
        state = state,
        actions = actions,
    )
}
