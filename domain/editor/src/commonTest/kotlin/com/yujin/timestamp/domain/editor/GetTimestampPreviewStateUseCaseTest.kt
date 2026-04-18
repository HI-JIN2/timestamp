package com.yujin.timestamp.domain.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTimestampPreviewStateUseCaseTest {
    private val useCase = GetTimestampPreviewStateUseCase()

    @Test
    fun returnsMetadataTimestampWhenAvailable() {
        val preview = useCase(
            hasSelectedPhoto = true,
            metadataTimestampLabel = "03.04.26  19:10",
        )

        assertEquals("03.04.26  19:10", preview.timestampLabel)
        assertTrue(preview.metadataDescription.contains("메타데이터"))
    }

    @Test
    fun returnsSampleTimestampWhenMetadataMissing() {
        val preview = useCase(
            hasSelectedPhoto = false,
            metadataTimestampLabel = null,
        )

        assertTrue(Regex("""\d{2}\.\d{2}\.\d{2}\s{2}\d{2}:\d{2}""").matches(preview.timestampLabel))
        assertEquals("SEOUL, KR", preview.locationLabel)
    }
}
