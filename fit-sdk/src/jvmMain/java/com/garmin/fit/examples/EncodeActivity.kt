/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.ActivityMesg
import com.garmin.fit.DateTime
import com.garmin.fit.DeveloperDataIdMesg
import com.garmin.fit.DeveloperField
import com.garmin.fit.DeviceIndex
import com.garmin.fit.DeviceInfoMesg
import com.garmin.fit.DisplayMeasure
import com.garmin.fit.Event
import com.garmin.fit.EventMesg
import com.garmin.fit.EventType
import com.garmin.fit.FieldDescriptionMesg
import com.garmin.fit.File
import com.garmin.fit.FileEncoder
import com.garmin.fit.FileIdMesg
import com.garmin.fit.Fit
import com.garmin.fit.FitBaseType
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.LapMesg
import com.garmin.fit.LengthMesg
import com.garmin.fit.LengthType
import com.garmin.fit.Manufacturer
import com.garmin.fit.Mesg
import com.garmin.fit.MesgNum
import com.garmin.fit.RecordMesg
import com.garmin.fit.SessionMesg
import com.garmin.fit.Sport
import com.garmin.fit.SubSport
import com.garmin.fit.SwimStroke
import java.util.Date
import java.util.Random
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sin

object EncodeActivity {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            CreateTimeBasedActivity()
            CreateLapSwimActivity()
        } catch (e: Exception) {
            println("Exception encoding activity: " + e.message)
            e.printStackTrace()
        }
    }

    fun CreateTimeBasedActivity() {
        val twoPI = Math.PI * 2.0
        val semiCirclesPerMeter = 107.173
        val filename = "ActivityEncodeRecipe.fit"

        val messages = mutableListOf<Mesg>()

        // The starting timestamp for the activity
        val startTime = DateTime(Date())

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        val eventMesg = EventMesg()
        eventMesg.timestamp = startTime
        eventMesg.event = Event.TIMER
        eventMesg.eventType = EventType.START
        messages.add(eventMesg)

        // Create the Developer Id message for the developer data fields.
        val developerIdMesg = DeveloperDataIdMesg()
        // It is a BEST PRACTICE to reuse the same Guid for all FIT files created by your platform
        val appId = byteArrayOf(
            0x1, 0x1, 0x2, 0x3,
            0x5, 0x8, 0xD, 0x15,
            0x22, 0x37, 0x59, 0x90.toByte(),
            0xE9.toByte(), 0x79, 0x62, 0xDB.toByte()
        )

        for (i in appId.indices) {
            developerIdMesg.setApplicationId(i, appId[i])
        }

        developerIdMesg.developerDataIndex = 0.toShort()
        messages.add(developerIdMesg)

        // Create the Developer Data Field Descriptions
        val doughnutsFieldDescMesg = FieldDescriptionMesg()
        doughnutsFieldDescMesg.developerDataIndex = 0.toShort()
        doughnutsFieldDescMesg.fieldDefinitionNumber = 0.toShort()
        doughnutsFieldDescMesg.fitBaseTypeId = FitBaseType.FLOAT32
        doughnutsFieldDescMesg.setUnits(0, "doughnuts")
        doughnutsFieldDescMesg.nativeMesgNum = MesgNum.SESSION
        messages.add(doughnutsFieldDescMesg)

        val hrFieldDescMesg = FieldDescriptionMesg()
        hrFieldDescMesg.developerDataIndex = 0.toShort()
        hrFieldDescMesg.fieldDefinitionNumber = 1.toShort()
        hrFieldDescMesg.fitBaseTypeId = FitBaseType.UINT8
        hrFieldDescMesg.setFieldName(0, "Heart Rate")
        hrFieldDescMesg.setUnits(0, "bpm")
        hrFieldDescMesg.nativeFieldNum = RecordMesg.HeartRateFieldNum.toShort()
        hrFieldDescMesg.nativeMesgNum = MesgNum.RECORD
        messages.add(hrFieldDescMesg)

        // Every FIT ACTIVITY file MUST contain Record messages
        val timestamp = DateTime(startTime)

        // Create one hour (3600 seconds) of Record data
        for (i in 0..3600) {
            // Create a new Record message and set the timestamp
            val recordMesg = RecordMesg()
            recordMesg.timestamp = timestamp

            // Fake Record Data of Various Signal Patterns
            recordMesg.distance = i.toFloat()
            recordMesg.speed = 1f
            recordMesg.heartRate = ((sin(twoPI * (0.01 * i + 10)) + 1.0) * 127.0).toInt().toShort()
            // Sine
            recordMesg.cadence = (i % 255).toShort() // Sawtooth
            recordMesg.power = (if ((i % 255).toShort() < 157) 150 else 250) //Square
            recordMesg.altitude = (abs(i.toDouble() % 255.0) - 127.0).toFloat() // Triangle
            recordMesg.positionLat = 0
            recordMesg.positionLong = (i * semiCirclesPerMeter).roundToLong().toInt()

            // Add a Developer Field to the Record Message
            val hrDevField = DeveloperField(hrFieldDescMesg, developerIdMesg)
            recordMesg.addDeveloperField(hrDevField)
            hrDevField.value = (sin(twoPI * (.01 * i + 10)) + 1.0).toInt().toShort() * 127.0

            // Write the Record message to the output stream
            messages.add(recordMesg)

            // Increment the timestamp by one second
            timestamp.add(1)
        }

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        val eventMesgStop = EventMesg()
        eventMesgStop.timestamp = timestamp
        eventMesgStop.event = Event.TIMER
        eventMesgStop.eventType = EventType.STOP_ALL
        messages.add(eventMesgStop)

        // Every FIT ACTIVITY file MUST contain at least one Lap message
        val lapMesg = LapMesg()
        lapMesg.messageIndex = 0
        lapMesg.timestamp = timestamp
        lapMesg.startTime = startTime
        lapMesg.totalElapsedTime = (timestamp.timestamp - startTime.timestamp).toFloat()
        lapMesg.totalTimerTime = (timestamp.timestamp - startTime.timestamp).toFloat()
        messages.add(lapMesg)

        // Every FIT ACTIVITY file MUST contain at least one Session message
        val sessionMesg = SessionMesg()
        sessionMesg.messageIndex = 0
        sessionMesg.timestamp = timestamp
        sessionMesg.startTime = startTime
        sessionMesg.totalElapsedTime = (timestamp.timestamp - startTime.timestamp).toFloat()
        sessionMesg.totalTimerTime = (timestamp.timestamp - startTime.timestamp).toFloat()
        sessionMesg.sport = Sport.STAND_UP_PADDLEBOARDING
        sessionMesg.subSport = SubSport.GENERIC
        sessionMesg.firstLapIndex = 0
        sessionMesg.numLaps = 1
        messages.add(sessionMesg)

        // Add a Developer Field to the Session message
        val doughnutsEarnedDevField = DeveloperField(doughnutsFieldDescMesg, developerIdMesg)
        doughnutsEarnedDevField.value = sessionMesg.totalElapsedTime?.let { it / 1200.0f }
        sessionMesg.addDeveloperField(doughnutsEarnedDevField)

        // Every FIT ACTIVITY file MUST contain EXACTLY one Activity message
        val activityMesg = ActivityMesg()
        activityMesg.timestamp = timestamp
        activityMesg.numSessions = 1
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val timezoneOffset = ((timeZone.rawOffset + timeZone.dstSavings) / 1000).toLong()
        activityMesg.localTimestamp = timestamp.timestamp + timezoneOffset
        activityMesg.totalTimerTime = (timestamp.timestamp - startTime.timestamp).toFloat()
        messages.add(activityMesg)

        CreateActivityFile(messages, filename, startTime)
    }

    fun CreateLapSwimActivity() {
        val filename = "ActivityEncodeRecipeLapSwim.fit"
        val messages = mutableListOf<Mesg>()

        // The starting timestamp for the activity
        val startTime = DateTime(Date())

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        val eventMesgStart = EventMesg()
        eventMesgStart.timestamp = startTime
        eventMesgStart.event = Event.TIMER
        eventMesgStart.eventType = EventType.START
        messages.add(eventMesgStart)

        // Create a Length or Lap message for each item in the sample swim data. Calculate
        // distance, duration, and stroke count for each lap and the overall session.

        // Session Accumulators
        var sessionTotalElapsedTime = 0
        var sessionDistance = 0f
        var sessionNumLengths: Short = 0
        var sessionNumActiveLengths: Short = 0
        var sessionTotalStrokes: Short = 0
        var sessionNumLaps = 0

        // Lap accumulators
        var lapTotalElapsedTime = 0
        var lapDistance = 0f
        var lapNumActiveLengths: Short = 0
        var lapNumLengths: Short = 0
        var lapFirstLengthIndex: Short = 0
        var lapTotalStrokes: Short = 0
        var lapStartTime = DateTime(startTime)

        val poolLength = 22.86f
        var messageIndex: Short = 0
        val poolLengthUnit = DisplayMeasure.STATUTE
        val timestamp = DateTime(startTime)

        val swimData: MutableList<MutableMap<String?, Any?>> =
            swimLengths

        for (swimLength in swimData) {
            val type = swimLength["type"] as String

            if (type == "LAP") {
                // Create a Lap message, set its fields, and write it to the file
                val lapMesg = LapMesg()
                lapMesg.messageIndex = sessionNumLaps
                lapMesg.timestamp = timestamp
                lapMesg.startTime = lapStartTime
                lapMesg.totalElapsedTime = lapTotalElapsedTime.toFloat()
                lapMesg.totalTimerTime = lapTotalElapsedTime.toFloat()
                lapMesg.totalDistance = lapDistance
                lapMesg.firstLengthIndex = lapFirstLengthIndex.toInt()
                lapMesg.numActiveLengths = lapNumActiveLengths.toInt()
                lapMesg.numLengths = lapNumLengths.toInt()
                lapMesg.totalStrokes = lapTotalStrokes.toLong()
                lapMesg.avgStrokeDistance = lapDistance / lapTotalStrokes
                lapMesg.sport = Sport.SWIMMING
                lapMesg.subSport = SubSport.LAP_SWIMMING
                messages.add(lapMesg)

                sessionNumLaps++

                // Reset the Lap accumulators
                lapFirstLengthIndex = messageIndex
                lapNumActiveLengths = 0
                lapNumLengths = 0
                lapTotalElapsedTime = 0
                lapDistance = 0f
                lapTotalStrokes = 0
                lapStartTime = DateTime(timestamp)
            } else {
                val duration = swimLength["duration"] as Int
                val lengthType = LengthType.valueOf(type)

                // Create a Length message and its fields
                val lengthMesg = LengthMesg()
                lengthMesg.messageIndex = (messageIndex++).toInt()
                lengthMesg.startTime = timestamp
                lengthMesg.totalElapsedTime = duration.toFloat()
                lengthMesg.totalTimerTime = duration.toFloat()
                lengthMesg.lengthType = lengthType

                timestamp.add(duration.toLong())
                lengthMesg.timestamp = timestamp

                // Create the Record message that pairs with the Length Message
                val recordMesg = RecordMesg()
                recordMesg.timestamp = timestamp
                recordMesg.distance = sessionDistance + poolLength

                // Is this an Active Length?
                if (lengthType == LengthType.ACTIVE) {
                    // Get the Active data from the model
                    val stroke =
                        if (swimLength.containsKey("stroke")) swimLength["stroke"] as String? else "FREESTYLE"
                    val strokes =
                        if (swimLength.containsKey("strokes")) swimLength["strokes"] as Int else 0
                    val swimStroke = SwimStroke.valueOf(stroke!!)

                    // Set the Active data on the Length Message
                    lengthMesg.avgSpeed = poolLength / (duration.toFloat())
                    lengthMesg.swimStroke = swimStroke
                    val cadence = (strokes * 60 / duration).toShort()

                    if (strokes > 0) {
                        lengthMesg.totalStrokes = strokes
                        lengthMesg.avgSwimmingCadence = cadence
                    }

                    // Set the Active data on the Record Message
                    recordMesg.speed = poolLength / (duration.toFloat())

                    if (strokes > 0) {
                        recordMesg.cadence = cadence
                    }

                    // Increment the "Active" accumulators
                    sessionNumActiveLengths++
                    lapNumActiveLengths++
                    sessionDistance += poolLength
                    lapDistance += poolLength
                    sessionTotalStrokes = (sessionTotalStrokes + strokes).toShort()
                    lapTotalStrokes = (lapTotalStrokes + strokes).toShort()
                }

                // Write the messages to the file
                messages.add(recordMesg)
                messages.add(lengthMesg)

                // Increment the "Total" accumulators
                sessionTotalElapsedTime += duration
                lapTotalElapsedTime += duration
                sessionNumLengths++
                lapNumLengths++
            }
        }

        // Timer Events are a BEST PRACTICE for FIT ACTIVITY files
        val eventMesgStop = EventMesg()
        eventMesgStop.timestamp = timestamp
        eventMesgStop.event = Event.TIMER
        eventMesgStop.eventType = EventType.STOP_ALL
        messages.add(eventMesgStop)

        // Every FIT ACTIVITY file MUST contain at least one Session message
        val sessionMesg = SessionMesg()
        sessionMesg.messageIndex = 0
        sessionMesg.timestamp = timestamp
        sessionMesg.startTime = startTime
        sessionMesg.totalElapsedTime = sessionTotalElapsedTime.toFloat()
        sessionMesg.totalTimerTime = sessionTotalElapsedTime.toFloat()
        sessionMesg.totalDistance = sessionDistance
        sessionMesg.sport = Sport.SWIMMING
        sessionMesg.subSport = SubSport.LAP_SWIMMING
        sessionMesg.firstLapIndex = 0
        sessionMesg.numLaps = sessionNumLaps
        sessionMesg.poolLength = poolLength
        sessionMesg.poolLengthUnit = poolLengthUnit
        sessionMesg.numLengths = sessionNumLengths.toInt()
        sessionMesg.numActiveLengths = sessionNumActiveLengths.toInt()
        sessionMesg.totalStrokes = sessionTotalStrokes.toLong()
        sessionMesg.avgStrokeDistance = sessionDistance / sessionTotalStrokes
        messages.add(sessionMesg)

        // Every FIT ACTIVITY file MUST contain EXACTLY one Activity message
        val activityMesg = ActivityMesg()
        activityMesg.timestamp = timestamp
        activityMesg.numSessions = 1
        val timeZone = TimeZone.getTimeZone("America/Denver")
        val timezoneOffset = ((timeZone.rawOffset + timeZone.dstSavings) / 1000).toLong()
        activityMesg.localTimestamp = timestamp.timestamp + timezoneOffset
        activityMesg.totalTimerTime = sessionTotalElapsedTime.toFloat()

        messages.add(activityMesg)

        CreateActivityFile(messages, filename, startTime)
    }

    fun CreateActivityFile(messages: MutableList<Mesg>, filename: String, startTime: DateTime) {
        // The combination of file type, manufacturer id, product id, and serial number should be unique.
        // When available, a non-random serial number should be used.
        val fileType = File.ACTIVITY
        val manufacturerId = Manufacturer.DEVELOPMENT.toShort()
        val productId: Short = 0
        val softwareVersion = 1.0f

        val random = Random()
        val serialNumber = random.nextInt()

        // Every FIT file MUST contain a File ID message
        val fileIdMesg = FileIdMesg()
        fileIdMesg.type = fileType
        fileIdMesg.manufacturer = manufacturerId.toInt()
        fileIdMesg.product = productId.toInt()
        fileIdMesg.timeCreated = startTime
        fileIdMesg.serialNumber = serialNumber.toLong()

        // A Device Info message is a BEST PRACTICE for FIT ACTIVITY files
        val deviceInfoMesg = DeviceInfoMesg()
        deviceInfoMesg.deviceIndex = DeviceIndex.CREATOR
        deviceInfoMesg.manufacturer = Manufacturer.DEVELOPMENT
        deviceInfoMesg.product = productId.toInt()
        deviceInfoMesg.productName = "FIT Cookbook" // Max 20 Chars
        deviceInfoMesg.serialNumber = serialNumber.toLong()
        deviceInfoMesg.softwareVersion = softwareVersion
        deviceInfoMesg.timestamp = startTime

        // Create the output stream
        val encode: FileEncoder?

        try {
            encode = FileEncoder(java.io.File(filename), Fit.ProtocolVersion.V2_0)
        } catch (e: FitRuntimeException) {
            System.err.println("Error opening file $filename")
            e.printStackTrace()
            return
        }

        encode.write(fileIdMesg)
        encode.write(deviceInfoMesg)

        for (message in messages) {
            encode.write(message)
        }

        // Close the output stream
        try {
            encode.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing encode.")
            e.printStackTrace()
            return
        }
        println("Encoded FIT Activity file $filename")
    }

    val swimLengths: MutableList<MutableMap<String?, Any?>>
        /**
         * Creates an example pool swim data set
         * Each length contains a type, duration,
         * stroke type, and stroke count.
         *
         * @return a list of maps where each map is a pool length.
         */
        get() {
            // Example Swim length representing a 500 yard pool swim using different strokes and drills.
            val length0: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 20)
                        put("stroke", "FREESTYLE")
                        put("strokes", 30)
                    }
                }

            val length1: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 25)
                        put("stroke", "FREESTYLE")
                        put("strokes", 20)
                    }
                }

            val length2: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 30)
                        put("stroke", "FREESTYLE")
                        put("strokes", 10)
                    }
                }

            val length3: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 35)
                        put("stroke", "FREESTYLE")
                        put("strokes", 20)
                    }
                }

            val length4: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length5: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "IDLE")
                        put("duration", 60)
                    }
                }

            val length6: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length7: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 20)
                        put("stroke", "BACKSTROKE")
                        put("strokes", 30)
                    }
                }

            val length8: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 25)
                        put("stroke", "BACKSTROKE")
                        put("strokes", 20)
                    }
                }

            val length9: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 30)
                        put("stroke", "BACKSTROKE")
                        put("strokes", 10)
                    }
                }

            val length10: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 35)
                        put("stroke", "BACKSTROKE")
                        put("strokes", 20)
                    }
                }

            val length11: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length12: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "IDLE")
                        put("duration", 60)
                    }
                }

            val length13: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length14: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 20)
                        put("stroke", "BREASTSTROKE")
                        put("strokes", 30)
                    }
                }

            val length15: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 25)
                        put("stroke", "BREASTSTROKE")
                        put("strokes", 20)
                    }
                }

            val length16: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 30)
                        put("stroke", "BREASTSTROKE")
                        put("strokes", 10)
                    }
                }

            val length17: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 35)
                        put("stroke", "BREASTSTROKE")
                        put("strokes", 20)
                    }
                }

            val length18: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length19: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "IDLE")
                        put("duration", 60)
                    }
                }

            val length20: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length21: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 20)
                        put("stroke", "BUTTERFLY")
                        put("strokes", 30)
                    }
                }

            val length22: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 25)
                        put("stroke", "BUTTERFLY")
                        put("strokes", 20)
                    }
                }

            val length23: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 30)
                        put("stroke", "BUTTERFLY")
                        put("strokes", 10)
                    }
                }

            val length24: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 35)
                        put("stroke", "BUTTERFLY")
                        put("strokes", 20)
                    }
                }

            val length25: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length26: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "IDLE")
                        put("duration", 60)
                    }
                }

            val length27: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val length28: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 40)
                        put("stroke", "DRILL")
                    }
                }

            val length29: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 40)
                        put("stroke", "DRILL")
                    }
                }

            val length30: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 40)
                        put("stroke", "DRILL")
                    }
                }

            val length31: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "ACTIVE")
                        put("duration", 40)
                        put("stroke", "DRILL")
                    }
                }

            val length32: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("type", "LAP")
                    }
                }

            val swimLengths: MutableList<MutableMap<String?, Any?>> =
                ArrayList(
                    listOf(
                        length0,
                        length1,
                        length2,
                        length3,
                        length4,
                        length5,
                        length6,
                        length7,
                        length8,
                        length9,
                        length10,
                        length11,
                        length12,
                        length13,
                        length14,
                        length15,
                        length16,
                        length17,
                        length18,
                        length19,
                        length20,
                        length21,
                        length22,
                        length23,
                        length24,
                        length25,
                        length26,
                        length27,
                        length28,
                        length29,
                        length30,
                        length31,
                        length32
                    )
                )
            return swimLengths
        }
}
