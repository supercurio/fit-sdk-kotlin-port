package com.garmin.fit

import com.garmin.fit.DateTime.Companion.OFFSET
import java.util.Date
import kotlin.math.roundToLong

fun DateTime(instant: java.time.Instant) = DateTime(
    timestamp = instant.epochSecond - (OFFSET / 1000),
    fractional_timestamp = (instant.toEpochMilli() - OFFSET) % 1000 / 1000.0,
)

val DateTime.instant: java.time.Instant
    get() {
        // Express fractional component in (nearest) ms
        val fractional_ms = (this.fractionalTimestamp * 1000).roundToLong()

        return java.time.Instant.ofEpochMilli((timestamp * 1000) + fractional_ms + OFFSET)
    }

fun DateTime.Companion.from(instant: java.time.Instant): DateTime {
    return DateTime(instant)
}

fun DateTime(date: Date) = DateTime(
    timestamp = (date.time - OFFSET) / 1000,
    fractional_timestamp = ((date.time - OFFSET) % 1000) / 1000.0
)

val DateTime.date: Date
    get() {
        // Express fractional component in (nearest) ms
        val fractional_ms = (this.fractionalTimestamp * 1000).roundToLong()

        return Date((timestamp * 1000) + fractional_ms + OFFSET)
    }
