package com.yujin.timestamp.shared.preview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimestampPreviewPresenterTest {
    @Test
    fun preview_usesExpectedRetroLabelShape() {
        val preview = TimestampPreviewPresenter.preview(
            hasSelectedPhoto = false,
            metadataTimestampLabel = null,
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
            metadataTimestampLabel = "03.29.26  09:14",
        )

        assertTrue(preview.helperText.contains("메타데이터 날짜"))
        assertEquals("기본값: 사진 메타데이터 촬영 일시", preview.metadataDescription)
    }
}
