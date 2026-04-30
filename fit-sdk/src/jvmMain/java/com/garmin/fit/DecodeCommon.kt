package com.garmin.fit

import com.garmin.fit.util.addOffset
import kotlin.math.round
import kotlin.math.roundToLong

internal fun applyScaleOffset64(
    bitsValue: Long, fieldType: Int,
    componentScale: Double, componentOffset: Double,
    fieldScale: Double, fieldOffset: Double
): Any {
    val isUnsigned = (fieldType == Fit.BASE_TYPE_UINT64 || fieldType == Fit.BASE_TYPE_UINT64Z)

    val offset = fieldOffset - componentOffset

    val noOffset = offset == Fit.FIELD_DEFAULT_OFFSET.toDouble()
    val noScale = componentScale == Fit.FIELD_DEFAULT_SCALE.toDouble() &&
            fieldScale == Fit.FIELD_DEFAULT_SCALE.toDouble()

    if (noOffset && noScale) return if (isUnsigned) bitsValue.toULong() else bitsValue

    if (noScale) return when {
        isUnsigned -> {
            val invalid = Fit.baseTypeInvalidMap[fieldType] as ULong
            bitsValue.toULong().addOffset(offset, invalid)
        }

        else -> bitsValue + offset.roundToLong()
    }

    val doubleValue = if (isUnsigned) bitsValue.toULong().toDouble() else bitsValue.toDouble()
    val result = (doubleValue / componentScale + offset) * fieldScale

    return if (isUnsigned) round(result).toULong() else result.toLong()
}
