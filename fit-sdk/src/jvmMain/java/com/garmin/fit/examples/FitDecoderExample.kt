/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.FitDecoder
import com.garmin.fit.FitMessages
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.plugins.HrToRecordMesgBroadcastPlugin
import java.io.FileInputStream
import java.io.IOException

object FitDecoderExample {
    @JvmStatic
    fun main(args: Array<String>) {
        println("FIT Decode Example Application")

        if (!validateCommandLine(args)) {
            printUsage()
            return
        }

        try {
            println("Opening file: " + args[0])
            val inputStream = FileInputStream(args[0])

            println("Decoding file...")
            val fitDecoder = FitDecoder()
            val fitMessages: FitMessages

            fitMessages = if (args.size == 1) {
                fitDecoder.decode(inputStream)
            } else {
                fitDecoder.decode(inputStream, HrToRecordMesgBroadcastPlugin())
            }

            println("File decoded")

            // fitMessages will contain all of the messages decoded from the file.
            printMessageSummary(fitMessages)
        } catch (e: IOException) {
            println("IOException opening file: " + args[0])
            e.printStackTrace()
            return
        } catch (e: FitRuntimeException) {
            println("FitRuntimeException decoding file: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            println("Exception decoding file: " + e.message)
            e.printStackTrace()
        }

        return
    }

    private fun validateCommandLine(args: Array<String>): Boolean {
        if (args.isEmpty() || args.size > 2) {
            return false
        }

        if (args.size == 2) {
            return args[1] == "-hr"
        }

        return true
    }

    private fun printUsage() {
        println("Usage: java DecodeExample <filename> [-hr]")
        println("<filename>      required")
        println("-hr             optional argument to use the HrMesgToBroadcastPlugin when decoding file")
    }

    private fun printMessageSummary(fitMessages: FitMessages) {
        if (!fitMessages.getFileIdMesgs().isEmpty()) {
            println("FileId Messages: " + fitMessages.getFileIdMesgs().size)
        }
    }
}
