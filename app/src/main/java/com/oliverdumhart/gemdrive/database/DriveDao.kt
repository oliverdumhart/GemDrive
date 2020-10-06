package com.oliverdumhart.android.gemdrive.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.DriveWithLocations
import com.oliverdumhart.gemdrive.entities.Location

@Dao
interface DriveDao {

    @Query("SELECT * FROM Drive ORDER BY date DESC")
    fun loadDrives(): LiveData<List<DriveWithLocations>>

    @Query("SELECT * FROM Drive WHERE id = :driveId")
    fun loadDriveById(driveId: Long): LiveData<DriveWithLocations>

    @Insert
    fun insertDrive(drive: Drive) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDrive(drive: Drive)

    @Delete
    fun deleteDrive(drive: Drive)

    @Insert
    fun insertLocation(location: Location)

    @Query("DELETE FROM Drive")
    fun deleteAllDrives()

    @Query("DELETE FROM Location")
    fun deleteAllLocations()

    @Update
    fun updateLocation(location: Location)

    @Query("SELECT * FROM Drive WHERE date BETWEEN :fromTimeSpan AND :toTimeSpan ORDER BY date ASC")
    fun loadDriveInTimespan(fromTimeSpan: Long, toTimeSpan: Long) : List<DriveWithLocations>

    @Query("UPDATE Location SET note = :note WHERE id = :locationId")
    fun updateLocationNote(locationId: Long, note: String)

    @Query("SELECT note FROM Location WHERE id = :locationId")
    fun loadLocationNote(locationId: Long): String

    @Query("SELECT * FROM Location WHERE id = :locationId LIMIT 1")
    fun getLocation(locationId: Long) : Location?

    @Query("UPDATE Drive SET distance = :distance WHERE id = :driveId")
    fun updateDistance(driveId: Long, distance: Int)
}