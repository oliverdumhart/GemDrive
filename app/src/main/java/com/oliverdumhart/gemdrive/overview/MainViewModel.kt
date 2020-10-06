package com.oliverdumhart.gemdrive.overview

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.oliverdumhart.android.gemdrive.database.DriveDao
import com.oliverdumhart.gemdrive.CsvManager
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.DriveWithLocations
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val database: DriveDao) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    val drives = database.loadDrives()

    private val _eventShowDatePicker = MutableLiveData<Boolean>(false)
    val eventShowDatePicker: LiveData<Boolean>
        get() = _eventShowDatePicker

    private val _eventDatePickingFinished = MutableLiveData<Drive?>(null)
    val eventDatePickingFinished: LiveData<Drive?>
        get() = _eventDatePickingFinished

    fun onCreateDriveClicked() {
        _eventShowDatePicker.value = true
    }

    fun onDatePicked(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        /*val drive = Drive(date = calendar.timeInMillis)
        uiScope.launch {
            var driveId: Long = 0
            withContext(Dispatchers.IO) {
                driveId = database.insertDrive(drive)
            }
            this@MainViewModel._eventDatePickingFinished.value = driveId
        }*/
        /*_eventDatePickingFinished.value = DriveWithLocations().apply {
            this.drive = drive
        }*/
        _eventDatePickingFinished.value = Drive().apply { date = calendar.timeInMillis }
    }

    fun onEventShowDatePickerComplete() {
        _eventShowDatePicker.value = false
    }

    fun onEventDatePickerFinishedComplete() {
        _eventDatePickingFinished.value = null
    }

    private val _navigateToDriveEvent = MutableLiveData<Long>()
    val navigateToDriveEvent: LiveData<Long>
        get() = _navigateToDriveEvent

    fun onDriveClicked(id: Long) {
        uiScope.launch {
            var drive: DriveWithLocations? = null
            withContext(Dispatchers.IO) {
                drive = database.loadDriveById(id).value
            }
            _navigateToDriveEvent.value = drive?.drive?.id ?: 0L
        }

        _navigateToDriveEvent.value = id
    }

    fun doneNavigatingToDrive() {
        _navigateToDriveEvent.value = null
    }

    fun onDeleteEntriesClicked() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.deleteAllDrives()
                database.deleteAllLocations()
            }
        }
    }

    private val _eventPickMonth = MutableLiveData(false)
    val eventPickMonth: LiveData<Boolean> get() = _eventPickMonth

    fun onCreateCsvClicked() {
        _eventPickMonth.value = true
    }

    fun onEventPickMonthCompleted() {
        _eventPickMonth.value = false
    }

    private val _eventShowCsvMonthDialog = MutableLiveData(false)
    val eventShowCsvMonthDialog: LiveData<Boolean> get() = _eventShowCsvMonthDialog


    fun onWritePermissionGranted() {
        _eventShowCsvMonthDialog.value = true
    }

    fun onEventShowCsvMonthDialogComplete() {
        _eventShowCsvMonthDialog.value = false
    }

    private val _csvFileCreated = MutableLiveData<String?>()
    val csvFileCreated: LiveData<String?> get() = _csvFileCreated

    fun csvFileCreatedComplete() {
        _csvFileCreated.value = null
    }

    fun createCsvFile(timespanBegin: Long, timespanEnd: Long) {
        uiScope.launch {
            var filename = ""
            withContext(Dispatchers.IO) {
                val drivesInTimeSpan =
                    database.loadDriveInTimespan(timespanBegin, timespanEnd)

                if (drivesInTimeSpan.isNotEmpty()) {
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val fileNamePostFix = "_" + sdf.format(Date())
                    CsvManager.createCsvFile(drivesInTimeSpan, fileNamePostFix)
                    filename =
                        Environment.getExternalStorageDirectory().toString() + "/GemDrive/Drives" + fileNamePostFix + ".csv"
                }
            }
            _csvFileCreated.value = filename
        }
    }
}