package com.yujin.timestamp.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yujin.timestamp.shared.preview.TimestampPreviewPresenter

@Composable
fun TimestampApp(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    onPickPhoto: () -> Unit = {},
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
                    onTimestampChange = { editableTimestamp = it },
                    onResetTimestamp = {
                        editableTimestamp = previewState.timestampLabel
                    },
                    onPickPhoto = onPickPhoto,
                )

                RoadmapCard(
                    title = "다음 구현 순서",
                    lines = listOf(
                        "1. 선택한 이미지를 실제 프리뷰로 디코딩",
                        "2. 타임스탬프 스타일/위치 옵션 모델링",
                        "3. 실제 이미지 위 오버레이 렌더링",
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
    onTimestampChange: (String) -> Unit,
    onResetTimestamp: () -> Unit,
    onPickPhoto: () -> Unit,
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

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = timestamp,
                        color = Color(0xFFFFA347),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.4.sp,
                    )
                    Text(
                        text = location,
                        color = Color(0xFFF7E7C6),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Text(
                text = helper,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

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
                Button(onClick = {}, enabled = hasSelectedPhoto && timestamp.isNotBlank()) {
                    Text("내보내기")
                }
            }
        }
    }
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
