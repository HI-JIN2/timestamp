package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import org.jetbrains.compose.resources.stringResource
import timestamp.feature.editor.generated.resources.Res
import timestamp.feature.editor.generated.resources.crop_image_description
import kotlin.math.roundToInt

private enum class CropDragHandle {
    Move,
    Left,
    Right,
    Top,
    Bottom,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

private data class CropRectPx(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f
}

@Composable
internal fun CropGestureSurface(
    previewImage: ImageBitmap,
    aspectRatioPreset: TimestampAspectRatioPreset,
    cropLeftRatio: Float,
    cropTopRatio: Float,
    cropWidthRatio: Float,
    cropHeightRatio: Float,
    palette: EditorPalette,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
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

        val cropRect = CropRectPx(
            left = cropLeftRatio * imageWidthPx,
            top = cropTopRatio * imageHeightPx,
            width = cropWidthRatio * imageWidthPx,
            height = cropHeightRatio * imageHeightPx,
        )

        Box(
            modifier = imageModifier.clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = previewImage,
                contentDescription = stringResource(Res.string.crop_image_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            CropMaskOverlay(
                cropLeftPx = cropRect.left,
                cropTopPx = cropRect.top,
                cropWidthPx = cropRect.width,
                cropHeightPx = cropRect.height,
                palette = palette,
            )
            CropFrame(
                cropRect = cropRect,
                imageWidthPx = imageWidthPx,
                imageHeightPx = imageHeightPx,
                cropAspectRatio = aspectRatioPreset.ratio,
                palette = palette,
                onCropRectChanged = onCropRectChanged,
            )
        }
    }
}

@Composable
private fun CropFrame(
    cropRect: CropRectPx,
    imageWidthPx: Float,
    imageHeightPx: Float,
    cropAspectRatio: Float,
    palette: EditorPalette,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(cropRect.left.roundToInt(), cropRect.top.roundToInt()) }
            .fillMaxWidth(fraction = cropRect.width / imageWidthPx)
            .aspectRatio(cropAspectRatio)
            .border(1.dp, palette.cropGuide)
            .pointerInput(cropRect, imageWidthPx, imageHeightPx, cropAspectRatio) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    updateCropRect(
                        handle = CropDragHandle.Move,
                        current = cropRect,
                        dragX = dragAmount.x,
                        dragY = dragAmount.y,
                        imageWidthPx = imageWidthPx,
                        imageHeightPx = imageHeightPx,
                        aspectRatio = cropAspectRatio,
                        onCropRectChanged = onCropRectChanged,
                    )
                }
            },
    ) {
        CropGridOverlay(palette)
        CropCornerHandles(
            palette = palette,
            cropRect = cropRect,
            imageWidthPx = imageWidthPx,
            imageHeightPx = imageHeightPx,
            aspectRatio = cropAspectRatio,
            onCropRectChanged = onCropRectChanged,
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
    cropRect: CropRectPx,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
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

    CropEdgeHandle(
        modifier = Modifier.align(Alignment.CenterStart),
        handle = CropDragHandle.Left,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropEdgeHandle(
        modifier = Modifier.align(Alignment.CenterEnd),
        handle = CropDragHandle.Right,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropEdgeHandle(
        modifier = Modifier.align(Alignment.TopCenter),
        handle = CropDragHandle.Top,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropEdgeHandle(
        modifier = Modifier.align(Alignment.BottomCenter),
        handle = CropDragHandle.Bottom,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.TopStart),
        handle = CropDragHandle.TopLeft,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.TopEnd),
        handle = CropDragHandle.TopRight,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.BottomStart),
        handle = CropDragHandle.BottomLeft,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
    CropResizeHandle(
        modifier = Modifier.align(Alignment.BottomEnd),
        handle = CropDragHandle.BottomRight,
        cropRect = cropRect,
        imageWidthPx = imageWidthPx,
        imageHeightPx = imageHeightPx,
        aspectRatio = aspectRatio,
        onCropRectChanged = onCropRectChanged,
    )
}

@Composable
private fun BoxScope.CropEdgeHandle(
    modifier: Modifier,
    handle: CropDragHandle,
    cropRect: CropRectPx,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .then(
                if (handle == CropDragHandle.Left || handle == CropDragHandle.Right) {
                    Modifier.fillMaxHeight().size(width = 28.dp, height = 1.dp)
                } else {
                    Modifier.fillMaxWidth().size(width = 1.dp, height = 28.dp)
                },
            )
            .pointerInput(cropRect, imageWidthPx, imageHeightPx, aspectRatio) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    updateCropRect(
                        handle = handle,
                        current = cropRect,
                        dragX = dragAmount.x,
                        dragY = dragAmount.y,
                        imageWidthPx = imageWidthPx,
                        imageHeightPx = imageHeightPx,
                        aspectRatio = aspectRatio,
                        onCropRectChanged = onCropRectChanged,
                    )
                }
            },
    )
}

@Composable
private fun BoxScope.CropResizeHandle(
    modifier: Modifier,
    handle: CropDragHandle,
    cropRect: CropRectPx,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .pointerInput(cropRect, imageWidthPx, imageHeightPx, aspectRatio) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    updateCropRect(
                        handle = handle,
                        current = cropRect,
                        dragX = dragAmount.x,
                        dragY = dragAmount.y,
                        imageWidthPx = imageWidthPx,
                        imageHeightPx = imageHeightPx,
                        aspectRatio = aspectRatio,
                        onCropRectChanged = onCropRectChanged,
                    )
                }
            },
    )
}

private fun updateCropRect(
    handle: CropDragHandle,
    current: CropRectPx,
    dragX: Float,
    dragY: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
    onCropRectChanged: (Float, Float, Float, Float) -> Unit,
) {
    val updated = when (handle) {
        CropDragHandle.Move -> moveCropRect(current, dragX, dragY, imageWidthPx, imageHeightPx)
        CropDragHandle.Left -> resizeFromLeft(current, dragX, imageWidthPx, imageHeightPx, aspectRatio)
        CropDragHandle.Right -> resizeFromRight(current, dragX, imageWidthPx, imageHeightPx, aspectRatio)
        CropDragHandle.Top -> resizeFromTop(current, dragY, imageWidthPx, imageHeightPx, aspectRatio)
        CropDragHandle.Bottom -> resizeFromBottom(current, dragY, imageWidthPx, imageHeightPx, aspectRatio)
        CropDragHandle.TopLeft -> resizeFromCorner(current, dragX, dragY, imageWidthPx, imageHeightPx, aspectRatio, true, true)
        CropDragHandle.TopRight -> resizeFromCorner(current, dragX, dragY, imageWidthPx, imageHeightPx, aspectRatio, false, true)
        CropDragHandle.BottomLeft -> resizeFromCorner(current, dragX, dragY, imageWidthPx, imageHeightPx, aspectRatio, true, false)
        CropDragHandle.BottomRight -> resizeFromCorner(current, dragX, dragY, imageWidthPx, imageHeightPx, aspectRatio, false, false)
    }

    onCropRectChanged(
        updated.left / imageWidthPx,
        updated.top / imageHeightPx,
        updated.width / imageWidthPx,
        updated.height / imageHeightPx,
    )
}

private fun moveCropRect(
    current: CropRectPx,
    dragX: Float,
    dragY: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
): CropRectPx {
    val left = (current.left + dragX).coerceIn(0f, imageWidthPx - current.width)
    val top = (current.top + dragY).coerceIn(0f, imageHeightPx - current.height)
    return current.copy(left = left, top = top)
}

private fun resizeFromLeft(
    current: CropRectPx,
    dragX: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
): CropRectPx {
    val right = current.right
    val requestedWidth = (right - (current.left + dragX)).coerceAtLeast(1f)
    val maxWidthByBounds = minOf(right, imageHeightPx * aspectRatio)
    val width = requestedWidth.coerceIn(imageWidthPx * 0.12f, maxWidthByBounds)
    val height = width / aspectRatio
    val left = right - width
    val top = (current.centerY - height / 2f).coerceIn(0f, imageHeightPx - height)
    return CropRectPx(left = left, top = top, width = width, height = height)
}

private fun resizeFromRight(
    current: CropRectPx,
    dragX: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
): CropRectPx {
    val left = current.left
    val requestedWidth = (current.width + dragX).coerceAtLeast(1f)
    val maxWidthByBounds = minOf(imageWidthPx - left, imageHeightPx * aspectRatio)
    val width = requestedWidth.coerceIn(imageWidthPx * 0.12f, maxWidthByBounds)
    val height = width / aspectRatio
    val top = (current.centerY - height / 2f).coerceIn(0f, imageHeightPx - height)
    return CropRectPx(left = left, top = top, width = width, height = height)
}

private fun resizeFromTop(
    current: CropRectPx,
    dragY: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
): CropRectPx {
    val bottom = current.bottom
    val requestedHeight = (bottom - (current.top + dragY)).coerceAtLeast(1f)
    val maxHeightByBounds = minOf(bottom, imageWidthPx / aspectRatio)
    val height = requestedHeight.coerceIn(imageHeightPx * 0.12f, maxHeightByBounds)
    val width = height * aspectRatio
    val left = (current.centerX - width / 2f).coerceIn(0f, imageWidthPx - width)
    val top = bottom - height
    return CropRectPx(left = left, top = top, width = width, height = height)
}

private fun resizeFromBottom(
    current: CropRectPx,
    dragY: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
): CropRectPx {
    val top = current.top
    val requestedHeight = (current.height + dragY).coerceAtLeast(1f)
    val maxHeightByBounds = minOf(imageHeightPx - top, imageWidthPx / aspectRatio)
    val height = requestedHeight.coerceIn(imageHeightPx * 0.12f, maxHeightByBounds)
    val width = height * aspectRatio
    val left = (current.centerX - width / 2f).coerceIn(0f, imageWidthPx - width)
    return CropRectPx(left = left, top = top, width = width, height = height)
}

private fun resizeFromCorner(
    current: CropRectPx,
    dragX: Float,
    dragY: Float,
    imageWidthPx: Float,
    imageHeightPx: Float,
    aspectRatio: Float,
    anchoredRight: Boolean,
    anchoredBottom: Boolean,
): CropRectPx {
    val widthDelta = when {
        anchoredRight && anchoredBottom -> -(dragX + dragY * aspectRatio) / 2f
        anchoredRight -> -(dragX - dragY * aspectRatio) / 2f
        anchoredBottom -> (dragX - dragY * aspectRatio) / 2f
        else -> (dragX + dragY * aspectRatio) / 2f
    }
    val requestedWidth = (current.width + widthDelta).coerceAtLeast(1f)
    val maxWidthByHorizontal = if (anchoredRight) current.right else imageWidthPx - current.left
    val maxHeightByVertical = if (anchoredBottom) current.bottom else imageHeightPx - current.top
    val maxWidth = minOf(maxWidthByHorizontal, maxHeightByVertical * aspectRatio)
    val width = requestedWidth.coerceIn(imageWidthPx * 0.12f, maxWidth)
    val height = width / aspectRatio
    val left = if (anchoredRight) current.right - width else current.left
    val top = if (anchoredBottom) current.bottom - height else current.top
    return CropRectPx(left = left, top = top, width = width, height = height)
}
