package com.yujin.timestamp.feature.editor

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yujin.timestamp.feature.crop.TimestampCropRoute

@Composable
internal fun TimestampEditorScreen(
    state: TimestampEditorUiContract.State,
    onIntent: (TimestampEditorUiContract.Intent) -> Unit,
    onPickPhoto: () -> Unit,
    onEditDateRequest: (String) -> Unit,
    onEditTimeRequest: (String) -> Unit,
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
                    TimestampCropRoute(
                        previewImage = state.previewImage,
                        aspectRatioPreset = state.aspectRatioPreset,
                        cropLeftRatio = state.cropLeftRatio,
                        cropTopRatio = state.cropTopRatio,
                        cropWidthRatio = state.cropWidthRatio,
                        cropHeightRatio = state.cropHeightRatio,
                        onAspectRatioChanged = {
                            onIntent(TimestampEditorUiContract.Intent.AspectRatioChanged(it))
                        },
                        onCropRectChanged = { leftRatio, topRatio, widthRatio, heightRatio ->
                            onIntent(
                                TimestampEditorUiContract.Intent.CropRectChanged(
                                    leftRatio = leftRatio,
                                    topRatio = topRatio,
                                    widthRatio = widthRatio,
                                    heightRatio = heightRatio,
                                ),
                            )
                        },
                        onResetCrop = { onIntent(TimestampEditorUiContract.Intent.ResetCrop) },
                        onClose = { onIntent(TimestampEditorUiContract.Intent.CloseCropEditor) },
                    )
                } else {
                    EditorHomeSection(
                        state = state,
                        onIntent = onIntent,
                        onPickPhoto = onPickPhoto,
                        onEditDateRequest = onEditDateRequest,
                        onEditTimeRequest = onEditTimeRequest,
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
