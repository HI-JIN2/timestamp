package com.yujin.timestamp.app

import androidx.compose.ui.window.ComposeUIViewController
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.feature.editor.EditorRoute
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    selectedTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onEditDateRequest: (String) -> Unit = {},
    onEditTimeRequest: (String) -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
    onExportMessageConsumed: () -> Unit = {},
): UIViewController = ComposeUIViewController {
    EditorRoute(
        selectedImageBase64 = selectedImageBase64,
        metadataTimestampLabel = metadataTimestampLabel,
        selectedTimestampLabel = selectedTimestampLabel,
        exportMessage = exportMessage,
        onPickPhoto = onPickPhoto,
        onEditDateRequest = onEditDateRequest,
        onEditTimeRequest = onEditTimeRequest,
        onExport = onExport,
        onExportMessageConsumed = onExportMessageConsumed,
    )
}
