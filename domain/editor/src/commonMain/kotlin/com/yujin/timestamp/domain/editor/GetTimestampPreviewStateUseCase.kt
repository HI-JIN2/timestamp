package com.yujin.timestamp.domain.editor

class GetTimestampPreviewStateUseCase {
    operator fun invoke(
        metadataTimestampLabel: String?,
    ): TimestampPreviewState {
        val timestampLabel = metadataTimestampLabel ?: "04.01.26  03:42"

        return TimestampPreviewState(
            timestampLabel = timestampLabel,
            locationLabel = "SEOUL, KR",
        )
    }
}
