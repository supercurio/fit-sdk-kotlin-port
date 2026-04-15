/**////////////////////////////////////////////////////////////////////////////////////////// */ // Copyright 2026 Garmin International, Inc.
// Licensed under the Flexible and Interoperable Data Transfer (FIT) Protocol License; you
// may not use this file except in compliance with the Flexible and Interoperable Data
// Transfer (FIT) Protocol License.
/**////////////////////////////////////////////////////////////////////////////////////////// */
package com.garmin.fit.examples

import com.garmin.fit.CourseMesg
import com.garmin.fit.CoursePoint
import com.garmin.fit.CoursePointMesg
import com.garmin.fit.DateTime
import com.garmin.fit.Event
import com.garmin.fit.EventMesg
import com.garmin.fit.EventType
import com.garmin.fit.FileEncoder
import com.garmin.fit.FileIdMesg
import com.garmin.fit.Fit
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.LapMesg
import com.garmin.fit.Manufacturer
import com.garmin.fit.RecordMesg
import com.garmin.fit.Sport
import java.io.File
import java.util.Arrays

object EncodeCourse {
    const val PRODUCTID: Int = 0

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // Create the output stream
            val encode: FileEncoder?
            val filename = "CourseEncodeRecipe.fit"

            try {
                encode = FileEncoder(File(filename), Fit.ProtocolVersion.V2_0)
            } catch (e: FitRuntimeException) {
                System.err.println("Error opening file " + filename)
                e.printStackTrace()
                return
            }

            // Get a list of course points
            val courseData: MutableList<MutableMap<String?, Any?>> =
                coursePoints

            // Reference points for the course
            val firstRecord = courseData.get(0)
            val lastRecord = courseData.get(courseData.size - 1)
            val halfwayRecord: MutableMap<String?, Any?>? = courseData.get(courseData.size / 2)
            val startTimestamp = firstRecord.get("timestamp") as Int
            val endTimestamp = lastRecord.get("timestamp") as Int
            val startDateTime = DateTime(startTimestamp.toLong())
            val endDateTime = DateTime(endTimestamp.toLong())

            // Every FIT file MUST contain a 'File ID' message as the first message
            val fileIdMesg = FileIdMesg()
            fileIdMesg.setType(com.garmin.fit.File.COURSE)
            fileIdMesg.setManufacturer(Manufacturer.DEVELOPMENT)
            fileIdMesg.setProduct(PRODUCTID)
            fileIdMesg.setTimeCreated(startDateTime)
            fileIdMesg.setSerialNumber(12345L)
            encode.write(fileIdMesg)

            // Every FIT COURSE file MUST contain a Course message
            val courseMesg = CourseMesg()
            courseMesg.setName("Garmin Field Day")
            courseMesg.setSport(Sport.CYCLING)
            encode.write(courseMesg)

            // Every FIT COURSE file MUST contain a Lap message
            val lapMesg = LapMesg()
            lapMesg.setStartTime(startDateTime)
            lapMesg.setTimestamp(startDateTime)
            lapMesg.setTotalElapsedTime(endTimestamp.toFloat() - startTimestamp)
            lapMesg.setTotalTimerTime(endTimestamp.toFloat() - startTimestamp)
            lapMesg.setStartPositionLat(firstRecord.get("position_lat") as Int)
            lapMesg.setStartPositionLong(firstRecord.get("position_long") as Int)
            lapMesg.setEndPositionLat(lastRecord.get("position_lat") as Int)
            lapMesg.setEndPositionLong(lastRecord.get("position_long") as Int)
            lapMesg.setTotalDistance(lastRecord.get("distance") as Float)
            encode.write(lapMesg)

            // Timer Events are REQUIRED for FIT COURSE files
            val eventMesgStart = EventMesg()
            eventMesgStart.setTimestamp(startDateTime)
            eventMesgStart.setEvent(Event.TIMER)
            eventMesgStart.setEventType(EventType.START)
            encode.write(eventMesgStart)

            // Every FIT COURSE file MUST contain Record messages
            for (record in courseData) {
                val timestamp = record.get("timestamp") as Int
                val latitude = record.get("position_lat") as Int
                val longitude = record.get("position_long") as Int
                val distance = record.get("distance") as Float
                val speed = record.get("speed") as Float
                val altitude = record.get("altitude") as Float

                val recordMesg = RecordMesg()
                recordMesg.setTimestamp(DateTime(timestamp.toLong()))
                recordMesg.setPositionLat(latitude)
                recordMesg.setPositionLong(longitude)
                recordMesg.setDistance(distance)
                recordMesg.setSpeed(speed)
                recordMesg.setAltitude(altitude)
                encode.write(recordMesg)

                // Add a Course Point at the halfway point of the route
                if (record === halfwayRecord) {
                    val coursePointMesg = CoursePointMesg()
                    coursePointMesg.setTimestamp(DateTime(timestamp.toLong()))
                    coursePointMesg.setName("Halfway")
                    coursePointMesg.setType(CoursePoint.GENERIC)
                    coursePointMesg.setPositionLat(latitude)
                    coursePointMesg.setPositionLong(longitude)
                    coursePointMesg.setDistance(distance)
                    encode.write(coursePointMesg)
                }
            }

            // Timer Events are REQUIRED for FIT COURSE files
            val eventMesgStop = EventMesg()
            eventMesgStop.setTimestamp(endDateTime)
            eventMesgStop.setEvent(Event.TIMER)
            eventMesgStop.setEventType(EventType.STOP_ALL)
            encode.write(eventMesgStop)

            // Close the output stream
            try {
                encode.close()
            } catch (e: FitRuntimeException) {
                System.err.println("Error closing encode.")
                e.printStackTrace()
                return
            }

            println("Encoded FIT Course File " + filename)
        } catch (e: Exception) {
            println("Exception encoding course: " + e.message)
            e.printStackTrace()
        }
    }

    val coursePoints: MutableList<MutableMap<String?, Any?>>
        /**
         * Creates a list of example course points
         * Each course point contains a timestamp, latitude position,
         * longitude position, altitude, distance, and speed
         * 
         * @return a list of maps where each map is a course point.
         */
        get() {
            val point0: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262849)
                        put("position_lat", 463583114)
                        put("position_long", -1131028903)
                        put("altitude", 329f)
                        put("distance", 0f)
                        put("speed", 0f)
                    }
                }

            val point1: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262855)
                        put("position_lat", 463583127)
                        put("position_long", -1131031938)
                        put("altitude", 328.6f)
                        put("distance", 22.03f)
                        put("speed", 3.0f)
                    }
                }

            val point2: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262869)
                        put("position_lat", 463583152)
                        put("position_long", -1131038159)
                        put("altitude", 327.6f)
                        put("distance", 67.29f)
                        put("speed", 3.0f)
                    }
                }

            val point3: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262876)
                        put("position_lat", 463583164)
                        put("position_long", -1131041346)
                        put("altitude", 327f)
                        put("distance", 90.52f)
                        put("speed", 3.0f)
                    }
                }

            val point4: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262876)
                        put("position_lat", 463583164)
                        put("position_long", -1131041319)
                        put("altitude", 327f)
                        put("distance", 90.72f)
                        put("speed", 3.0f)
                    }
                }

            val point5: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262891)
                        put("position_lat", 463588537)
                        put("position_long", -1131041383)
                        put("altitude", 327f)
                        put("distance", 140.72f)
                        put("speed", 3.0f)
                    }
                }

            val point6: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262891)
                        put("position_lat", 463588549)
                        put("position_long", -1131041383)
                        put("altitude", 327f)
                        put("distance", 140.82f)
                        put("speed", 3.0f)
                    }
                }

            val point7: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262897)
                        put("position_lat", 463588537)
                        put("position_long", -1131038293)
                        put("altitude", 327.6f)
                        put("distance", 163.26f)
                        put("speed", 3.0f)
                    }
                }

            val point8: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262911)
                        put("position_lat", 463588512)
                        put("position_long", -1131032041)
                        put("altitude", 328.4f)
                        put("distance", 208.75f)
                        put("speed", 3.0f)
                    }
                }

            val point9: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262918)
                        put("position_lat", 463588499)
                        put("position_long", -1131028879)
                        put("altitude", 329f)
                        put("distance", 231.8f)
                        put("speed", 3.0f)
                    }
                }

            val point10: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262918)
                        put("position_lat", 463588499)
                        put("position_long", -1131028903)
                        put("altitude", 329f)
                        put("distance", 231.97f)
                        put("speed", 3.0f)
                    }
                }

            val point11: LinkedHashMap<String?, Any?> =
                object : LinkedHashMap<String?, Any?>() {
                    init {
                        put("timestamp", 961262933)
                        put("position_lat", 463583127)
                        put("position_long", -1131028903)
                        put("altitude", 329f)
                        put("distance", 281.96f)
                        put("speed", 3.0f)
                    }
                }

            val courseData: MutableList<MutableMap<String?, Any?>> =
                ArrayList<MutableMap<String?, Any?>>(
                    Arrays.asList<LinkedHashMap<String?, Any?>?>(
                        point0, point1, point2,
                        point3, point4, point5,
                        point6, point7, point8,
                        point9, point10, point11
                    )
                )
            return courseData
        }
}
