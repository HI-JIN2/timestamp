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
import com.yujin.timestamp.feature.crop.TimestampCropUiContract

@Composable
internal fun TimestampEditorScreen(
    state: TimestampEditorUiContract.State,
    actions: (TimestampEditorUiContract.Action) -> Unit,
    modifier: Modifier = Modifier,
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
        actions(TimestampEditorUiContract.Action.ExportMessageShown)
    }

    MaterialTheme(colorScheme = timestampColorScheme(isDarkTheme)) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
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
                        state = TimestampCropUiContract.State(
                            previewImage = state.previewImage,
                            aspectRatio = state.aspectRatioPreset,
                            cropLeftRatio = state.cropLeftRatio,
                            cropTopRatio = state.cropTopRatio,
                            cropWidthRatio = state.cropWidthRatio,
                            cropHeightRatio = state.cropHeightRatio,
                        ),
                        actions = { cropAction ->
                            when (cropAction) {
                                is TimestampCropUiContract.Action.AspectRatioChanged -> {
                                    actions(TimestampEditorUiContract.Action.AspectRatioChanged(cropAction.value))
                                }
                                is TimestampCropUiContract.Action.CropRectChanged -> {
                                    actions(
                                        TimestampEditorUiContract.Action.CropRectChanged(
                                            leftRatio = cropAction.leftRatio,
                                            topRatio = cropAction.topRatio,
                                            widthRatio = cropAction.widthRatio,
                                            heightRatio = cropAction.heightRatio,
                                        ),
                                    )
                                }
                                TimestampCropUiContract.Action.Reset -> {
                                    actions(TimestampEditorUiContract.Action.ResetCrop)
                                }
                                TimestampCropUiContract.Action.Done -> {
                                    actions(TimestampEditorUiContract.Action.CloseCropEditor)
                                }
                            }
                        },
                    )
                } else {
                    EditorHomeSection(
                        state = state,
                        actions = actions,
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
