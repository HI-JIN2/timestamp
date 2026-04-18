package com.yujin.timestamp.domain.editor

class GetEditorInitialStateUseCase {
    operator fun invoke(
        metadataTimestampLabel: String?,
    ): EditorInitialState {
        val timestampLabel = metadataTimestampLabel ?: "04.01.26  03:42"

        return EditorInitialState(
            timestampLabel = timestampLabel,
            locationLabel = "SEOUL, KR",
        )
    }
}
