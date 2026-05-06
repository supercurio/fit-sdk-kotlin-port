/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.ActivityType
import com.garmin.fit.BatteryStatus
import com.garmin.fit.DateTime
import com.garmin.fit.DeveloperDataIdMesg
import com.garmin.fit.DeveloperField
import com.garmin.fit.DeviceInfoMesg
import com.garmin.fit.FieldDescriptionMesg
import com.garmin.fit.FileEncoder
import com.garmin.fit.FileIdMesg
import com.garmin.fit.Fit
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.Gender
import com.garmin.fit.Manufacturer
import com.garmin.fit.MonitoringMesg
import com.garmin.fit.RecordMesg
import com.garmin.fit.UserProfileMesg
import java.io.File
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Example demonstrating how to encode FIT files.
 *
 *
 * The example creates 3 sample FIT files.
 */
object EncodeExample {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            System.out.printf(
                "FIT Encode Example Application - Protocol %d.%d Profile %d.%d %s\n",
                Fit.PROTOCOL_VERSION_MAJOR,
                Fit.PROTOCOL_VERSION_MINOR,
                Fit.PROFILE_VERSION_MAJOR,
                Fit.PROFILE_VERSION_MINOR,
                Fit.PROFILE_TYPE
            )

            encodeExampleSettings()
            encodeExampleMonitoring()
            encodeExampleActivity()
        } catch (e: Exception) {
            println("Exception encoding file: " + e.message)
            e.printStackTrace()
        }
    }

    private fun encodeExampleActivity() {
        println("Encode Example Activity FIT File")

        val encode: FileEncoder?

        try {
            encode = FileEncoder(File("ExampleActivity.fit"), Fit.ProtocolVersion.V2_0)
        } catch (e: FitRuntimeException) {
            System.err.println("Error opening file ExampleActivity.fit")
            return
        }

        //Generate FileIdMessage
        val fileIdMesg =
            FileIdMesg() // Every FIT file MUST contain a 'File ID' message as the first message
        fileIdMesg.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMesg.type = com.garmin.fit.File.ACTIVITY
        fileIdMesg.product = 1
        fileIdMesg.serialNumber = 12345L

        encode.write(fileIdMesg) // Encode the FileIDMesg

        val appId = byteArrayOf(
            0x1, 0x1, 0x2, 0x3,
            0x5, 0x8, 0xD, 0x15,
            0x22, 0x37, 0x59, 0x90.toByte(),
            0xE9.toByte(), 0x79, 0x62, 0xDB.toByte()
        )

        val developerIdMesg = DeveloperDataIdMesg()
        for (i in appId.indices) {
            developerIdMesg.setApplicationId(i, appId[i])
        }
        developerIdMesg.developerDataIndex = 0.toShort()
        encode.write(developerIdMesg)

        val fieldDescMesg = FieldDescriptionMesg()
        fieldDescMesg.developerDataIndex = 0.toShort()
        fieldDescMesg.fieldDefinitionNumber = 0.toShort()
        fieldDescMesg.fitBaseTypeId = Fit.BASE_TYPE_SINT8.toShort()
        fieldDescMesg.setFieldName(0, "doughnuts_earned")
        fieldDescMesg.setUnits(0, "doughnuts")
        encode.write(fieldDescMesg)

        val hrFieldDescMesg = FieldDescriptionMesg()
        hrFieldDescMesg.developerDataIndex = 0.toShort()
        hrFieldDescMesg.fieldDefinitionNumber = 1.toShort()
        hrFieldDescMesg.fitBaseTypeId = Fit.BASE_TYPE_UINT8.toShort()
        hrFieldDescMesg.setFieldName(0, "hr")
        hrFieldDescMesg.setUnits(0, "bpm")
        hrFieldDescMesg.nativeFieldNum = RecordMesg.HeartRateFieldNum.toShort()
        encode.write(hrFieldDescMesg)

        val record = RecordMesg()
        val doughnutsEarnedField = DeveloperField(fieldDescMesg, developerIdMesg)
        val hrDevField = DeveloperField(hrFieldDescMesg, developerIdMesg)
        record.addDeveloperField(doughnutsEarnedField)
        record.addDeveloperField(hrDevField)

        record.heartRate = 140.toShort()
        hrDevField.value = 140.toShort()
        record.cadence = 88.toShort()
        record.distance = 510f
        record.speed = 2800f
        doughnutsEarnedField.value = 1
        encode.write(record)

        record.heartRate = Fit.UINT8_INVALID
        hrDevField.value = 143.toShort()
        record.cadence = 90.toShort()
        record.distance = 2080f
        record.speed = 2920f
        doughnutsEarnedField.value = 2
        encode.write(record)

        record.heartRate = 144.toShort()
        hrDevField.value = 144.toShort()
        record.cadence = 92.toShort()
        record.distance = 3710f
        record.speed = 3050f
        doughnutsEarnedField.value = 3
        encode.write(record)

        try {
            encode.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing encode.")
            return
        }

        println("Encoded FIT file ExampleActivity.fit.")
    }

    private fun encodeExampleSettings() {
        println("Encode Example Settings FIT File")
        val encode: FileEncoder?

        try {
            encode = FileEncoder(File("ExampleSettings.fit"), Fit.ProtocolVersion.V1_0)
        } catch (e: FitRuntimeException) {
            System.err.println("Error opening file ExampleSettings.fit")
            return
        }

        //Generate FileIdMessage
        val fileIdMesg =
            FileIdMesg() // Every FIT file MUST contain a 'File ID' message as the first message
        fileIdMesg.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMesg.type = com.garmin.fit.File.SETTINGS
        fileIdMesg.product = 1
        fileIdMesg.serialNumber = 12345L

        encode.write(fileIdMesg) // Encode the FileIDMesg

        //Generate UserProfileMesg
        val userProfileMesg = UserProfileMesg()
        userProfileMesg.gender = Gender.FEMALE
        userProfileMesg.weight = 63.1f
        userProfileMesg.age = 99.toShort()
        userProfileMesg.friendlyName = "TestUser"

        encode.write(userProfileMesg) // Encode the UserProfileMesg

        try {
            encode.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing encode.")
            return
        }

        println("Encoded FIT file ExampleSettings.fit.")
    }

    private fun encodeExampleMonitoring() {
        println("Encode Example Monitoring FIT File")

        // Dates to be used to generate some sample data
        var systemCurrentTime = Clock.System.now()

        val encode: FileEncoder?

        try {
            encode = FileEncoder(File("ExampleMonitoring.fit"), Fit.ProtocolVersion.V1_0)
        } catch (e: FitRuntimeException) {
            System.err.println("Error opening file ExampleMonitoring.fit")
            return
        }

        val fileIdMesg =
            FileIdMesg() // Every FIT file MUST contain a 'File ID' message as the first message
        fileIdMesg.timeCreated = DateTime(Clock.System.now())
        fileIdMesg.type = com.garmin.fit.File.MONITORING_B
        fileIdMesg.manufacturer = Manufacturer.DEVELOPMENT
        fileIdMesg.product = 1
        fileIdMesg.serialNumber = 12345L
        fileIdMesg.number = 0

        encode.write(fileIdMesg) // Encode the FileIDMesg

        val deviceInfoMesg = DeviceInfoMesg()
        deviceInfoMesg.timestamp = DateTime(systemCurrentTime)
        deviceInfoMesg.batteryStatus = BatteryStatus.GOOD

        encode.write(deviceInfoMesg) // Encode the DeviceInfoMesg

        val monitoringMesg = MonitoringMesg()

        // By default, each time a new message is written the Local Message Type 0 will be redefined to match the new message.
        // In this case,to avoid having a definition message each time there is a DeviceInfoMesg, we can manually set the Local Message Type of the MonitoringMessage to '1'.
        // By doing this we avoid an additional 7 definition messages in our FIT file.
        monitoringMesg.localNum = 1

        monitoringMesg.timestamp =
            (DateTime(systemCurrentTime)) // Initialise Timestamp to current time
        monitoringMesg.cycles = 0f //Initialise Cycles to 0

        for (i in 0..3) { // Each of these loops represent a quarter of a day

            for (j in 0..5) { // Each of these loops represent 1 hour
                monitoringMesg.timestamp = DateTime(systemCurrentTime)
                monitoringMesg.activityType =
                    ActivityType.WALKING // Setting this to WALKING will cause Cycles to be interpreted as steps
                monitoringMesg.cycles =
                    monitoringMesg.cycles!! + (Random.nextFloat() * 1000) // Cycles are accumulated (i.e. must be increasing)

                encode.write(monitoringMesg) // Encode the MonitoringMesg

                systemCurrentTime += 1.hours  // Add an hour to our contrived timestamp
            }

            deviceInfoMesg.timestamp = DateTime(systemCurrentTime)
            deviceInfoMesg.batteryStatus = BatteryStatus.GOOD

            encode.write(deviceInfoMesg) // Encode the DeviceInfoMesg
        }

        try {
            encode.close()
        } catch (e: FitRuntimeException) {
            System.err.println("Error closing encode.")
            return
        }

        println("Encoded FIT file ExampleMonitoring.fit.")
    }
}
