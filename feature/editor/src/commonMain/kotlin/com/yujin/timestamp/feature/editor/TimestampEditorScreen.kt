package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun TimestampEditorScreen(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
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
                    CropEditorSection(
                        state = state,
                        onIntent = onIntent,
                        palette = editorPalette,
                    )
                } else {
                    EditorHomeSection(
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
private fun EditorHomeSection(
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPickPhoto,
                shape = RectangleShape,
                colors = retroActionButtonColors(),
                contentPadding = ButtonDefaults.ContentPadding,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = if (state.hasSelectedPhoto) "사진 다시 선택" else "사진 선택",
                )
            }
            Button(
                onClick = { onIntent(TimestampEditorUiContract.Intent.OpenCropEditor) },
                enabled = state.hasSelectedPhoto,
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
private fun CropEditorSection(
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
                Button(
                    onClick = { onIntent(TimestampEditorUiContract.Intent.ResetCrop) },
                    shape = RectangleShape,
                    colors = retroActionButtonColors(),
                ) {
                    Text("초기화")
                }
                Button(
                    onClick = { onIntent(TimestampEditorUiContract.Intent.CloseCropEditor) },
                    shape = RectangleShape,
                    colors = retroActionButtonColors(),
                ) {
                    Text("완료")
                }
            }
        }

        OverlayControlRow(
            label = "크롭 비율",
            options = TimestampAspectRatioPreset.entries,
            selected = state.aspectRatioPreset,
            optionLabel = { it.label },
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
private fun PreviewPanel(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
    onEditTimestampRequest: (String) -> Unit,
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
                shape = RectangleShape,
                colors = retroActionButtonColors(),
            ) {
                Text(state.timestamp)
            }

            OverlayControlRow("오버레이 톤", TimestampOverlayTone.entries, state.overlayTone, { it.label }) {
                onIntent(TimestampEditorUiContract.Intent.ToneChanged(it))
            }

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
            Text("기본값 복원")
        }
        Button(
            onClick = onExport,
            enabled = isExportEnabled,
            shape = RectangleShape,
            colors = retroActionButtonColors(),
        ) {
            Text("내보내기")
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
                    shape = RectangleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    label = { Text(optionLabel(option)) },
                )
            }
        }
    }
}
