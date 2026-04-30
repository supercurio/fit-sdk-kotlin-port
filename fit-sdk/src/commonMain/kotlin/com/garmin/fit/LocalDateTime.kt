package com.garmin.fit

import kotlin.time.Instant

class LocalDateTime {
    var timestamp: Long
        private set

    constructor(timestamp: Long) {
        this.timestamp = timestamp
    }

    override fun equals(other: Any?): Boolean {
        if (other is DateTime) return (this.timestamp == other.timestamp)
        return false
    }

    fun convertSystemTimeToLocal(offset: Long) {
        if (timestamp < MIN) {
            timestamp += offset
        }
    }

    val instantKt: Instant
        get() = Instant.fromEpochMilliseconds(timestamp * 1000 + OFFSET)

    override fun toString(): String {
        return instantKt.toString()
    }

    companion object {
        // if date_time is < 0x10000000 then it is system time (seconds from device power on)
        const val MIN: Long = 0x10000000
        const val INVALID: Long = Fit.UINT32_INVALID

        // Offset between Garmin (FIT) time and Unix time in ms (Dec 31, 1989 - 00:00:00 January 1, 1970).
        const val OFFSET: Long = 631065600000L

        private val stringMap = mapOf(MIN to "MIN")


        /**
         * Retrieves the String Representation of the Value
         * @param value The enum constant
         * @return The name of this enum contsant
         */
        fun getStringFromValue(value: Long): String {
            if (stringMap.containsKey(value)) {
                return stringMap[value] ?: ""
            }

            return ""
        }

        /**
         * Returns the enum constant with the specified name.
         * @param value The enum string value
         * @return The enum constant or INVALID if unknown
         */
        fun getValueFromString(value: String): Long {
            for (entry in stringMap.entries) {
                if (entry.value == value) {
                    return entry.key
                }
            }

            return INVALID
        }
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + instantKt.hashCode()
        return result
    }
}