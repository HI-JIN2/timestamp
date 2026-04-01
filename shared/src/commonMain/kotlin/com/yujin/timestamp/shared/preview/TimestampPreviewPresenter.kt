package com.yujin.timestamp.shared.preview

import com.yujin.timestamp.shared.model.TimestampPreviewState

object TimestampPreviewPresenter {
    fun preview(hasSelectedPhoto: Boolean): TimestampPreviewState {
        return TimestampPreviewState(
            timestampLabel = "04.01.26  03:42",
            locationLabel = "SEOUL, KR",
            helperText = if (hasSelectedPhoto) {
                "선택한 사진 데이터를 연결했습니다. 다음 단계에서 실제 이미지 프리뷰와 타임스탬프 렌더링을 붙입니다."
            } else {
                "플랫폼별 사진 선택기를 연결했습니다. 먼저 사진을 고른 뒤 실제 프리뷰와 타임스탬프 렌더링으로 이어집니다."
            },
        )
    }
}
