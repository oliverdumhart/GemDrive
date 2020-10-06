package com.oliverdumhart.android.gemdrive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.Location

@Database(entities = [Drive::class, Location::class], version = 1)
abstract class DriveDatabase : RoomDatabase() {

    abstract val driveDao: DriveDao

    companion object {
        @Volatile
        private var INSTANCE: DriveDatabase? = null;

        fun getInstance(context: Context): DriveDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DriveDatabase::class.java,
                        "gemdrive"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}