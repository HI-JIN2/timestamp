package com.yujin.timestamp.feature.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import timestamp.feature.crop.generated.resources.Res
import timestamp.feature.crop.generated.resources.crop_editor_title
import timestamp.feature.crop.generated.resources.crop_ratio
import timestamp.feature.crop.generated.resources.done
import timestamp.feature.crop.generated.resources.reset

@Composable
fun TimestampCropRoute(
    previewImage: ImageBitmap,
    aspectRatioPreset: TimestampAspectRatioPreset,
    cropLeftRatio: Float,
    cropTopRatio: Float,
    cropWidthRatio: Float,
    cropHeightRatio: Float,
    onAspectRatioChanged: (TimestampAspectRatioPreset) -> Unit,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
    onResetCrop: () -> Unit,
    onClose: () -> Unit,
) {
    val palette = rememberTimestampCropPalette(isSystemInDarkTheme())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.crop_editor_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onResetCrop,
                    shape = RectangleShape,
                    colors = cropActionButtonColors(),
                ) { Text(stringResource(Res.string.reset)) }
                Button(
                    onClick = onClose,
                    shape = RectangleShape,
                    colors = cropActionButtonColors(),
                ) { Text(stringResource(Res.string.done)) }
            }
        }

        CropControlRow(
            label = stringResource(Res.string.crop_ratio),
            options = TimestampAspectRatioPreset.entries,
            selected = aspectRatioPreset,
            optionLabelRes = { it.labelRes },
            onSelected = onAspectRatioChanged,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(palette.background),
            contentAlignment = Alignment.Center,
        ) {
            TimestampCropCanvas(
                previewImage = previewImage,
                aspectRatioPreset = aspectRatioPreset,
                cropLeftRatio = cropLeftRatio,
                cropTopRatio = cropTopRatio,
                cropWidthRatio = cropWidthRatio,
                cropHeightRatio = cropHeightRatio,
                palette = palette,
                onCropRectChanged = onCropRectChanged,
            )
        }
    }
}
