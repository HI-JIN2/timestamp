package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun EditorHomeSection(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
    onEditTimestampRequest: (String) -> Unit,
    onExport: () -> Unit,
    palette: EditorPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Timestamp",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            ),
        )
        HomeActionRow(
            hasSelectedPhoto = state.hasSelectedPhoto,
            onPickPhoto = onPickPhoto,
            onOpenCropEditor = { onIntent(TimestampEditorUiContract.Intent.OpenCropEditor) },
        )
        PreviewPanel(
            state = state,
            onIntent = onIntent,
            onEditTimestampRequest = onEditTimestampRequest,
            onExport = onExport,
            palette = palette,
        )
    }
}

@Composable
private fun HomeActionRow(
    hasSelectedPhoto: Boolean,
    onPickPhoto: () -> Unit,
    onOpenCropEditor: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onPickPhoto,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = if (hasSelectedPhoto) "사진 다시 선택" else "사진 선택",
            )
        }
        Button(
            onClick = onOpenCropEditor,
            enabled = hasSelectedPhoto,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Icon(
                imageVector = Icons.Rounded.Crop,
                contentDescription = "크롭 편집",
            )
        }
    }
}
