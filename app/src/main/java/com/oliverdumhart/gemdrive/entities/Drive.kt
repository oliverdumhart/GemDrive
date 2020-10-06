package com.oliverdumhart.gemdrive.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

@Entity
@Parcelize
data class Drive(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo
    var date: Long = -1,
    @ColumnInfo
    var distance: Int = -1
) : Parcelable {

    @Ignore
    fun getDateString(): String {
        val sdfDate = SimpleDateFormat("EE, dd.MM.yyyy", DateFormatSymbols(Locale.GERMAN))
        return sdfDate.format(date)
    }

    @Ignore
    // TODO: getter to get properties
    fun getDistanceString(): String {
        return String.format("%d km", distance)
    }

}