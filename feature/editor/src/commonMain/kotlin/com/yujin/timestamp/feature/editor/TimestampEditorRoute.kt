package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.domain.editor.GetTimestampPreviewStateUseCase
import kotlin.math.roundToInt

@Composable
fun TimestampEditorRoute(
    selectedImageBase64: String? = null,
    metadataTimestampLabel: String? = null,
    selectedTimestampLabel: String? = null,
    exportMessage: String? = null,
    onPickPhoto: () -> Unit = {},
    onEditTimestampRequest: (String) -> Unit = {},
    onExport: (TimestampExportRequest) -> Unit = {},
    onExportMessageConsumed: () -> Unit = {},
) {
    val previewImage = remember(selectedImageBase64) {
        selectedImageBase64?.let(::decodeSelectedImage)
    }
    val viewModel = remember {
        TimestampEditorViewModel(GetTimestampPreviewStateUseCase())
    }

    LaunchedEffect(selectedImageBase64, metadataTimestampLabel, selectedTimestampLabel, exportMessage, previewImage) {
        viewModel.dispatch(
            TimestampEditorContract.Intent.SyncExternal(
                selectedImageBase64 = selectedImageBase64,
                previewImage = previewImage,
                metadataTimestampLabel = metadataTimestampLabel,
                selectedTimestampLabel = selectedTimestampLabel,
                exportMessage = exportMessage,
            ),
        )
    }

    val state by remember { derivedStateOf { viewModel.state } }

    TimestampEditorScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onPickPhoto = onPickPhoto,
        onEditTimestampRequest = onEditTimestampRequest,
        onExport = { viewModel.buildExportRequest()?.let(onExport) },
        onExportMessageConsumed = onExportMessageConsumed,
    )
}

@Composable
private fun TimestampEditorScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
    onEditTimestampRequest: (String) -> Unit,
    onExport: () -> Unit,
    onExportMessageConsumed: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkTheme = isSystemInDarkTheme()
    val editorPalette = rememberEditorPalette(isDarkTheme)

    LaunchedEffect(state.exportMessage) {
        val message = state.exportMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
        )
        onExportMessageConsumed()
    }

    MaterialTheme(colorScheme = timestampColorScheme(isDarkTheme)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    TimestampSnackbar(data)
                }
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background,
            ) {
                if (state.isCropEditorVisible && state.previewImage != null) {
                    CropEditorScreen(
                        state = state,
                        onIntent = onIntent,
                        palette = editorPalette,
                    )
                } else {
                    EditorHomeScreen(
                        state = state,
                        onIntent = onIntent,
                        onPickPhoto = onPickPhoto,
                        onEditTimestampRequest = onEditTimestampRequest,
                        onExport = onExport,
                        palette = editorPalette,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimestampSnackbar(data: SnackbarData) {
    Snackbar(
        snackbarData = data,
        containerColor = Color.Black,
        contentColor = Color.White,
        actionColor = Color.White,
        dismissActionContentColor = Color.White,
    )
}

@Composable
private fun EditorHomeScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
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
                letterSpacing = (-0.5).sp,
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onPickPhoto) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = if (state.hasSelectedPhoto) "사진 다시 선택" else "사진 선택",
                )
            }
            IconButton(
                onClick = { onIntent(TimestampEditorContract.Intent.OpenCropEditor) },
                enabled = state.hasSelectedPhoto,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Crop,
                    contentDescription = "크롭 편집",
                )
            }
        }
        PreviewCard(
            state = state,
            onIntent = onIntent,
            onEditTimestampRequest = onEditTimestampRequest,
            onExport = onExport,
            palette = palette,
        )
    }
}

@Composable
private fun CropEditorScreen(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    palette: EditorPalette,
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
                    onIntent(TimestampEditorContract.Intent.CropFrameDragged(deltaXRatio, deltaYRatio))
                },
                onFrameScaled = { scaleDelta ->
                    onIntent(TimestampEditorContract.Intent.CropFrameScaled(scaleDelta))
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
    palette: EditorPalette,
    onFrameDragged: (Float, Float) -> Unit,
    onFrameScaled: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val viewportWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val viewportHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val imageAspectRatio = previewImage.width.toFloat() / previewImage.height.toFloat()
        val viewportAspectRatio = viewportWidthPx / viewportHeightPx

        val imageWidthPx: Float
        val imageHeightPx: Float
        val imageModifier: Modifier
        if (imageAspectRatio > viewportAspectRatio) {
            imageWidthPx = viewportWidthPx
            imageHeightPx = imageWidthPx / imageAspectRatio
            imageModifier = Modifier.fillMaxWidth().aspectRatio(imageAspectRatio)
        } else {
            imageHeightPx = viewportHeightPx
            imageWidthPx = imageHeightPx * imageAspectRatio
            imageModifier = Modifier.fillMaxSize().aspectRatio(imageAspectRatio)
        }

        val cropAspectRatio = aspectRatioPreset.ratio
        val maxCropWidthPx: Float
        val maxCropHeightPx: Float
        if (imageWidthPx / imageHeightPx > cropAspectRatio) {
            maxCropHeightPx = imageHeightPx
            maxCropWidthPx = maxCropHeightPx * cropAspectRatio
        } else {
            maxCropWidthPx = imageWidthPx
            maxCropHeightPx = maxCropWidthPx / cropAspectRatio
        }

        val cropWidthPx = (maxCropWidthPx / cropScale).coerceAtLeast(maxCropWidthPx * 0.25f)
        val cropHeightPx = (maxCropHeightPx / cropScale).coerceAtLeast(maxCropHeightPx * 0.25f)
        val maxShiftX = ((imageWidthPx - cropWidthPx) / 2f).coerceAtLeast(0f)
        val maxShiftY = ((imageHeightPx - cropHeightPx) / 2f).coerceAtLeast(0f)
        val cropCenterX = imageWidthPx / 2f + cropOffsetXRatio.coerceIn(-1f, 1f) * maxShiftX
        val cropCenterY = imageHeightPx / 2f + cropOffsetYRatio.coerceIn(-1f, 1f) * maxShiftY
        val cropLeftPx = (cropCenterX - cropWidthPx / 2f).coerceIn(0f, imageWidthPx - cropWidthPx)
        val cropTopPx = (cropCenterY - cropHeightPx / 2f).coerceIn(0f, imageHeightPx - cropHeightPx)

        Box(
            modifier = imageModifier.clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = previewImage,
                contentDescription = "크롭 편집 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            CropMaskOverlay(
                cropLeftPx = cropLeftPx,
                cropTopPx = cropTopPx,
                cropWidthPx = cropWidthPx,
                cropHeightPx = cropHeightPx,
                palette = palette,
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(cropLeftPx.roundToInt(), cropTopPx.roundToInt()) }
                    .fillMaxWidth(fraction = cropWidthPx / imageWidthPx)
                    .aspectRatio(cropAspectRatio)
                    .border(1.dp, palette.cropGuide)
                    .pointerInput(cropScale, cropOffsetXRatio, cropOffsetYRatio, imageWidthPx, imageHeightPx) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onFrameDragged(
                                if (maxShiftX > 0f) dragAmount.x / maxShiftX else 0f,
                                if (maxShiftY > 0f) dragAmount.y / maxShiftY else 0f,
                            )
                        }
                    },
            ) {
                CropGridOverlay(palette)
                CropCornerHandles(
                    palette = palette,
                    currentCropWidthPx = cropWidthPx,
                    maxCropWidthPx = maxCropWidthPx,
                    onScale = onFrameScaled,
                )
            }
        }
    }
}

@Composable
private fun CropMaskOverlay(
    cropLeftPx: Float,
    cropTopPx: Float,
    cropWidthPx: Float,
    cropHeightPx: Float,
    palette: EditorPalette,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = palette.cropShade)
        clipRect(
            left = cropLeftPx,
            top = cropTopPx,
            right = cropLeftPx + cropWidthPx,
            bottom = cropTopPx + cropHeightPx,
            clipOp = ClipOp.Difference,
        ) {
            drawRect(color = palette.cropShade)
        }
    }
}

@Composable
private fun CropGridOverlay(palette: EditorPalette) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val thirdWidth = size.width / 3f
        val thirdHeight = size.height / 3f
        val stroke = size.minDimension * 0.0025f

        repeat(2) { index ->
            val x = thirdWidth * (index + 1)
            drawLine(
                color = palette.cropGrid,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = stroke,
            )
        }

        repeat(2) { index ->
            val y = thirdHeight * (index + 1)
            drawLine(
                color = palette.cropGrid,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
            )
        }
    }
}

@Composable
private fun BoxScope.CropCornerHandles(
    palette: EditorPalette,
    currentCropWidthPx: Float,
    maxCropWidthPx: Float,
    onScale: (Float) -> Unit,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val handleLength = size.minDimension * 0.055f
        val stroke = size.minDimension * 0.008f

        fun drawCorner(originX: Float, originY: Float, horizontalSign: Float, verticalSign: Float) {
            drawLine(
                color = palette.cropFrame,
                start = Offset(originX, originY),
                end = Offset(originX + handleLength * horizontalSign, originY),
                strokeWidth = stroke,
            )
            drawLine(
                color = palette.cropFrame,
                start = Offset(originX, originY),
                end = Offset(originX, originY + handleLength * verticalSign),
                strokeWidth = stroke,
            )
        }

        drawCorner(0f, 0f, 1f, 1f)
        drawCorner(size.width, 0f, -1f, 1f)
        drawCorner(0f, size.height, 1f, -1f)
        drawCorner(size.width, size.height, -1f, -1f)
    }

    CropResizeHandle(
        modifier = Modifier.align(Alignment.TopStart),
        horizontalSign = -1f,
        verticalSign = -1f,
        currentCropWidthPx = currentCropWidthPx,
        maxCropWidthPx = maxCropWidthPx,
        onScale = onScale,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.TopEnd),
        horizontalSign = 1f,
        verticalSign = -1f,
        currentCropWidthPx = currentCropWidthPx,
        maxCropWidthPx = maxCropWidthPx,
        onScale = onScale,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.BottomStart),
        horizontalSign = -1f,
        verticalSign = 1f,
        currentCropWidthPx = currentCropWidthPx,
        maxCropWidthPx = maxCropWidthPx,
        onScale = onScale,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.BottomEnd),
        horizontalSign = 1f,
        verticalSign = 1f,
        currentCropWidthPx = currentCropWidthPx,
        maxCropWidthPx = maxCropWidthPx,
        onScale = onScale,
    )
}

@Composable
private fun BoxScope.CropResizeHandle(
    modifier: Modifier,
    horizontalSign: Float,
    verticalSign: Float,
    currentCropWidthPx: Float,
    maxCropWidthPx: Float,
    onScale: (Float) -> Unit,
) {
    Box(
        modifier = modifier
            .padding(14.dp)
            .pointerInput(currentCropWidthPx, maxCropWidthPx) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val widthDelta = (horizontalSign * dragAmount.x + verticalSign * dragAmount.y) / 2f
                    val targetWidth = (currentCropWidthPx + widthDelta).coerceIn(
                        maxCropWidthPx * 0.25f,
                        maxCropWidthPx,
                    )
                    onScale(currentCropWidthPx / targetWidth)
                }
            },
    )
}

@Composable
private fun PreviewCard(
    state: TimestampEditorContract.State,
    onIntent: (TimestampEditorContract.Intent) -> Unit,
    onEditTimestampRequest: (String) -> Unit,
    onExport: () -> Unit,
    palette: EditorPalette,
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
                        cropScale = state.cropScale,
                        cropOffsetXRatio = state.cropOffsetXRatio,
                        cropOffsetYRatio = state.cropOffsetYRatio,
                    )
                } else if (!state.hasSelectedPhoto) {
                    Text(
                        text = "선택한 사진이 여기에 표시됩니다",
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

            Button(
                onClick = { onEditTimestampRequest(state.timestamp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.hasSelectedPhoto,
            ) {
                Text(state.timestamp)
            }

            OverlayControlRow("오버레이 톤", TimestampOverlayTone.entries, state.overlayTone, { it.label }) {
                onIntent(TimestampEditorContract.Intent.ToneChanged(it))
            }

            HorizontalDivider(color = palette.divider)
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

private data class EditorPalette(
    val cropBackground: Color,
    val cropGuide: Color,
    val cropFrame: Color,
    val cropGrid: Color,
    val cropShade: Color,
    val previewGradient: List<Color>,
    val previewBorder: Color,
    val placeholderText: Color,
    val divider: Color,
)

@Composable
private fun rememberEditorPalette(isDarkTheme: Boolean): EditorPalette {
    return remember(isDarkTheme) {
        if (isDarkTheme) {
            EditorPalette(
                cropBackground = Color(0xFF050505),
                cropGuide = Color.White.copy(alpha = 0.22f),
                cropFrame = Color.White.copy(alpha = 0.9f),
                cropGrid = Color.White.copy(alpha = 0.45f),
                cropShade = Color.Black.copy(alpha = 0.42f),
                previewGradient = listOf(Color(0xFF2A2A2A), Color(0xFF171717), Color(0xFF080808)),
                previewBorder = Color.White.copy(alpha = 0.12f),
                placeholderText = Color(0xFFD8D8D8),
                divider = Color.White.copy(alpha = 0.08f),
            )
        } else {
            EditorPalette(
                cropBackground = Color(0xFFF2F2F2),
                cropGuide = Color.Black.copy(alpha = 0.18f),
                cropFrame = Color.Black.copy(alpha = 0.92f),
                cropGrid = Color.Black.copy(alpha = 0.26f),
                cropShade = Color.Black.copy(alpha = 0.18f),
                previewGradient = listOf(Color(0xFFF6F6F6), Color(0xFFD9D9D9), Color(0xFFB8B8B8)),
                previewBorder = Color.Black.copy(alpha = 0.12f),
                placeholderText = Color(0xFF4A4A4A),
                divider = Color.Black.copy(alpha = 0.08f),
            )
        }
    }
}

private fun timestampColorScheme(isDarkTheme: Boolean) = if (isDarkTheme) {
    darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        secondary = Color(0xFFD0D0D0),
        onSecondary = Color.Black,
        background = Color(0xFF000000),
        onBackground = Color(0xFFF5F5F5),
        surface = Color(0xFF0E0E0E),
        onSurface = Color(0xFFF5F5F5),
        surfaceVariant = Color(0xFF181818),
        onSurfaceVariant = Color(0xFFB8B8B8),
        outline = Color(0xFF3A3A3A),
    )
} else {
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        secondary = Color(0xFF3A3A3A),
        onSecondary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFF7F7F7),
        onSurface = Color(0xFF111111),
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = Color(0xFF5A5A5A),
        outline = Color(0xFFBDBDBD),
    )
}
