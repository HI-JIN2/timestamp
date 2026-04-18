package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
                    text = "Clean Architecture 기반 멀티모듈 구조에서 동작하는 MVVM + MVI 편집 화면입니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PreviewCard(
                    state = state,
                    onIntent = onIntent,
                    onPickPhoto = onPickPhoto,
                    onExport = onExport,
                )
                RoadmapCard(
                    title = "다음 구현 순서",
                    lines = listOf(
                        "1. 저장용 비트맵에 동일 오버레이 합성",
                        "2. 타임스탬프 폰트/간격 프리셋 확장",
                        "3. 위치 미세 조정과 안전 영역 처리",
                        "4. 저장 및 공유 파이프라인 정리",
                    ),
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
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
            Text("프리뷰 구조", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (state.hasSelectedPhoto) "사진 선택 완료" else "사진 미선택",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.hasSelectedPhoto) Color(0xFF2F6A3D) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(state.aspectRatioPreset.ratio)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE6D8BE), Color(0xFFC7B08A), Color(0xFF6C5A45)),
                        ),
                    )
                    .border(1.dp, Color(0x1A000000), RoundedCornerShape(22.dp)),
            ) {
                if (state.previewImage != null) {
                    CroppedPreviewImage(
                        previewImage = state.previewImage,
                        cropZoomPreset = state.cropZoomPreset,
                        cropOffsetXStep = state.cropOffsetXStep,
                        cropOffsetYStep = state.cropOffsetYStep,
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
            OverlayControlRow("오버레이 위치", TimestampOverlayAlignment.entries, state.overlayAlignment, { it.label }) {
                onIntent(TimestampEditorContract.Intent.AlignmentChanged(it))
            }
            OverlayControlRow("글자 크기", TimestampOverlayScale.entries, state.overlayScale, { it.label }) {
                onIntent(TimestampEditorContract.Intent.ScaleChanged(it))
            }
            OverlayControlRow("하단 여백", TimestampOverlayInset.entries, state.overlayInset, { it.label }) {
                onIntent(TimestampEditorContract.Intent.InsetChanged(it))
            }
            OverlayControlRow("안전 영역", TimestampOverlaySafeArea.entries, state.overlaySafeArea, { it.label }) {
                onIntent(TimestampEditorContract.Intent.SafeAreaChanged(it))
            }
            NudgeControlRow("좌우 미세 조정", state.overlayOffsetXStep) {
                onIntent(TimestampEditorContract.Intent.OffsetXChanged(it))
            }
            NudgeControlRow("상하 미세 조정", state.overlayOffsetYStep) {
                onIntent(TimestampEditorContract.Intent.OffsetYChanged(it))
            }
            OverlayControlRow("비율 선택", TimestampAspectRatioPreset.entries, state.aspectRatioPreset, { it.label }) {
                onIntent(TimestampEditorContract.Intent.AspectRatioChanged(it))
            }
            OverlayControlRow("크롭 줌", TimestampCropZoomPreset.entries, state.cropZoomPreset, { it.label }) {
                onIntent(TimestampEditorContract.Intent.CropZoomChanged(it))
            }
            NudgeControlRow("크롭 좌우 구도", state.cropOffsetXStep) {
                onIntent(TimestampEditorContract.Intent.CropOffsetXChanged(it))
            }
            NudgeControlRow("크롭 상하 구도", state.cropOffsetYStep) {
                onIntent(TimestampEditorContract.Intent.CropOffsetYChanged(it))
            }

            HorizontalDivider(color = Color(0x14000000))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPickPhoto) { Text("사진 선택") }
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
private fun CroppedPreviewImage(
    previewImage: androidx.compose.ui.graphics.ImageBitmap,
    cropZoomPreset: TimestampCropZoomPreset,
    cropOffsetXStep: Int,
    cropOffsetYStep: Int,
) {
    Image(
        bitmap = previewImage,
        contentDescription = "선택한 사진 프리뷰",
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = cropZoomPreset.previewScale,
                scaleY = cropZoomPreset.previewScale,
                translationX = cropOffsetXStep * 18f,
                translationY = cropOffsetYStep * 18f,
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

@Composable
private fun NudgeControlRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label: $value",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = { onValueChange(value - 1) }) { Text("-") }
        Button(onClick = { onValueChange(0) }) { Text("0") }
        Button(onClick = { onValueChange(value + 1) }) { Text("+") }
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
private fun RoadmapCard(
    title: String,
    lines: List<String>,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

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
