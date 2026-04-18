package com.yujin.timestamp.core.model

enum class TimestampAspectRatio(
    val widthUnit: Int,
    val heightUnit: Int,
) {
    ThreeFour(3, 4),
    SixteenNine(16, 9),
    ;

    val ratio: Float
        get() = widthUnit.toFloat() / heightUnit.toFloat()
}
