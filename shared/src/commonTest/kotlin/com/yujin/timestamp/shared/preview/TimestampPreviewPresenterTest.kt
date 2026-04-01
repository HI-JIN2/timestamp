package com.yujin.timestamp.shared.preview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimestampPreviewPresenterTest {
    @Test
    fun preview_usesExpectedRetroLabelShape() {
        val preview = TimestampPreviewPresenter.preview(
            hasSelectedPhoto = false,
        )

        assertTrue(
            actual = Regex("""\d{2}\.\d{2}\.\d{2}\s{2}\d{2}:\d{2}""").matches(preview.timestampLabel),
        )
        assertEquals("SEOUL, KR", preview.locationLabel)
    }

    @Test
    fun preview_updatesHelperTextAfterPhotoSelection() {
        val preview = TimestampPreviewPresenter.preview(
            hasSelectedPhoto = true,
        )

        assertTrue(preview.helperText.contains("선택한 사진 데이터"))
    }
}
