package com.yujin.timestamp.app

import android.content.ContentValues
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color.parseColor
import android.graphics.Rect
import android.net.Uri
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.exifinterface.media.ExifInterface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yujin.timestamp.core.model.TimestampExportRequest
import com.yujin.timestamp.core.model.TimestampImagePayload
import com.yujin.timestamp.feature.editor.TimestampEditorRoute
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedImagePayload by remember { mutableStateOf<TimestampImagePayload?>(null) }
            var metadataTimestampLabel by remember { mutableStateOf<String?>(null) }
            var selectedTimestampLabel by remember { mutableStateOf<String?>(null) }
            var exportMessage by remember { mutableStateOf<String?>(null) }
            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
            ) { uri ->
                selectedImagePayload = uri?.readImagePayload()
                metadataTimestampLabel = selectedImagePayload?.metadataTimestampLabel
                selectedTimestampLabel = null
                exportMessage = null
            }

            TimestampEditorRoute(
                selectedImageBase64 = selectedImagePayload?.base64,
                metadataTimestampLabel = metadataTimestampLabel,
                selectedTimestampLabel = selectedTimestampLabel,
                exportMessage = exportMessage,
                onExportMessageConsumed = { exportMessage = null },
                onPickPhoto = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onEditTimestampRequest = { currentTimestamp ->
                    showTimestampPicker(currentTimestamp) { selectedTimestampLabel = it }
                },
                onExport = { request ->
                    exportMessage = exportTimestampedImage(request)
                },
            )
        }
    }

    private fun Uri.readImagePayload(): TimestampImagePayload? {
        val bytes = contentResolver
            .openInputStream(this)
            ?.use { inputStream -> inputStream.readBytes() }
            ?: return null

        val originalDate = runCatching {
            val exif = ExifInterface(bytes.inputStream())
            exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        }.getOrNull()

        return TimestampImagePayload(
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            metadataTimestampLabel = originalDate
            ?.let {
                runCatching {
                    LocalDateTime.parse(
                        it,
                        DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"),
                    ).format(DateTimeFormatter.ofPattern("MM.dd.yy  HH:mm"))
                }.getOrNull()
            },
        )
    }

    private fun exportTimestampedImage(request: TimestampExportRequest): String {
        val renderedBitmap = renderTimestampedBitmap(request)
            ?: return "이미지를 저장하지 못했습니다."

        val fileName = "timestamp-${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Timestamp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues,
        ) ?: return "저장소 항목을 만들지 못했습니다."

        return runCatching {
            resolver.openOutputStream(uri).useBitmap(renderedBitmap)
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            "사진을 갤러리의 Pictures/Timestamp 폴더에 저장했습니다."
        }.getOrElse {
            resolver.delete(uri, null, null)
            "이미지를 저장하지 못했습니다."
        }
    }

    private fun renderTimestampedBitmap(request: TimestampExportRequest): Bitmap? {
        val sourceBytes = runCatching {
            Base64.decode(request.imageBase64, Base64.DEFAULT)
        }.getOrNull() ?: return null

        val sourceBitmap = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size)
            ?: return null
        val cropPreset = AndroidCropPreset.from(
            aspectRatioKey = request.aspectRatioKey,
            cropScale = request.cropScale,
            offsetXRatio = request.cropOffsetXRatio,
            offsetYRatio = request.cropOffsetYRatio,
            sourceWidth = sourceBitmap.width,
            sourceHeight = sourceBitmap.height,
        )
        val mutableBitmap = Bitmap.createBitmap(cropPreset.outputWidth, cropPreset.outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        canvas.drawBitmap(
            sourceBitmap,
            cropPreset.sourceRect,
            Rect(0, 0, cropPreset.outputWidth, cropPreset.outputHeight),
            Paint(Paint.FILTER_BITMAP_FLAG),
        )

        val stylePreset = AndroidOverlayStylePreset.from(
            scaleKey = request.scaleKey,
            insetKey = request.insetKey,
            safeAreaKey = request.safeAreaKey,
        )
        val horizontalPadding = mutableBitmap.width * 0.04f
        val verticalPadding = mutableBitmap.height * (stylePreset.bottomInsetRatio + stylePreset.safeAreaExtraRatio)
        val timestampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(request.timestampColorHex)
            textSize = mutableBitmap.width * stylePreset.timestampTextRatio
            typeface = android.graphics.Typeface.MONOSPACE
            setShadowLayer(
                mutableBitmap.width * 0.012f,
                0f,
                mutableBitmap.width * 0.004f,
                parseColor(request.shadowColorHex),
            )
        }
        val locationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(request.locationColorHex)
            textSize = mutableBitmap.width * stylePreset.locationTextRatio
            typeface = android.graphics.Typeface.MONOSPACE
            setShadowLayer(
                mutableBitmap.width * 0.009f,
                0f,
                mutableBitmap.width * 0.003f,
                parseColor(request.shadowColorHex),
            )
        }

        val timestampWidth = timestampPaint.measureText(request.timestamp)
        val locationWidth = locationPaint.measureText(request.location)
        val contentWidth = maxOf(timestampWidth, locationWidth)
        val startX = when (request.alignmentKey) {
            "bottom_end" -> mutableBitmap.width - horizontalPadding - contentWidth
            else -> horizontalPadding
        }
        val adjustedStartX = startX + (mutableBitmap.width * 0.018f * request.offsetXStep)
        val locationBaseline = mutableBitmap.height - verticalPadding
        val adjustedLocationBaseline = locationBaseline - (mutableBitmap.height * 0.016f * request.offsetYStep)
        val timestampBaseline = adjustedLocationBaseline - locationPaint.textSize - (mutableBitmap.height * 0.02f)

        canvas.drawText(request.timestamp, adjustedStartX, timestampBaseline, timestampPaint)
        canvas.drawText(request.location, adjustedStartX, adjustedLocationBaseline, locationPaint)

        return mutableBitmap
    }

    private fun showTimestampPicker(
        currentTimestamp: String,
        onSelected: (String) -> Unit,
    ) {
        val initialDateTime = currentTimestamp.toTimestampDateTime()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        onSelected(
                            LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                                .format(TIMESTAMP_FORMATTER),
                        )
                    },
                    initialDateTime.hour,
                    initialDateTime.minute,
                    true,
                ).show()
            },
            initialDateTime.year,
            initialDateTime.monthValue - 1,
            initialDateTime.dayOfMonth,
        ).show()
    }

    private fun String.toTimestampDateTime(): LocalDateTime {
        return runCatching { LocalDateTime.parse(this, TIMESTAMP_FORMATTER) }
            .getOrElse { LocalDateTime.now() }
    }
}

private val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.yy  HH:mm")

private data class AndroidCropPreset(
    val sourceRect: Rect,
    val outputWidth: Int,
    val outputHeight: Int,
) {
    companion object {
        fun from(
            aspectRatioKey: String,
            cropScale: Float,
            offsetXRatio: Float,
            offsetYRatio: Float,
            sourceWidth: Int,
            sourceHeight: Int,
        ): AndroidCropPreset {
            val aspectRatio = when (aspectRatioKey) {
                "16_9" -> 16f / 9f
                else -> 4f / 3f
            }
            val zoom = cropScale.coerceIn(1f, 4f)

            val baseCropWidth: Float
            val baseCropHeight: Float
            if (sourceWidth.toFloat() / sourceHeight > aspectRatio) {
                baseCropHeight = sourceHeight.toFloat()
                baseCropWidth = baseCropHeight * aspectRatio
            } else {
                baseCropWidth = sourceWidth.toFloat()
                baseCropHeight = baseCropWidth / aspectRatio
            }

            val cropWidth = baseCropWidth / zoom
            val cropHeight = baseCropHeight / zoom
            val maxShiftX = ((sourceWidth - cropWidth) / 2f).coerceAtLeast(0f)
            val maxShiftY = ((sourceHeight - cropHeight) / 2f).coerceAtLeast(0f)
            val centerX = sourceWidth / 2f + offsetXRatio.coerceIn(-1f, 1f) * maxShiftX
            val centerY = sourceHeight / 2f + offsetYRatio.coerceIn(-1f, 1f) * maxShiftY
            val left = (centerX - cropWidth / 2f).coerceIn(0f, sourceWidth - cropWidth)
            val top = (centerY - cropHeight / 2f).coerceIn(0f, sourceHeight - cropHeight)

            return AndroidCropPreset(
                sourceRect = Rect(
                    left.toInt(),
                    top.toInt(),
                    (left + cropWidth).toInt(),
                    (top + cropHeight).toInt(),
                ),
                outputWidth = cropWidth.toInt(),
                outputHeight = cropHeight.toInt(),
            )
        }
    }
}

private fun OutputStream?.useBitmap(bitmap: Bitmap) {
    if (this == null) error("outputStream unavailable")
    use { stream ->
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream))
    }
}

private data class AndroidOverlayStylePreset(
    val timestampTextRatio: Float,
    val locationTextRatio: Float,
    val bottomInsetRatio: Float,
    val safeAreaExtraRatio: Float,
) {
    companion object {
        fun from(scaleKey: String, insetKey: String, safeAreaKey: String): AndroidOverlayStylePreset {
            val timestampRatio = when (scaleKey) {
                "small" -> 0.038f
                "large" -> 0.074f
                else -> 0.064f
            }
            val locationRatio = when (scaleKey) {
                "small" -> 0.017f
                "large" -> 0.034f
                else -> 0.029f
            }
            val insetRatio = when (insetKey) {
                "tight" -> 0.055f
                "spacious" -> 0.11f
                else -> 0.08f
            }
            val safeAreaRatio = when (safeAreaKey) {
                "strong" -> 0.05f
                "standard" -> 0.025f
                else -> 0f
            }
            return AndroidOverlayStylePreset(
                timestampTextRatio = timestampRatio,
                locationTextRatio = locationRatio,
                bottomInsetRatio = insetRatio,
                safeAreaExtraRatio = safeAreaRatio,
            )
        }
    }
}
