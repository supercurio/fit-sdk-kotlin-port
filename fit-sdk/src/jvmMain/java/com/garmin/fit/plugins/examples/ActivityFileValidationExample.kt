/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.plugins.examples

import com.garmin.fit.FitDecoder
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.plugins.ActivityFileValidationPlugin
import java.io.FileInputStream
import java.io.IOException

object ActivityFileValidationExample {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Activity File Validator")

        if (!ActivityFileValidationExample.validateCommandLine(args)) {
            printUsage()
            return
        }

        val plugin = ActivityFileValidationPlugin()

        try {
            println("Opening file: " + args[0])
            val inputStream = FileInputStream(args[0])

            val fitDecoder = FitDecoder()

            println("Decoding file...")
            fitDecoder.decode(inputStream, plugin)

            println("File decoded")
        } catch (e: IOException) {
            println("IOException opening file: " + args[0])
            e.printStackTrace()
        } catch (e: FitRuntimeException) {
            println("FitRuntimeException decoding file: " + e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            println("Exception decoding file: " + e.message)
            e.printStackTrace()
        } finally {
            // If an exception occurs before onBroadcast() is called,
            // then the validation checks will not be executed. There
            // may still be decoded messages that are worth validating,
            // so force the validation checks to execute. Some tests may be
            // skipped or fail due to missing messages.
            if (plugin.getResults().size == 0) {
                plugin.repeatValidation()
            }

            printValidationReport(plugin)
        }
    }

    private fun validateCommandLine(args: Array<String?>): Boolean {
        return args.size == 1
    }

    private fun printUsage() {
        println("Usage: java DecodeExample <filename>")
        println("<filename>      required")
    }

    private fun printValidationReport(plugin: ActivityFileValidationPlugin) {
        println("Message Count: " + plugin.getMesgCount())

        for (result in plugin.getResults()) {
            println(result)
            if (result.getDescription() != null) {
                println("\t" + result.getDescription())
            }
        }
    }
}
