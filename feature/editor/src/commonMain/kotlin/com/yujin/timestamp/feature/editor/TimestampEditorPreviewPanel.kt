package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import timestamp.feature.editor.generated.resources.*

@Composable
internal fun PreviewPanel(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
    onEditDateRequest: (String) -> Unit,
    onEditTimeRequest: (String) -> Unit,
    onExport: () -> Unit,
    palette: EditorPalette,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RectangleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PreviewCanvas(
                state = state,
                palette = palette,
            )

            TimestampFieldRow(
                timestamp = state.timestamp,
                enabled = state.hasSelectedPhoto,
                onEditDateRequest = onEditDateRequest,
                onEditTimeRequest = onEditTimeRequest,
            )

            OverlayControlRow(
                label = stringResource(Res.string.overlay_tone),
                options = TimestampOverlayTone.entries,
                selected = state.overlayTone,
                optionLabelRes = { it.labelRes },
                onSelected = { onIntent(TimestampEditorUiContract.Intent.ToneChanged(it)) },
            )

            HorizontalDivider(color = palette.divider)
            PrimaryActionRow(
                hasSelectedPhoto = state.hasSelectedPhoto,
                isExportEnabled = state.isExportEnabled,
                onResetTimestamp = { onIntent(TimestampEditorUiContract.Intent.ResetTimestamp) },
                onExport = onExport,
            )
        }
    }
}

@Composable
private fun TimestampFieldRow(
    timestamp: String,
    enabled: Boolean,
    onEditDateRequest: (String) -> Unit,
    onEditTimeRequest: (String) -> Unit,
) {
    val datePart = timestamp.substringBefore("  ", timestamp)
    val timePart = timestamp.substringAfter("  ", "")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { onEditDateRequest(timestamp) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
        ) {
            Text(datePart)
        }
        Button(
            onClick = { onEditTimeRequest(timestamp) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
        ) {
            Text(timePart)
        }
    }
}

@Composable
private fun PreviewCanvas(
    state: TimestampEditorUiContract.State,
    palette: EditorPalette,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(state.aspectRatioPreset.ratio)
            .background(Brush.verticalGradient(colors = palette.previewGradient))
            .border(1.dp, palette.previewBorder),
    ) {
        if (state.previewImage != null) {
            GestureDrivenImage(
                previewImage = state.previewImage,
                cropLeftRatio = state.cropLeftRatio,
                cropTopRatio = state.cropTopRatio,
                cropWidthRatio = state.cropWidthRatio,
                cropHeightRatio = state.cropHeightRatio,
            )
        } else if (!state.hasSelectedPhoto) {
            Text(
                text = stringResource(Res.string.preview_placeholder),
                modifier = Modifier.align(Alignment.Center),
                color = palette.placeholderText,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        TimestampOverlay(
            timestamp = state.timestamp,
            location = state.location,
            tone = state.overlayTone,
            alignment = state.overlayAlignment,
            scale = state.overlayScale,
            inset = state.overlayInset,
            safeArea = state.overlaySafeArea,
            offsetXStep = state.overlayOffsetXStep,
            offsetYStep = state.overlayOffsetYStep,
        )
    }
}

@Composable
private fun PrimaryActionRow(
    hasSelectedPhoto: Boolean,
    isExportEnabled: Boolean,
    onResetTimestamp: () -> Unit,
    onExport: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onResetTimestamp,
            enabled = hasSelectedPhoto,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
        ) {
            Text(stringResource(Res.string.reset_default))
        }
        Button(
            onClick = onExport,
            enabled = isExportEnabled,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Rounded.SaveAlt,
                contentDescription = stringResource(Res.string.export),
            )
        }
    }
}

@Composable
private fun GestureDrivenImage(
    previewImage: ImageBitmap,
    cropLeftRatio: Float,
    cropTopRatio: Float,
    cropWidthRatio: Float,
    cropHeightRatio: Float,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawImage(
            image = previewImage,
            srcOffset = androidx.compose.ui.unit.IntOffset(
                x = (previewImage.width * cropLeftRatio).toInt(),
                y = (previewImage.height * cropTopRatio).toInt(),
            ),
            srcSize = androidx.compose.ui.unit.IntSize(
                width = (previewImage.width * cropWidthRatio).toInt().coerceAtLeast(1),
                height = (previewImage.height * cropHeightRatio).toInt().coerceAtLeast(1),
            ),
            dstSize = androidx.compose.ui.unit.IntSize(
                width = size.width.toInt().coerceAtLeast(1),
                height = size.height.toInt().coerceAtLeast(1),
            ),
        )
    }
}

@Composable
private fun BoxScope.TimestampOverlay(
    timestamp: String,
    location: String,
    tone: TimestampOverlayTone,
    alignment: TimestampOverlayAlignment,
    scale: TimestampOverlayScale,
    inset: TimestampOverlayInset,
    safeArea: TimestampOverlaySafeArea,
    offsetXStep: Int,
    offsetYStep: Int,
) {
    Column(
        modifier = Modifier
            .align(alignment.containerAlignment)
            .padding(inset.previewPaddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = if (alignment == TimestampOverlayAlignment.BottomEnd) Alignment.End else Alignment.Start,
    ) {
        Column(
            modifier = Modifier.offset(
                x = (offsetXStep * 10).dp,
                y = ((offsetYStep * -8) - safeArea.extraPreviewBottomDp).dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = if (alignment == TimestampOverlayAlignment.BottomEnd) Alignment.End else Alignment.Start,
        ) {
            Text(
                text = timestamp,
                color = tone.timestampColor,
                fontSize = scale.timestampFontSp.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp,
                textAlign = if (alignment == TimestampOverlayAlignment.BottomEnd) TextAlign.End else TextAlign.Start,
                style = overlayTextStyle(tone.shadowColor, 8f, 3f),
            )
            Text(
                text = location,
                color = tone.locationColor,
                style = MaterialTheme.typography.labelLarge
                    .copy(fontSize = scale.locationFontSp.sp)
                    .merge(overlayTextStyle(tone.shadowColor, 6f, 2f)),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}
