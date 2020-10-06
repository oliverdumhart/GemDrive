package com.oliverdumhart.gemdrive.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import java.text.SimpleDateFormat

class DriveWithLocations() {

    @Embedded
    var drive: Drive = Drive()

    @Relation(parentColumn = "id", entityColumn = "driveId", entity = Location::class)
    var locations: List<Location> = listOf()

    @Ignore
    fun getTimeSpanString(): String {
        //TODO: overthink first() and last() everywhere
        val sdfTime = SimpleDateFormat("HH:mm")
        var startTimeString = ""
        var endTimeString = ""
        if (locations.isNotEmpty()) {
            val startTime = locations.first().departureTime
            val endTime = locations.last().arrivalTime

            if (startTime != null) {
                startTimeString = sdfTime.format(startTime)
            }
            if (endTime != null) {
                endTimeString = sdfTime.format(endTime)
            }
        }

        return String.format("%s - %s", startTimeString, endTimeString)
    }

    @Ignore
    fun getLocationsString(): String {
        var locationsString = ""

        //TODO: implement again
        locations.forEachIndexed { index, location ->
            if (index != 0) {
                locationsString += " - "
            }
            locationsString += location.name
        }

        return locationsString
    }

    @Ignore
    fun isDriving(): Boolean {
        return locations.isNotEmpty() && locations.last().departureTime == null
    }
}