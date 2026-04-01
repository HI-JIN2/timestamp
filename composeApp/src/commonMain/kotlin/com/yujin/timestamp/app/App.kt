package com.yujin.timestamp.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yujin.timestamp.shared.preview.TimestampPreviewPresenter

@Composable
fun TimestampApp(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
) {
    val previewImage = remember(selectedImageBase64) {
        selectedImageBase64?.let(::decodeSelectedImage)
    }
    val hasSelectedPhoto = previewImage != null || selectedImageBase64 != null
    val previewState = TimestampPreviewPresenter.preview(
        hasSelectedPhoto = hasSelectedPhoto,
        metadataTimestampLabel = metadataTimestampLabel,
    )
    var editableTimestamp by remember(selectedImageBase64, metadataTimestampLabel) {
        mutableStateOf(previewState.timestampLabel)
    }
    var overlayTone by remember { mutableStateOf(TimestampOverlayTone.ClassicAmber) }
    var overlayAlignment by remember { mutableStateOf(TimestampOverlayAlignment.BottomStart) }
    var overlayScale by remember { mutableStateOf(TimestampOverlayScale.Medium) }
    var overlayInset by remember { mutableStateOf(TimestampOverlayInset.Balanced) }

    MaterialTheme(
        colorScheme = retroColorScheme(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    text = "레트로 사진 인화 감성의 타임스탬프를 Android와 iOS에서 동일하게 다루는 Compose Multiplatform 앱의 시작점입니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                PreviewCard(
                    timestamp = editableTimestamp,
                    metadataDescription = previewState.metadataDescription,
                    location = previewState.locationLabel,
                    helper = previewState.helperText,
                    hasSelectedPhoto = hasSelectedPhoto,
                    previewImage = previewImage,
                    overlayTone = overlayTone,
                    overlayAlignment = overlayAlignment,
                    overlayScale = overlayScale,
                    overlayInset = overlayInset,
                    onTimestampChange = { editableTimestamp = it },
                    onResetTimestamp = {
                        editableTimestamp = previewState.timestampLabel
                    },
                    onToneChange = { overlayTone = it },
                    onAlignmentChange = { overlayAlignment = it },
                    onScaleChange = { overlayScale = it },
                    onInsetChange = { overlayInset = it },
                    exportMessage = exportMessage,
                    onPickPhoto = onPickPhoto,
                    onExport = {
                        val imageBase64 = selectedImageBase64 ?: return@PreviewCard
                        onExport(
                            TimestampExportRequest(
                                imageBase64 = imageBase64,
                                timestamp = editableTimestamp,
                                location = previewState.locationLabel,
                                timestampColorHex = overlayTone.timestampColorHex,
                                locationColorHex = overlayTone.locationColorHex,
                                shadowColorHex = overlayTone.shadowColorHex,
                                alignmentKey = overlayAlignment.exportKey,
                                scaleKey = overlayScale.exportKey,
                                insetKey = overlayInset.exportKey,
                            ),
                        )
                    },
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
    timestamp: String,
    metadataDescription: String,
    location: String,
    helper: String,
    hasSelectedPhoto: Boolean,
    previewImage: ImageBitmap?,
    overlayTone: TimestampOverlayTone,
    overlayAlignment: TimestampOverlayAlignment,
    overlayScale: TimestampOverlayScale,
    overlayInset: TimestampOverlayInset,
    onTimestampChange: (String) -> Unit,
    onResetTimestamp: () -> Unit,
    onToneChange: (TimestampOverlayTone) -> Unit,
    onAlignmentChange: (TimestampOverlayAlignment) -> Unit,
    onScaleChange: (TimestampOverlayScale) -> Unit,
    onInsetChange: (TimestampOverlayInset) -> Unit,
    exportMessage: String?,
    onPickPhoto: () -> Unit,
    onExport: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "프리뷰 구조",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = if (hasSelectedPhoto) "사진 선택 완료" else "사진 미선택",
                style = MaterialTheme.typography.labelLarge,
                color = if (hasSelectedPhoto) {
                    Color(0xFF2F6A3D)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE6D8BE),
                                Color(0xFFC7B08A),
                                Color(0xFF6C5A45),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0x1A000000),
                        shape = RoundedCornerShape(22.dp),
                    ),
            ) {
                if (previewImage != null) {
                    Image(
                        bitmap = previewImage,
                        contentDescription = "선택한 사진 프리뷰",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else if (!hasSelectedPhoto) {
                    Text(
                        text = "선택한 사진이 여기에 표시됩니다",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4F4131),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                TimestampOverlay(
                    timestamp = timestamp,
                    location = location,
                    tone = overlayTone,
                    alignment = overlayAlignment,
                    scale = overlayScale,
                    inset = overlayInset,
                )
            }

            Text(
                text = helper,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (exportMessage != null) {
                Text(
                    text = exportMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedTextField(
                value = timestamp,
                onValueChange = onTimestampChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("타임스탬프")
                },
                supportingText = {
                    Text(metadataDescription)
                },
                singleLine = true,
            )

            OverlayControlRow(
                label = "오버레이 톤",
                options = TimestampOverlayTone.entries,
                selected = overlayTone,
                optionLabel = { it.label },
                onSelected = onToneChange,
            )

            OverlayControlRow(
                label = "오버레이 위치",
                options = TimestampOverlayAlignment.entries,
                selected = overlayAlignment,
                optionLabel = { it.label },
                onSelected = onAlignmentChange,
            )

            OverlayControlRow(
                label = "글자 크기",
                options = TimestampOverlayScale.entries,
                selected = overlayScale,
                optionLabel = { it.label },
                onSelected = onScaleChange,
            )

            OverlayControlRow(
                label = "하단 여백",
                options = TimestampOverlayInset.entries,
                selected = overlayInset,
                optionLabel = { it.label },
                onSelected = onInsetChange,
            )

            HorizontalDivider(color = Color(0x14000000))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onPickPhoto) {
                    Text("사진 선택")
                }
                Button(
                    onClick = onResetTimestamp,
                    enabled = hasSelectedPhoto,
                ) {
                    Text("기본값 복원")
                }
                Button(
                    onClick = onExport,
                    enabled = hasSelectedPhoto && timestamp.isNotBlank(),
                ) {
                    Text("내보내기")
                }
            }
        }
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
) {
    Column(
        modifier = Modifier
            .align(alignment.containerAlignment)
            .padding(inset.previewPaddingDp.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = if (alignment == TimestampOverlayAlignment.BottomEnd) {
            Alignment.End
        } else {
            Alignment.Start
        },
    ) {
        Text(
            text = timestamp,
            color = tone.timestampColor,
            fontSize = scale.timestampFontSp.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp,
            textAlign = if (alignment == TimestampOverlayAlignment.BottomEnd) {
                TextAlign.End
            } else {
                TextAlign.Start
            },
            style = overlayTextStyle(
                shadowColor = tone.shadowColor,
                blurRadius = 8f,
                yOffset = 3f,
            ),
        )
        Text(
            text = location,
            color = tone.locationColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = scale.locationFontSp.sp,
            ).merge(
                overlayTextStyle(
                    shadowColor = tone.shadowColor,
                    blurRadius = 6f,
                    yOffset = 2f,
                ),
            ),
            fontFamily = FontFamily.Monospace,
        )
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
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
): TextStyle {
    return TextStyle(
        shadow = Shadow(
            color = shadowColor,
            offset = Offset(0f, yOffset),
            blurRadius = blurRadius,
        ),
    )
}

@Composable
private fun RoadmapCard(
    title: String,
    lines: List<String>,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
