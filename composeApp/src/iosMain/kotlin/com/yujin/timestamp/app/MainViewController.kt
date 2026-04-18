package com.yujin.timestamp.app

import androidx.compose.ui.window.ComposeUIViewController
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.feature.editor.TimestampEditorRoute
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    selectedTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onEditTimestampRequest: (String) -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
    onExportMessageConsumed: () -> Unit = {},
): UIViewController = ComposeUIViewController {
    TimestampEditorRoute(
        selectedImageBase64 = selectedImageBase64,
        metadataTimestampLabel = metadataTimestampLabel,
        selectedTimestampLabel = selectedTimestampLabel,
        exportMessage = exportMessage,
        onPickPhoto = onPickPhoto,
        onEditTimestampRequest = onEditTimestampRequest,
        onExport = onExport,
        onExportMessageConsumed = onExportMessageConsumed,
    )
}
