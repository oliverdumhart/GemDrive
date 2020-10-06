package com.oliverdumhart.gemdrive.drive

import androidx.lifecycle.*
import com.oliverdumhart.android.gemdrive.database.DriveDao
import com.oliverdumhart.gemdrive.entities.Location
import kotlinx.coroutines.*

class DriveViewModel(
    private val driveId: Long,
    private val database: DriveDao,
    private val initialEditState: Boolean
) : ViewModel() {

    val drive = database.loadDriveById(driveId)


    val editState: LiveData<Boolean> get() = _editState

    private val _editState = MutableLiveData<Boolean>(initialEditState)

    private val createState = MediatorLiveData<Boolean>().apply {
        addSource(drive) {
            this.value = it.drive.distance == -1 && _editState.value == false
        }
        addSource(_editState) {
            this.value = (drive.value?.drive?.distance ?: -1) == -1 && it == false
        }
    }

    private val finishedState = MediatorLiveData<Boolean>().apply {
        addSource(createState) {
            this.value = it == false && _editState.value == false
        }
        addSource(_editState) {
            this.value = createState.value == false && it == false
        }
    }

    val title = Transformations.map(drive) {
        it.drive.getDateString()
    }

    val locations = Transformations.map(drive) {
        it.locations
    }

    val distance = Transformations.map(drive) {
        it.drive.distance
    }

    val distanceVisible = Transformations.map(distance) {
        it > 0
    }

    private var _driveContinue: LiveData<Boolean> = Transformations.map(locations) {
        it.last().departureTime == null
    }

    val menuItemFinishVisible: LiveData<Boolean>
        get() = _menuItemFinishVisible
    private val _menuItemFinishVisible = MediatorLiveData<Boolean>().apply {
        addSource(createState) {
            this.value = it && _driveContinue.value ?: false
        }
        addSource(_driveContinue) {
            this.value = it && createState.value == true
        }
    }

    val menuItemEditVisible: LiveData<Boolean>
        get() = finishedState

    val menuItemEditCompleteVisible: LiveData<Boolean>
        get() = _menuItemEditCompleteVisible
    private val _menuItemEditCompleteVisible = MediatorLiveData<Boolean>().apply {
        addSource(_editState) {
            this.value = it && _driveContinue.value ?: false
        }
        addSource(_driveContinue) {
            this.value = _editState.value == true && it
        }
    }

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    val actionButtonText = Transformations.map(_driveContinue) { driveContinue ->
        if (driveContinue) "Weiterfahren" else "Nächster Halt"
    }

    /*val actionButtonVisible: LiveData<Boolean>
        get() = _actionButtonVisible
    private val _actionButtonVisible = MediatorLiveData<Boolean>().apply {
        addSource(_editState) {
            this.value = it || !(driveFinished.value ?: true)
        }
        addSource(driveFinished) {
            this.value = !it || _editState.value ?: false
        }
    }*/

    private val _eventAddLocation = MutableLiveData<Location?>()
    val eventAddLocation: LiveData<Location?>
        get() = _eventAddLocation

    private val _eventUpdateLocation = MutableLiveData<Location?>()
    val eventUpdateLocation: LiveData<Location?>
        get() = _eventUpdateLocation

    fun onActionButtonClicked() {
        if (_driveContinue.value == true) {
            _eventUpdateLocation.value = locations.value!!.last()
        } else {
            _eventAddLocation.value = Location().apply { driveId = drive.value!!.drive.id }
        }
    }

    private val _eventEditLocation = MutableLiveData<Location>()
    val eventEditLocation: LiveData<Location>
        get() = _eventEditLocation

    private val _eventShowNoteDialog = MutableLiveData<String>()
    val eventShowNoteDialog: LiveData<String>
        get() = _eventShowNoteDialog

    fun onLocationClicked(location: Location) {
        if (_editState.value == true || createState.value == true) {
            _eventEditLocation.value = location
        } else if (finishedState.value == true && location.note != "") {
            _eventShowNoteDialog.value = location.note
        }
    }

    fun onEventShowNoteDialogCompleted() {
        _eventShowNoteDialog.value = ""
    }

    fun eventAddLocationCompleted() {
        _eventAddLocation.value = null
    }

    fun eventUpdateLocationCompleted() {
        _eventUpdateLocation.value = null
    }

    private val _eventShowDistanceDialog = MutableLiveData<Boolean>()
    val eventShowDistanceDialog: LiveData<Boolean>
        get() = _eventShowDistanceDialog


    val actionButtonVisible: LiveData<Boolean>
        get() = _actionButtonVisible
    private val _actionButtonVisible = MediatorLiveData<Boolean>().apply {
        addSource(finishedState) {
            this.value = !(it ?: false) || _editState.value ?: false
        }
        addSource(_editState) {
            this.value = it ?: false || !(finishedState.value ?: true)
        }
    }

    fun onFinishDriveClicked() {
        _eventShowDistanceDialog.value = true
    }


    fun onDistanceSaved(distance: Int) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.updateDistance(driveId, distance)
            }
            //HIDE MENU ITEM AND SHOW LIST ITEM
        }
    }

    fun onDistanceClicked() {
        if (_editState.value == true || createState.value == true) {
            _eventShowDistanceDialog.value = true
        }
    }

    fun onEditDriveClicked() {
        _editState.value = true
    }

    fun onEditCompleteClicked() {
        _editState.value = false
    }

    fun eventEditLocationCompleted() {
        _eventEditLocation.value = null
    }

    fun eventShowDistanceDialogCompleted() {
        _eventShowDistanceDialog.value = false
    }
}
