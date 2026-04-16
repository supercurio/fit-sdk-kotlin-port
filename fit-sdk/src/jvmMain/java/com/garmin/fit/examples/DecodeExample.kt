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
            var   inputStream: FileInputStream?

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
                  inputStream = FileInputStream(args[0])
            } catch (e: IOException) {
                throw RuntimeException("Error opening file " + args[0] + " [1]")
            }

            try {
                if (!decode.checkFileIntegrity(  inputStream as InputStream)) {
                    throw RuntimeException("FIT file integrity failed.")
                }
            } catch (e: RuntimeException) {
                System.err.print("Exception Checking File Integrity: ")
                System.err.println(e.message)
                System.err.println("Trying to continue...")
            } finally {
                try {
                      inputStream.close()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            try {
                  inputStream = FileInputStream(args[0])
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
                decode.read(  inputStream, mesgBroadcaster, mesgBroadcaster)
            } catch (e: FitRuntimeException) {
                // If a file with 0 data size in it's header  has been encountered,
                // attempt to keep processing the file
                if (decode.invalidFileDataSize) {
                    decode.nextFile()
                    decode.read(  inputStream, mesgBroadcaster, mesgBroadcaster)
                } else {
                    System.err.print("Exception decoding file: ")
                    System.err.println(e.message)

                    try {
                          inputStream.close()
                    } catch (f: IOException) {
                        throw RuntimeException(f)
                    }

                    return
                }
            }

            try {
                  inputStream.close()
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

            if (mesg.type != null) {
                print("   Type: ")
                println(mesg.type?.value)
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

        override fun onMesg(mesg: UserProfileMesg) {
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

        override fun onMesg(mesg: DeviceInfoMesg) {
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

        override fun onMesg(mesg: MonitoringMesg) {
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

        override fun onMesg(mesg: RecordMesg) {
            println("Record:")

            printValues(mesg, RecordMesg.HeartRateFieldNum)
            printValues(mesg, RecordMesg.CadenceFieldNum)
            printValues(mesg, RecordMesg.DistanceFieldNum)
            printValues(mesg, RecordMesg.SpeedFieldNum)

            printDeveloperData(mesg)
        }

        fun printDeveloperData(mesg: Mesg) {
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

        override fun onDescription(desc: DeveloperFieldDescription) {
            println("New Developer Field Description")
            println("   App Id: " + desc.applicationId)
            println("   App Version: " + desc.applicationVersion)
            println("   Field Num: " + desc.fieldDefinitionNumber)
        }

        fun printValues(mesg: Mesg, fieldNum: Int) {
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
                    println("      override: " + field?.value)
                }
            }
        }
    }
}
