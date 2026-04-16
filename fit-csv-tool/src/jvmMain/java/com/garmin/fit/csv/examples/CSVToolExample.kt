/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.csv.examples

import com.garmin.fit.csv.CSVTool
import com.garmin.fit.csv.examples.CSVToolExample.exampleInputStream
import java.io.ByteArrayInputStream

object CSVToolExample {
    internal val exampleInputStream: ByteArrayInputStream
        get() {
            val bytes = byteArrayOf(
                0x0E,
                0x20,
                0x8B.toByte(),
                0x08,
                0x24,
                0x00,
                0x00,
                0x00,
                0x2E,
                0x46,
                0x49,
                0x54,
                0x8E.toByte(),
                0xA3.toByte(),  // File Header
                0x40,
                0x00,
                0x00,
                0x00,
                0x00,
                0x04,
                0x00,
                0x01,
                0x00,
                0x01,
                0x02,
                0x84.toByte(),
                0x04,
                0x04,
                0x86.toByte(),
                0x08,
                0x0A,
                0x07,  // Message Definition
                0x00,
                0x04,
                0x01,
                0x00,
                0x00,
                0xCA.toByte(),
                0x9A.toByte(),
                0x3B,
                0x61,
                0x62,
                0x63,
                0x64,
                0x65,
                0x66,
                0x67,
                0x68,
                0x69,
                0x00,  // Message
                0x5D,
                0xF2.toByte() // CRC
            )

            return ByteArrayInputStream(bytes)
        }
}

@Throws(Exception::class)
fun main(args: Array<String>) {
    val byteArrayInputStream: ByteArrayInputStream = exampleInputStream

    val csvTool = CSVTool()
    csvTool.enableEnumsAsStrings(true)
    csvTool.enableDateTimeAsISO8601(true)
    csvTool.enableSemicirclesAsDegrees(true)

    val byteArrayOutputStream = csvTool.convertFitToCsv(byteArrayInputStream)

    println(byteArrayOutputStream.toString())
}
