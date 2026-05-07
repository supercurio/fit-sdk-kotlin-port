package com.garmin.fit.util

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant


object DateTimeConverter {
    const val FIT_EPOCH_MS: Long = 631065600000L

    /**
     * Converts a FIT timestamp to a ISO-8601 formatted time string.
     *
     * @param timestamp a FIT timestamp
     * @return a ISO-8601 formatted time string
     */
    fun fitTimestampToISO8601(timestamp: Long) = Instant
        .fromEpochMilliseconds(timestamp * 1000 + FIT_EPOCH_MS)
        .toString()

    /**
     * Parses a formatted ISO-8601 formatted time string in UTC and converts it to a FIT timestamp string.
     *
     * @param dateTime a formatted time string in UTC
     * @return a FIT timestamp string
     */
    @JvmStatic
    fun parseDateTime(dateTime: String): String {
        try {
            val instant = Instant.parse(dateTime) - FIT_EPOCH_MS.milliseconds
            return instant.epochSeconds.toString()
        } catch (_: Exception) {
            //no op
        }

        return dateTime
    }
}
