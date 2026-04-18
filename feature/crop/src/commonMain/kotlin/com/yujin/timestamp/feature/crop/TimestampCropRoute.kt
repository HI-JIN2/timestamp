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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yujin.timestamp.core.model.TimestampAspectRatio
import org.jetbrains.compose.resources.stringResource
import timestamp.feature.crop.generated.resources.Res
import timestamp.feature.crop.generated.resources.aspect_ratio_sixteen_nine
import timestamp.feature.crop.generated.resources.aspect_ratio_three_four
import timestamp.feature.crop.generated.resources.crop_editor_title
import timestamp.feature.crop.generated.resources.crop_ratio
import timestamp.feature.crop.generated.resources.done
import timestamp.feature.crop.generated.resources.reset

@Composable
fun TimestampCropRoute(
    state: TimestampCropUiContract.State,
    actions: (TimestampCropUiContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberTimestampCropPalette(isSystemInDarkTheme())

    MaterialTheme(colorScheme = timestampCropColorScheme(isSystemInDarkTheme())) {
        TimestampCropScreen(
            state = state,
            actions = actions,
            modifier = modifier,
            palette = palette,
        )
    }
}

@Composable
internal fun TimestampCropScreen(
    state: TimestampCropUiContract.State,
    actions: (TimestampCropUiContract.Action) -> Unit,
    modifier: Modifier = Modifier,
    palette: TimestampCropPalette,
) {
    Column(
        modifier = modifier
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
                    onClick = { actions(TimestampCropUiContract.Action.Reset) },
                    shape = RectangleShape,
                    colors = cropActionButtonColors(),
                ) { Text(stringResource(Res.string.reset)) }
                Button(
                    onClick = { actions(TimestampCropUiContract.Action.Done) },
                    shape = RectangleShape,
                    colors = cropActionButtonColors(),
                ) { Text(stringResource(Res.string.done)) }
            }
        }

        CropControlRow(
            label = stringResource(Res.string.crop_ratio),
            options = TimestampAspectRatio.entries,
            selected = state.aspectRatio,
            optionLabelRes = {
                when (it) {
                    TimestampAspectRatio.ThreeFour -> Res.string.aspect_ratio_three_four
                    TimestampAspectRatio.SixteenNine -> Res.string.aspect_ratio_sixteen_nine
                }
            },
            onSelected = { actions(TimestampCropUiContract.Action.AspectRatioChanged(it)) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(palette.background),
            contentAlignment = Alignment.Center,
        ) {
            TimestampCropCanvas(
                previewImage = state.previewImage,
                aspectRatioPreset = state.aspectRatio,
                cropLeftRatio = state.cropLeftRatio,
                cropTopRatio = state.cropTopRatio,
                cropWidthRatio = state.cropWidthRatio,
                cropHeightRatio = state.cropHeightRatio,
                palette = palette,
                onCropRectChanged = { leftRatio, topRatio, widthRatio, heightRatio ->
                    actions(
                        TimestampCropUiContract.Action.CropRectChanged(
                            leftRatio = leftRatio,
                            topRatio = topRatio,
                            widthRatio = widthRatio,
                            heightRatio = heightRatio,
                        ),
                    )
                },
            )
        }
    }
}
