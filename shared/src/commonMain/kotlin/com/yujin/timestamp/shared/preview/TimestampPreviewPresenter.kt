package com.yujin.timestamp.shared.preview

import com.yujin.timestamp.shared.model.TimestampPreviewState

object TimestampPreviewPresenter {
    fun preview(): TimestampPreviewState {
        return TimestampPreviewState(
            timestampLabel = "04.01.26  03:42",
            locationLabel = "SEOUL, KR",
            helperText = "첫 단계에서는 공용 프리뷰 상태와 UI 뼈대를 맞추고, 이후 실제 사진 선택과 렌더링을 연결합니다.",
        )
    }
}
