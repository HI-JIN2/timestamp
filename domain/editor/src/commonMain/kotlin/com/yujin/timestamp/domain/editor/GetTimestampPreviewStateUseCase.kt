package com.yujin.timestamp.domain.editor

class GetTimestampPreviewStateUseCase {
    operator fun invoke(
        hasSelectedPhoto: Boolean,
        metadataTimestampLabel: String?,
    ): TimestampPreviewState {
        val timestampLabel = metadataTimestampLabel ?: "04.01.26  03:42"

        return TimestampPreviewState(
            timestampLabel = timestampLabel,
            metadataDescription = if (metadataTimestampLabel != null) {
                "기본값: 사진 메타데이터 촬영 일시"
            } else {
                "기본값: 샘플 타임스탬프"
            },
            locationLabel = "SEOUL, KR",
            helperText = if (hasSelectedPhoto) {
                if (metadataTimestampLabel != null) {
                    "사진 메타데이터 날짜를 기본 타임스탬프로 불러왔습니다. 필요하면 아래에서 직접 수정할 수 있습니다."
                } else {
                    "메타데이터 날짜를 찾지 못해 기본 타임스탬프를 사용합니다. 아래에서 직접 수정할 수 있습니다."
                }
            } else {
                "사진을 고르면 실제 프리뷰를 표시하고, 메타데이터 날짜를 기본 타임스탬프로 불러옵니다."
            },
        )
    }
}
