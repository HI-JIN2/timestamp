package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
internal fun CropEditorSection(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
    palette: EditorPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CropEditorHeader(
            onResetCrop = { onIntent(TimestampEditorUiContract.Intent.ResetCrop) },
            onClose = { onIntent(TimestampEditorUiContract.Intent.CloseCropEditor) },
        )

        CropAspectRatioRow(
            selected = state.aspectRatioPreset,
            onSelected = { onIntent(TimestampEditorUiContract.Intent.AspectRatioChanged(it)) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(palette.cropBackground),
            contentAlignment = Alignment.Center,
        ) {
            CropGestureSurface(
                previewImage = state.previewImage ?: return@Box,
                aspectRatioPreset = state.aspectRatioPreset,
                cropScale = state.cropScale,
                cropOffsetXRatio = state.cropOffsetXRatio,
                cropOffsetYRatio = state.cropOffsetYRatio,
                palette = palette,
                onFrameDragged = { deltaXRatio, deltaYRatio ->
                    onIntent(TimestampEditorUiContract.Intent.CropFrameDragged(deltaXRatio, deltaYRatio))
                },
                onFrameScaled = { scaleDelta ->
                    onIntent(TimestampEditorUiContract.Intent.CropFrameScaled(scaleDelta))
                },
            )
        }
    }
}

@Composable
private fun CropEditorHeader(
    onResetCrop: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "크롭 편집",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onResetCrop,
                shape = RectangleShape,
                colors = retroActionButtonColors(),
            ) {
                Text("초기화")
            }
            Button(
                onClick = onClose,
                shape = RectangleShape,
                colors = retroActionButtonColors(),
            ) {
                Text("완료")
            }
        }
    }
}

@Composable
private fun CropAspectRatioRow(
    selected: TimestampAspectRatioPreset,
    onSelected: (TimestampAspectRatioPreset) -> Unit,
) {
    OverlayControlRow(
        label = "크롭 비율",
        options = TimestampAspectRatioPreset.entries,
        selected = selected,
        optionLabel = { it.label },
        onSelected = onSelected,
    )
}
