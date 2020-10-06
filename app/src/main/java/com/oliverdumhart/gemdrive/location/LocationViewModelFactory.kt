package com.oliverdumhart.gemdrive.location

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oliverdumhart.android.gemdrive.database.DriveDao
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.Location

class LocationViewModelFactory(
    private val database: DriveDao,
    private val location: Location,
    private val arrivalTimePickable: Boolean,
    private val departureTimePickable: Boolean,
    private val locationVisible: Boolean,
    private val noteVisible: Boolean,
    private val editState: Boolean,
    private val drive: Drive?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(database, location, arrivalTimePickable, departureTimePickable, locationVisible, noteVisible, editState, drive) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}