package com.yujin.timestamp.app

import androidx.compose.ui.window.ComposeUIViewController
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.feature.editor.TimestampEditorRoute
import platform.UIKit.UIViewController

fun MainViewController(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
): UIViewController = ComposeUIViewController {
    TimestampEditorRoute(
        selectedImageBase64 = selectedImageBase64,
        metadataTimestampLabel = metadataTimestampLabel,
        exportMessage = exportMessage,
        onPickPhoto = onPickPhoto,
        onExport = onExport,
    )
}
