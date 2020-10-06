package com.oliverdumhart.gemdrive.drive

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oliverdumhart.android.gemdrive.database.DriveDao
import com.oliverdumhart.android.gemdrive.database.DriveDatabase
import com.oliverdumhart.gemdrive.entities.Location

class DriveViewModelFactory(
    private val driveId : Long,
    private val database : DriveDao,
    private val editState: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriveViewModel::class.java)) {
            return DriveViewModel(driveId, database, editState) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}