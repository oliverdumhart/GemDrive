package com.oliverdumhart.gemdrive.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.oliverdumhart.android.gemdrive.database.DriveDao
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.Location
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LocationViewModel(
    private val database: DriveDao,
    private var location: Location,
    val arrivalTimeVisible: Boolean,
    val departureTimeVisible: Boolean,
    val locationVisible: Boolean,
    val noteVisible: Boolean,
    val editState: Boolean,
    private val drive: Drive?
) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _arrivalTime = MutableLiveData<Long>()
    val arrivalTimeString: LiveData<String> = Transformations.map(_arrivalTime, ::toTimeFormat)

    private val _departureTime = MutableLiveData<Long>()
    val departureTimeString: LiveData<String> = Transformations.map(_departureTime, ::toTimeFormat)

    val name = MutableLiveData<String>()
    val note = MutableLiveData<String>()

    init {
        //Load existing location or create a new instance
        /*if (location.id > 0) {
            uiScope.launch {
                var location: Location? = null
                withContext(Dispatchers.IO) {
                    location = database.getLocation(locationId) ?: Location()
                }
                location?.let{
                    location = it
                    setLocation(it)
                }
            }
        } else {
            location = Location()
            setLocation(location)
        }*/
        setLocation(location);

    }

    fun setLocation(location: Location){
        _arrivalTime.value = location.arrivalTime ?: System.currentTimeMillis()
        _departureTime.value = location.departureTime ?: System.currentTimeMillis()
        name.value = location.name
        note.value = location.note
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        private const val SHORTCUT_HOME = "Home"
        private const val SHORTCUT_COMPANY = "Firma"
    }


    private fun toTimeFormat(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.GERMANY).format(Date(millis))
    }

    private val _eventShowArrivalTimePicker = MutableLiveData(false)
    val eventShowArrivalTimePicker: LiveData<Boolean>
        get() = _eventShowArrivalTimePicker

    fun onArrivalTimeClicked() {
        _eventShowArrivalTimePicker.value = true
    }

    fun onArrivalTimePicked(time: Long) {
        _arrivalTime.value = time
    }

    private val _eventShowDepartureTimePicker = MutableLiveData(false)
    val eventShowDepartureTimePicker: LiveData<Boolean>
        get() = _eventShowDepartureTimePicker

    fun onDepartureTimeClicked() {
        _eventShowDepartureTimePicker.value = true
    }

    fun onDepartureTimePicked(time: Long) {
        _departureTime.value = time
    }

    fun onHomeButtonClicked() {
        name.value = SHORTCUT_HOME
    }

    fun onCompanyButtonClicked() {
        name.value = SHORTCUT_COMPANY
    }

    private val _eventLocationSaveClicked = MutableLiveData<Long>()
    val eventLocationSaveClicked: LiveData<Long>
        get() = _eventLocationSaveClicked

    val nameError: LiveData<String> = Transformations.map(name){
        if(it.isEmpty() || it.isBlank()){
            "Bitte Ort angeben!"
        }
        else{
            null
        }
    }
    private val _nameError = MutableLiveData<String>("")
    fun onSaveClicked() {
        if(nameError.value == null){
            saveLocationToDatabase();
        }
    }

    private fun saveLocationToDatabase() {
        uiScope.launch {
            //Set updated values to location
            if(arrivalTimeVisible) {
                location.arrivalTime = _arrivalTime.value
            }
            if(departureTimeVisible) {
                location.departureTime = _departureTime.value
            }
            location.name = name.value ?: ""
            location.note = note.value ?: ""

            withContext(Dispatchers.IO) {
                if(drive != null){
                    location.driveId = database.insertDrive(drive)
                }

                if (location.id > 0) {
                    database.updateLocation(location)
                } else {
                    database.insertLocation(location)
                }
            }

            _eventLocationSaveClicked.value = location.driveId
        }
    }
}
