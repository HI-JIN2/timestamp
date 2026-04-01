package com.yujin.timestamp.app

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color.parseColor
import android.net.Uri
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedImagePayload by remember { mutableStateOf<TimestampImagePayload?>(null) }
            var metadataTimestampLabel by remember { mutableStateOf<String?>(null) }
            var exportMessage by remember { mutableStateOf<String?>(null) }
            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
            ) { uri ->
                selectedImagePayload = uri?.readImagePayload()
                metadataTimestampLabel = selectedImagePayload?.metadataTimestampLabel
                exportMessage = null
            }

            TimestampApp(
                selectedImageBase64 = selectedImagePayload?.base64,
                metadataTimestampLabel = metadataTimestampLabel,
                exportMessage = exportMessage,
                onPickPhoto = { imagePicker.launch("image/*") },
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
        val mutableBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val density = resources.displayMetrics.density
        val horizontalPadding = 18f * density
        val verticalPadding = 18f * density
        val timestampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(request.timestampColorHex)
            textSize = 28f * density
            typeface = android.graphics.Typeface.MONOSPACE
            setShadowLayer(8f * density, 0f, 3f * density, parseColor(request.shadowColorHex))
        }
        val locationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(request.locationColorHex)
            textSize = 14f * density
            typeface = android.graphics.Typeface.MONOSPACE
            setShadowLayer(6f * density, 0f, 2f * density, parseColor(request.shadowColorHex))
        }

        val timestampWidth = timestampPaint.measureText(request.timestamp)
        val locationWidth = locationPaint.measureText(request.location)
        val contentWidth = maxOf(timestampWidth, locationWidth)
        val startX = when (request.alignmentKey) {
            "bottom_end" -> mutableBitmap.width - horizontalPadding - contentWidth
            else -> horizontalPadding
        }
        val locationBaseline = mutableBitmap.height - verticalPadding
        val timestampBaseline = locationBaseline - locationPaint.textSize - (6f * density)

        canvas.drawText(request.timestamp, startX, timestampBaseline, timestampPaint)
        canvas.drawText(request.location, startX, locationBaseline, locationPaint)

        return mutableBitmap
    }
}

internal actual fun decodeSelectedImage(base64: String): ImageBitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()

private fun OutputStream?.useBitmap(bitmap: Bitmap) {
    if (this == null) error("outputStream unavailable")
    use { stream ->
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream))
    }
}
