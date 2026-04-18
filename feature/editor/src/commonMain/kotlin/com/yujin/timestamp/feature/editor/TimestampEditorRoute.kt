package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetTimestampPreviewStateUseCase

@Composable
fun TimestampEditorRoute(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
) {
    val previewImage = remember(selectedImageBase64) {
        selectedImageBase64?.let(::decodeSelectedImage)
    }
    val viewModel = remember {
        TimestampEditorViewModel(GetTimestampPreviewStateUseCase())
    }

    LaunchedEffect(selectedImageBase64, metadataTimestampLabel, exportMessage, previewImage) {
        viewModel.dispatch(
            TimestampEditorContract.Intent.SyncExternal(
                selectedImageBase64 = selectedImageBase64,
                previewImage = previewImage,
                metadataTimestampLabel = metadataTimestampLabel,
                exportMessage = exportMessage,
            ),
        )
    }

    val state by remember { derivedStateOf { viewModel.state } }

    TimestampEditorScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onPickPhoto = onPickPhoto,
        onExport = { viewModel.buildExportRequest()?.let(onExport) },
    )
}

@Composable
private fun TimestampEditorScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
    onExport: () -> Unit,
) {
    MaterialTheme(colorScheme = retroColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (state.isCropEditorVisible && state.previewImage != null) {
                CropEditorScreen(
                    state = state,
                    onIntent = onIntent,
                )
            } else {
                EditorHomeScreen(
                    state = state,
                    onIntent = onIntent,
                    onPickPhoto = onPickPhoto,
                    onExport = onExport,
                )
            }
        }
    }
}

@Composable
private fun EditorHomeScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
    onExport: () -> Unit,
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
                letterSpacing = (-0.5).sp,
            ),
        )
        Text(
            text = "사진 선택 후 별도 크롭 화면에서 4:3 또는 16:9 비율을 고르고 손가락으로 직접 구도를 조정합니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onPickPhoto) {
                Text(if (state.hasSelectedPhoto) "사진 다시 선택" else "사진 선택")
            }
            Button(
                onClick = { onIntent(TimestampEditorContract.Intent.OpenCropEditor) },
                enabled = state.hasSelectedPhoto,
            ) {
                Text("크롭 편집")
            }
        }
        PreviewCard(
            state = state,
            onIntent = onIntent,
            onExport = onExport,
        )
    }
}

@Composable
private fun CropEditorScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
) {
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
                text = "크롭 편집",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onIntent(TimestampEditorContract.Intent.ResetCrop) }) {
                    Text("초기화")
                }
                Button(onClick = { onIntent(TimestampEditorContract.Intent.CloseCropEditor) }) {
                    Text("완료")
                }
            }
        }

        Text(
            text = "화면에서 두 손가락으로 확대/축소하고 드래그로 구도를 맞춥니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OverlayControlRow(
            label = "크롭 비율",
            options = TimestampAspectRatioPreset.entries,
            selected = state.aspectRatioPreset,
            optionLabel = { it.label },
            onSelected = { onIntent(TimestampEditorContract.Intent.AspectRatioChanged(it)) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF16120F)),
            contentAlignment = Alignment.Center,
        ) {
            CropGestureSurface(
                previewImage = state.previewImage ?: return@Box,
                aspectRatioPreset = state.aspectRatioPreset,
                cropScale = state.cropScale,
                cropOffsetXRatio = state.cropOffsetXRatio,
                cropOffsetYRatio = state.cropOffsetYRatio,
                onGesture = { scaleDelta, panDeltaXRatio, panDeltaYRatio ->
                    onIntent(
                        TimestampEditorContract.Intent.CropGestureChanged(
                            scaleDelta = scaleDelta,
                            panDeltaXRatio = panDeltaXRatio,
                            panDeltaYRatio = panDeltaYRatio,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun CropGestureSurface(
    previewImage: ImageBitmap,
    aspectRatioPreset: TimestampAspectRatioPreset,
    cropScale: Float,
    cropOffsetXRatio: Float,
    cropOffsetYRatio: Float,
    onGesture: (Float, Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatioPreset.ratio)
                .border(1.dp, Color.White.copy(alpha = 0.35f))
                .pointerInput(aspectRatioPreset, cropScale, cropOffsetXRatio, cropOffsetYRatio) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val width = size.width.coerceAtLeast(1).toFloat()
                        val height = size.height.coerceAtLeast(1).toFloat()
                        onGesture(
                            zoom,
                            pan.x / (width * 0.5f),
                        pan.y / (height * 0.5f),
                    )
                }
            },
    ) {
        GestureDrivenImage(
            previewImage = previewImage,
            cropScale = cropScale,
            cropOffsetXRatio = cropOffsetXRatio,
            cropOffsetYRatio = cropOffsetYRatio,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(2.dp, Color(0xFFE6D8BE)),
        )
    }
}

@Composable
private fun PreviewCard(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onExport: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("프리뷰", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (state.hasSelectedPhoto) "사진 선택 완료" else "사진 미선택",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.hasSelectedPhoto) Color(0xFF2F6A3D) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(state.aspectRatioPreset.ratio)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE6D8BE), Color(0xFFC7B08A), Color(0xFF6C5A45)),
                        ),
                    )
                    .border(1.dp, Color(0x1A000000)),
            ) {
                if (state.previewImage != null) {
                    GestureDrivenImage(
                        previewImage = state.previewImage,
                        cropScale = state.cropScale,
                        cropOffsetXRatio = state.cropOffsetXRatio,
                        cropOffsetYRatio = state.cropOffsetYRatio,
                    )
                } else if (!state.hasSelectedPhoto) {
                    Text(
                        text = "선택한 사진이 여기에 표시됩니다",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4F4131),
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

            Text(state.helperText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            if (state.exportMessage != null) {
                Text(state.exportMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = state.timestamp,
                onValueChange = { onIntent(TimestampEditorContract.Intent.TimestampChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("타임스탬프") },
                supportingText = { Text(state.metadataDescription) },
                singleLine = true,
            )

            OverlayControlRow("오버레이 톤", TimestampOverlayTone.entries, state.overlayTone, { it.label }) {
                onIntent(TimestampEditorContract.Intent.ToneChanged(it))
            }

            HorizontalDivider(color = Color(0x14000000))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onIntent(TimestampEditorContract.Intent.ResetTimestamp) },
                    enabled = state.hasSelectedPhoto,
                ) { Text("기본값 복원") }
                Button(onClick = onExport, enabled = state.isExportEnabled) { Text("내보내기") }
            }
        }
    }
}

@Composable
private fun GestureDrivenImage(
    previewImage: ImageBitmap,
    cropScale: Float,
    cropOffsetXRatio: Float,
    cropOffsetYRatio: Float,
) {
    Image(
        bitmap = previewImage,
        contentDescription = "선택한 사진 프리뷰",
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = cropScale,
                scaleY = cropScale,
                translationX = cropOffsetXRatio * 180f,
                translationY = cropOffsetYRatio * 180f,
                transformOrigin = TransformOrigin.Center,
            ),
        contentScale = ContentScale.Crop,
    )
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
        modifier = Modifier.align(alignment.containerAlignment).padding(inset.previewPaddingDp.dp),
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
                style = MaterialTheme.typography.labelLarge.copy(fontSize = scale.locationFontSp.sp)
                    .merge(overlayTextStyle(tone.shadowColor, 6f, 2f)),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun <T> OverlayControlRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(optionLabel(option)) },
                )
            }
        }
    }
}

private fun overlayTextStyle(
    shadowColor: Color,
    blurRadius: Float,
    yOffset: Float,
): TextStyle = TextStyle(
    shadow = Shadow(
        color = shadowColor,
        offset = Offset(0f, yOffset),
        blurRadius = blurRadius,
    ),
)

@Composable
private fun retroColorScheme() = lightColorScheme(
    primary = Color(0xFFAB5C1D),
    onPrimary = Color(0xFFFFF7F1),
    secondary = Color(0xFF73614C),
    background = Color(0xFFF6F0E6),
    surface = Color(0xFFFFFBF5),
    surfaceVariant = Color(0xFFE8DDCF),
    onSurfaceVariant = Color(0xFF594835),
)
