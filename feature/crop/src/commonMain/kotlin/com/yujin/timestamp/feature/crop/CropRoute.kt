package com.yujin.timestamp.feature.crop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yujin.timestamp.feature.crop.ui.CropScreen
import com.yujin.timestamp.feature.crop.ui.rememberCropPalette
import com.yujin.timestamp.feature.crop.ui.timestampCropColorScheme

@Composable
fun CropRoute(
    state: CropUiContract.State,
    actions: (CropUiContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCropPalette(isSystemInDarkTheme())

    MaterialTheme(colorScheme = timestampCropColorScheme(isSystemInDarkTheme())) {
        CropScreen(
            state = state,
            actions = actions,
            modifier = modifier,
            palette = palette,
        )
    }
}
