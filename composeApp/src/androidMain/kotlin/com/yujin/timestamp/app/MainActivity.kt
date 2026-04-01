package com.yujin.timestamp.app

import android.net.Uri
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedImagePayload by remember { mutableStateOf<TimestampImagePayload?>(null) }
            var metadataTimestampLabel by remember { mutableStateOf<String?>(null) }
            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
            ) { uri ->
                selectedImagePayload = uri?.readImagePayload()
                metadataTimestampLabel = selectedImagePayload?.metadataTimestampLabel
            }

            TimestampApp(
                selectedImageBase64 = selectedImagePayload?.base64,
                metadataTimestampLabel = metadataTimestampLabel,
                onPickPhoto = { imagePicker.launch("image/*") },
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
}

internal actual fun decodeSelectedImage(base64: String): ImageBitmap? = runCatching {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
