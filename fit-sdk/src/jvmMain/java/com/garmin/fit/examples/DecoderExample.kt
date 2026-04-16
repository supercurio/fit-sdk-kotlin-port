/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.BatteryStatus
import com.garmin.fit.Decoder
import com.garmin.fit.DeveloperFieldDescription
import com.garmin.fit.DeveloperFieldDescriptionListener
import com.garmin.fit.DeviceInfoMesg
import com.garmin.fit.Factory
import com.garmin.fit.Field
import com.garmin.fit.FileIdMesg
import com.garmin.fit.Fit
import com.garmin.fit.FitListener
import com.garmin.fit.Gender
import com.garmin.fit.Mesg
import com.garmin.fit.MesgDefinition
import com.garmin.fit.MesgDefinitionListener
import com.garmin.fit.MesgListener
import com.garmin.fit.MonitoringMesg
import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import com.garmin.fit.UserProfileMesg
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Consumer

object DecoderExample {
    @JvmStatic
    fun main(args: Array<String>) {
        System.out.printf(
            "FIT Decoder Example Application - Protocol %d.%d Profile %d.%d %s\n",
            Fit.PROTOCOL_VERSION_MAJOR,
            Fit.PROTOCOL_VERSION_MINOR,
            Fit.PROFILE_VERSION_MAJOR,
            Fit.PROFILE_VERSION_MINOR,
            Fit.PROFILE_TYPE
        )

        if (args.size != 1) {
            println("Usage: java -jar DecoderExample.jar <filename>")
            return
        }

        val bytes: ByteArray?

        try {
            bytes = Files.readAllBytes(Paths.get(args[0]))
        } catch (e: IOException) {
            throw RuntimeException("Error opening file " + args[0] + " [2]")
        }

        val decoder = Decoder(bytes)

        // Use a FitListener to capture all decoded messages in a FitMessages object
        val fitListener = FitListener()
        decoder.addListener(fitListener as MesgListener)
        decoder.addListener(fitListener as DeveloperFieldDescriptionListener)

        // Use a custom listener to process messages as they are being decoded, and to
        // capture message definitions and developer field descriptions
        val customListener = CustomListener()
        decoder.addListener(customListener as MesgListener)
        decoder.addListener(customListener as MesgDefinitionListener)

        // Use a MesgBroadcaster for easy integration with existing projects
        // MesgBroadcaster mesgBroadcaster = new MesgBroadcaster();
        // mesgBroadcaster.addListener((RecordMesgListener)customListener);
        // mesgBroadcaster.addListener((MesgDefinitionListener)customListener);
        // decoder.addListener((MesgListener) mesgBroadcaster);
        // decoder.addListener((MesgDefinitionListener)mesgBroadcaster);
        try {
            decoder.read()

            val fitMessages = fitListener.fitMessages

            fitMessages.getFileIdMesgs()
                .forEach(Consumer { obj: FileIdMesg? -> ::printFileIdMesg })
            fitMessages.getUserProfileMesgs()
                .forEach(Consumer { obj: UserProfileMesg? -> ::printUserProfileMesg })
            fitMessages.getDeviceInfoMesgs()
                .forEach(Consumer { obj: DeviceInfoMesg? -> ::printDeviceInfoMesg })
            fitMessages.getMonitoringMesgs()
                .forEach(Consumer { obj: MonitoringMesg? -> ::printMonitoringMesg })
            fitMessages.getRecordMesgs()
                .forEach(Consumer { obj: RecordMesg? -> ::printRecordMesg })
            fitMessages.getDeveloperFieldDescriptionMesgs()
                .forEach(Consumer { obj: DeveloperFieldDescription? -> ::printDeveloperFieldDescriptionMesg })

            println("Decoded FIT file " + args[0] + ".")
        } catch (e: Exception) {
            System.err.print("Exception decoding file: ")
            System.err.println(e.message)
        }
    }

    fun printFileIdMesg(mesg: FileIdMesg) {
        println("File ID:")

        if (mesg.type != null) {
            print("   Type: ")
            println(mesg.type!!.value)
        }

        if (mesg.manufacturer != null) {
            print("   Manufacturer: ")
            println(mesg.manufacturer)
        }

        if (mesg.product != null) {
            print("   Product: ")
            println(mesg.product)
        }

        if (mesg.serialNumber != null) {
            print("   Serial Number: ")
            println(mesg.serialNumber)
        }

        if (mesg.number != null) {
            print("   Number: ")
            println(mesg.number)
        }
    }

    fun printUserProfileMesg(mesg: UserProfileMesg) {
        println("User profile:")

        if (mesg.friendlyName != null) {
            print("   Friendly Name: ")
            println(mesg.friendlyName)
        }

        if (mesg.gender != null) {
            if (mesg.gender == Gender.MALE) {
                println("   Gender: Male")
            } else if (mesg.gender == Gender.FEMALE) {
                println("   Gender: Female")
            }
        }

        if (mesg.age != null) {
            print("   Age [years]: ")
            println(mesg.age)
        }

        if (mesg.weight != null) {
            print("   Weight [kg]: ")
            println(mesg.weight)
        }
    }

    fun printDeviceInfoMesg(mesg: DeviceInfoMesg) {
        println("Device info:")

        if (mesg.timestamp != null) {
            print("   Timestamp: ")
            println(mesg.timestamp)
        }

        if (mesg.batteryStatus != null) {
            print("   Battery status: ")

            when (mesg.batteryStatus) {
                BatteryStatus.CRITICAL -> println("Critical")
                BatteryStatus.GOOD -> println("Good")
                BatteryStatus.LOW -> println("Low")
                BatteryStatus.NEW -> println("New")
                BatteryStatus.OK -> println("OK")
                else -> println("Invalid")
            }
        }
    }

    fun printMonitoringMesg(mesg: MonitoringMesg) {
        println("Monitoring:")

        if (mesg.timestamp != null) {
            print("   Timestamp: ")
            println(mesg.timestamp)
        }

        if (mesg.activityType != null) {
            print("   Activity Type: ")
            println(mesg.activityType)
        }

        // Depending on the ActivityType, there may be Steps, Strokes, or Cycles present in the file
        if (mesg.steps != null) {
            print("   Steps: ")
            println(mesg.steps)
        } else if (mesg.strokes != null) {
            print("   Strokes: ")
            println(mesg.strokes)
        } else if (mesg.cycles != null) {
            print("   Cycles: ")
            println(mesg.cycles)
        }

        printDeveloperData(mesg)
    }

    fun printRecordMesg(mesg: RecordMesg) {
        println("Record:")

        printValues(mesg, RecordMesg.HeartRateFieldNum)
        printValues(mesg, RecordMesg.CadenceFieldNum)
        printValues(mesg, RecordMesg.DistanceFieldNum)
        printValues(mesg, RecordMesg.SpeedFieldNum)
        printValues(mesg, RecordMesg.EnhancedAltitudeFieldNum)

        printDeveloperData(mesg)
    }

    private fun printDeveloperData(mesg: Mesg) {
        for (field in mesg.developerFields) {
            if (field.numValues < 1) {
                continue
            }

            if (field.isDefined) {
                print("   " + field.name)

                if (field.units != null) {
                    print(" [" + field.units + "]")
                }

                print(": ")
            } else {
                print("   Undefined Field: ")
            }

            print(field.getValue(0))
            for (i in 1..<field.numValues) {
                print("," + field.getValue(i))
            }

            println()
        }
    }

    private fun printValues(mesg: Mesg, fieldNum: Int) {
        val fields = mesg.getOverrideField(fieldNum.toShort())
        val profileField = Factory.createField(mesg.num, fieldNum)
        var namePrinted = false

        for (field in fields) {
            if (!namePrinted) {
                println("   " + profileField.name + ":")
                namePrinted = true
            }

            if (field is Field) {
                println("      native: " + field.value)
            } else {
                println("      override: " + field!!.value)
            }
        }
    }

    fun printDeveloperFieldDescriptionMesg(desc: DeveloperFieldDescription) {
        println("Developer Field Description:")

        println("   App Id: " + desc.applicationId)
        println("   App Version: " + desc.applicationVersion)
        println("   Field Num: " + desc.fieldDefinitionNumber)
    }

    private class CustomListener : MesgListener, RecordMesgListener, MesgDefinitionListener {
        override fun onMesg(mesg: Mesg) {
            // TODO - Implement custom Mesg handling
        }

        override fun onMesg(mesg: RecordMesg) {
            // TODO - Implement custom RecordMesg handling
        }

        override fun onMesgDefinition(mesgDefn: MesgDefinition) {
            // TODO - Implement custom MesgDefinition handling
        }
    }
}