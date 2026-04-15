/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.BatteryStatus
import com.garmin.fit.Decode
import com.garmin.fit.DeveloperFieldDescription
import com.garmin.fit.DeveloperFieldDescriptionListener
import com.garmin.fit.DeviceInfoMesg
import com.garmin.fit.DeviceInfoMesgListener
import com.garmin.fit.Factory
import com.garmin.fit.Field
import com.garmin.fit.FileIdMesg
import com.garmin.fit.FileIdMesgListener
import com.garmin.fit.Fit
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.Gender
import com.garmin.fit.Mesg
import com.garmin.fit.MesgBroadcaster
import com.garmin.fit.MonitoringMesg
import com.garmin.fit.MonitoringMesgListener
import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import com.garmin.fit.UserProfileMesg
import com.garmin.fit.UserProfileMesgListener
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object DecodeExample {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val decode = Decode()
            //decode.skipHeader();        // Use on streams with no header and footer (stream contains FIT defn and data messages only)
            //decode.incompleteStream();  // This suppresses exceptions with unexpected eof (also incorrect crc)
            val mesgBroadcaster = MesgBroadcaster(decode)
            val listener = Listener()
            var `in`: FileInputStream?

            System.out.printf(
                "FIT Decode Example Application - Protocol %d.%d Profile %d.%d %s\n",
                Fit.PROTOCOL_VERSION_MAJOR,
                Fit.PROTOCOL_VERSION_MINOR,
                Fit.PROFILE_VERSION_MAJOR,
                Fit.PROFILE_VERSION_MINOR,
                Fit.PROFILE_TYPE
            )

            if (args.size != 1) {
                println("Usage: java -jar DecodeExample.jar <filename>")
                return
            }

            try {
                `in` = FileInputStream(args[0])
            } catch (e: IOException) {
                throw RuntimeException("Error opening file " + args[0] + " [1]")
            }

            try {
                if (!decode.checkFileIntegrity(`in` as InputStream)) {
                    throw RuntimeException("FIT file integrity failed.")
                }
            } catch (e: RuntimeException) {
                System.err.print("Exception Checking File Integrity: ")
                System.err.println(e.message)
                System.err.println("Trying to continue...")
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

            mesgBroadcaster.addListener(listener as FileIdMesgListener)
            mesgBroadcaster.addListener(listener as UserProfileMesgListener)
            mesgBroadcaster.addListener(listener as DeviceInfoMesgListener)
            mesgBroadcaster.addListener(listener as MonitoringMesgListener)
            mesgBroadcaster.addListener(listener as RecordMesgListener)

            decode.addListener(listener as DeveloperFieldDescriptionListener)

            try {
                decode.read(`in`, mesgBroadcaster, mesgBroadcaster)
            } catch (e: FitRuntimeException) {
                // If a file with 0 data size in it's header  has been encountered,
                // attempt to keep processing the file
                if (decode.getInvalidFileDataSize()) {
                    decode.nextFile()
                    decode.read(`in`, mesgBroadcaster, mesgBroadcaster)
                } else {
                    System.err.print("Exception decoding file: ")
                    System.err.println(e.message)

                    try {
                        `in`.close()
                    } catch (f: IOException) {
                        throw RuntimeException(f)
                    }

                    return
                }
            }

            try {
                `in`.close()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            println("Decoded FIT file " + args[0] + ".")
        } catch (e: Exception) {
            println("Exception decoding file: " + e.message)
            e.printStackTrace()
        }
    }

    private class Listener : FileIdMesgListener, UserProfileMesgListener, DeviceInfoMesgListener,
        MonitoringMesgListener, RecordMesgListener, DeveloperFieldDescriptionListener {
        override fun onMesg(mesg: FileIdMesg) {
            println("File ID:")

            if (mesg.getType() != null) {
                print("   Type: ")
                println(mesg.getType().getValue())
            }

            if (mesg.getManufacturer() != null) {
                print("   Manufacturer: ")
                println(mesg.getManufacturer())
            }

            if (mesg.getProduct() != null) {
                print("   Product: ")
                println(mesg.getProduct())
            }

            if (mesg.getSerialNumber() != null) {
                print("   Serial Number: ")
                println(mesg.getSerialNumber())
            }

            if (mesg.getNumber() != null) {
                print("   Number: ")
                println(mesg.getNumber())
            }
        }

        override fun onMesg(mesg: UserProfileMesg) {
            println("User profile:")

            if (mesg.getFriendlyName() != null) {
                print("   Friendly Name: ")
                println(mesg.getFriendlyName())
            }

            if (mesg.getGender() != null) {
                if (mesg.getGender() == Gender.MALE) {
                    println("   Gender: Male")
                } else if (mesg.getGender() == Gender.FEMALE) {
                    println("   Gender: Female")
                }
            }

            if (mesg.getAge() != null) {
                print("   Age [years]: ")
                println(mesg.getAge())
            }

            if (mesg.getWeight() != null) {
                print("   Weight [kg]: ")
                println(mesg.getWeight())
            }
        }

        override fun onMesg(mesg: DeviceInfoMesg) {
            println("Device info:")

            if (mesg.getTimestamp() != null) {
                print("   Timestamp: ")
                println(mesg.getTimestamp())
            }

            if (mesg.getBatteryStatus() != null) {
                print("   Battery status: ")

                when (mesg.getBatteryStatus()) {
                    BatteryStatus.CRITICAL -> println("Critical")
                    BatteryStatus.GOOD -> println("Good")
                    BatteryStatus.LOW -> println("Low")
                    BatteryStatus.NEW -> println("New")
                    BatteryStatus.OK -> println("OK")
                    else -> println("Invalid")
                }
            }
        }

        override fun onMesg(mesg: MonitoringMesg) {
            println("Monitoring:")

            if (mesg.getTimestamp() != null) {
                print("   Timestamp: ")
                println(mesg.getTimestamp())
            }

            if (mesg.getActivityType() != null) {
                print("   Activity Type: ")
                println(mesg.getActivityType())
            }

            // Depending on the ActivityType, there may be Steps, Strokes, or Cycles present in the file
            if (mesg.getSteps() != null) {
                print("   Steps: ")
                println(mesg.getSteps())
            } else if (mesg.getStrokes() != null) {
                print("   Strokes: ")
                println(mesg.getStrokes())
            } else if (mesg.getCycles() != null) {
                print("   Cycles: ")
                println(mesg.getCycles())
            }

            printDeveloperData(mesg)
        }

        override fun onMesg(mesg: RecordMesg) {
            println("Record:")

            printValues(mesg, RecordMesg.Companion.HeartRateFieldNum)
            printValues(mesg, RecordMesg.Companion.CadenceFieldNum)
            printValues(mesg, RecordMesg.Companion.DistanceFieldNum)
            printValues(mesg, RecordMesg.Companion.SpeedFieldNum)

            printDeveloperData(mesg)
        }

        fun printDeveloperData(mesg: Mesg) {
            for (field in mesg.getDeveloperFields()) {
                if (field.getNumValues() < 1) {
                    continue
                }

                if (field.isDefined()) {
                    print("   " + field.getName())

                    if (field.getUnits() != null) {
                        print(" [" + field.getUnits() + "]")
                    }

                    print(": ")
                } else {
                    print("   Undefined Field: ")
                }

                print(field.getValue(0))
                for (i in 1..<field.getNumValues()) {
                    print("," + field.getValue(i))
                }

                println()
            }
        }

        override fun onDescription(desc: DeveloperFieldDescription) {
            println("New Developer Field Description")
            println("   App Id: " + desc.getApplicationId())
            println("   App Version: " + desc.getApplicationVersion())
            println("   Field Num: " + desc.getFieldDefinitionNumber())
        }

        fun printValues(mesg: Mesg, fieldNum: Int) {
            val fields = mesg.getOverrideField(fieldNum.toShort())
            val profileField = Factory.createField(mesg.getNum(), fieldNum)
            var namePrinted = false

            if (profileField == null) {
                return
            }

            for (field in fields) {
                if (!namePrinted) {
                    println("   " + profileField.getName() + ":")
                    namePrinted = true
                }

                if (field is Field) {
                    println("      native: " + field.getValue())
                } else {
                    println("      override: " + field.getValue())
                }
            }
        }
    }
}
