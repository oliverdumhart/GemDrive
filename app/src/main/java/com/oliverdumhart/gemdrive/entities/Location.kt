package com.oliverdumhart.gemdrive.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.concurrent.TimeUnit

@Entity
@Parcelize
data class Location(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo
    var name: String = "",
    @ColumnInfo
    var driveId: Long = 0L,
    @ColumnInfo
    var arrivalTime: Long? = null,
    @ColumnInfo
    var departureTime: Long? = null,
    @ColumnInfo
    var note: String = ""
) : Parcelable {
/*
    val locationTime: Date?
        get() {
            // TODO: timeZones?
            // TODO: General departureTime arrivalTime vergleichen auf größer/kleiner
            var time: Date? = null;
            if (arrivalTime != null && departureTime != null) {
                // TODO: 1 hour substraction hack? because 1 additional shown
                // TODO: Zeitbegrenzung auf heutigen tag

                val departureCalendar = Calendar.getInstance()
                departureCalendar.time = departureTime!!
                departureCalendar.set(Calendar.SECOND, 0)
                departureCalendar.set(Calendar.MILLISECOND, 0)

                val arrivalCalendar = Calendar.getInstance()
                arrivalCalendar.time = arrivalTime!!
                arrivalCalendar.set(Calendar.SECOND, 0)
                arrivalCalendar.set(Calendar.MILLISECOND, 0)

                time = Date(departureCalendar.timeInMillis - arrivalCalendar.timeInMillis - TimeUnit.HOURS.toMillis(1));
            }

            return time;
        }*/
}