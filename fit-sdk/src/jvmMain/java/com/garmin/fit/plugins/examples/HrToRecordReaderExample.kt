/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.plugins.examples

import com.garmin.fit.BufferedMesgBroadcaster
import com.garmin.fit.Decode
import com.garmin.fit.Fit
import com.garmin.fit.HrMesg
import com.garmin.fit.HrMesgListener
import com.garmin.fit.MesgBroadcastPlugin
import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import com.garmin.fit.csv.MesgCSVWriter
import com.garmin.fit.plugins.HrToRecordMesgBroadcastPlugin
import com.garmin.fit.util.StreamHelpers
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException

/**
 * Example demonstrating usage of BufferedMesgBroadcaster and
 * HrToRecordMesgBroadcaster plugin.
 * 
 * 
 * The example outputs all
 * record message and HR messages to a CSV file with the record
 * messages to match the HR data if their times align
 * 
 */
class HrToRecordReaderExample : RecordMesgListener, HrMesgListener {
    private var mesgWriter: MesgCSVWriter? = null

    override fun onMesg(mesg: RecordMesg) {
        mesgWriter!!.onMesg(mesg)
    }

    override fun onMesg(mesg: HrMesg) {
        mesgWriter!!.onMesg(mesg)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.out.printf(
                "FIT Hr Record Reader Example Application - Protocol %d.%d Profile %d.%d %s\n",
                Fit.PROTOCOL_VERSION_MAJOR,
                Fit.PROTOCOL_VERSION_MINOR,
                Fit.PROFILE_VERSION_MAJOR,
                Fit.PROFILE_VERSION_MINOR,
                Fit.PROFILE_TYPE
            )

            var `in`: FileInputStream?

            val example = HrToRecordReaderExample()
            val decode = Decode()
            val mesgBroadcaster = BufferedMesgBroadcaster(decode)

            if (args.size != 1) {
                println("Usage: java -jar FitHrRecordReaderExample.jar <input file>.fit")
                return
            }

            try {
                `in` = FileInputStream(args[0])
            } catch (e: IOException) {
                throw RuntimeException("Error opening file " + args[0])
            }

            try {
                if (!decode.checkFileIntegrity(`in`)) {
                    throw RuntimeException("FIT file integrity failed.")
                }
            } catch (e: RuntimeException) {
                System.err.print("Exception Checking File Integrity: ")
                System.err.println(e.message)
            } finally {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            try {
                `in` = FileInputStream(args[0])
            } catch (e: IOException) {
                throw RuntimeException("Error opening file " + args[0] + " [2]")
            }

            val outputFile = args[0] + "-HrRecordExampleProcessed.csv"
            mesgBroadcaster.addListener(example as RecordMesgListener)
            mesgBroadcaster.addListener(example as HrMesgListener)

            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                example.mesgWriter = MesgCSVWriter(byteArrayOutputStream)

                // Create plugin and register with mesgBroadcaster
                val plugin: MesgBroadcastPlugin = HrToRecordMesgBroadcastPlugin()
                mesgBroadcaster.registerMesgBroadcastPlugin(plugin)

                mesgBroadcaster.run(`in`) // Run decoder
                mesgBroadcaster.broadcast() // End of file so flush pending data.
                example.mesgWriter!!.close()

                StreamHelpers.writeByteStreamToFile(byteArrayOutputStream, outputFile, true)
            } catch (e: Exception) {
                System.err.print("Exception decoding file: ")
                System.err.println(e.message)
            }

            println("Decoded Record and Hr data from " + args[0] + " to " + outputFile)
        }
    }
}
