package com.yujin.timestamp.domain.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetEditorInitialStateUseCaseTest {
    private val useCase = GetEditorInitialStateUseCase()

    @Test
    fun returnsMetadataTimestampWhenAvailable() {
        val preview = useCase(
            metadataTimestampLabel = "03.04.26  19:10",
        )

        assertEquals("03.04.26  19:10", preview.timestampLabel)
        assertEquals("SEOUL, KR", preview.locationLabel)
    }

    @Test
    fun returnsSampleTimestampWhenMetadataMissing() {
        val preview = useCase(
            metadataTimestampLabel = null,
        )

        assertTrue(Regex("""\d{2}\.\d{2}\.\d{2}\s{2}\d{2}:\d{2}""").matches(preview.timestampLabel))
        assertEquals("SEOUL, KR", preview.locationLabel)
    }
}
