package com.garmin.fit.util

import kotlin.math.abs

internal fun ULong.addOffset(
    offset: Double,
    invalidValue: ULong,
    applyInvalidValue: Boolean = false
): ULong {
    val roundedOffset = if (offset >= 0.0)
        (offset + 0.5).toULong()
    else
        abs(offset - 0.5).toULong()

    if (applyInvalidValue)
        if (offset >= 0.0) {
            if (roundedOffset > ULong.MAX_VALUE - this) return invalidValue
        } else {
            if (roundedOffset > this) return invalidValue
        }

    return if (offset >= 0.0)
        this + roundedOffset
    else
        this - roundedOffset
}

internal fun ULong.subtractOffset(
    offset: Double,
    invalidValue: ULong,
    applyInvalidValue: Boolean = false
) = addOffset(-offset, invalidValue, applyInvalidValue)
