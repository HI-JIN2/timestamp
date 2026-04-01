package com.yujin.timestamp.app

import android.os.Bundle
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
            ) { uri ->
                selectedImageBase64 = uri
                    ?.let(contentResolver::openInputStream)
                    ?.use { inputStream -> inputStream.readBytes() }
                    ?.let { bytes -> Base64.encodeToString(bytes, Base64.NO_WRAP) }
            }

            TimestampApp(
                selectedImageBase64 = selectedImageBase64,
                onPickPhoto = { imagePicker.launch("image/*") },
            )
        }
    }
}
