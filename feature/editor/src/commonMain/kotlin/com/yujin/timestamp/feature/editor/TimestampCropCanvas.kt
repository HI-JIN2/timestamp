package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun CropGestureSurface(
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
            CropFrame(
                cropLeftPx = cropLeftPx,
                cropTopPx = cropTopPx,
                cropWidthPx = cropWidthPx,
                imageWidthPx = imageWidthPx,
                cropAspectRatio = cropAspectRatio,
                cropScale = cropScale,
                cropOffsetXRatio = cropOffsetXRatio,
                cropOffsetYRatio = cropOffsetYRatio,
                imageWidthPxForGesture = imageWidthPx,
                imageHeightPxForGesture = imageHeightPx,
                maxShiftX = maxShiftX,
                maxShiftY = maxShiftY,
                palette = palette,
                currentCropWidthPx = cropWidthPx,
                maxCropWidthPx = maxCropWidthPx,
                onFrameDragged = onFrameDragged,
                onFrameScaled = onFrameScaled,
            )
        }
    }
}

@Composable
private fun CropFrame(
    cropLeftPx: Float,
    cropTopPx: Float,
    cropWidthPx: Float,
    imageWidthPx: Float,
    cropAspectRatio: Float,
    cropScale: Float,
    cropOffsetXRatio: Float,
    cropOffsetYRatio: Float,
    imageWidthPxForGesture: Float,
    imageHeightPxForGesture: Float,
    maxShiftX: Float,
    maxShiftY: Float,
    palette: EditorPalette,
    currentCropWidthPx: Float,
    maxCropWidthPx: Float,
    onFrameDragged: (Float, Float) -> Unit,
    onFrameScaled: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(cropLeftPx.roundToInt(), cropTopPx.roundToInt()) }
            .fillMaxWidth(fraction = cropWidthPx / imageWidthPx)
            .aspectRatio(cropAspectRatio)
            .border(1.dp, palette.cropGuide)
            .pointerInput(
                cropScale,
                cropOffsetXRatio,
                cropOffsetYRatio,
                imageWidthPxForGesture,
                imageHeightPxForGesture,
            ) {
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
            currentCropWidthPx = currentCropWidthPx,
            maxCropWidthPx = maxCropWidthPx,
            onScale = onFrameScaled,
        )
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
