package com.oliverdumhart.gemdrive.location

import android.app.TimePickerDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TimePicker
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.oliverdumhart.android.gemdrive.database.DriveDatabase
import com.oliverdumhart.gemdrive.R
import com.oliverdumhart.gemdrive.databinding.LocationFragmentBinding
import com.oliverdumhart.gemdrive.entities.Location
import kotlinx.android.synthetic.main.location_fragment.*
import java.io.InputStreamReader
import java.util.*


class LocationFragment : Fragment() {

    companion object {
        fun newInstance() = LocationFragment()
    }

    private lateinit var viewModel: LocationViewModel
    private lateinit var binding: LocationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.location_fragment,
            container,
            false
        )
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val locationFragmentArgs by navArgs<LocationFragmentArgs>()
        val application = requireNotNull(this.activity).application

        val dataSource = DriveDatabase.getInstance(application).driveDao

        val viewModelFactory = LocationViewModelFactory(
            dataSource,
            locationFragmentArgs.location ?: Location(),
            locationFragmentArgs.arrivalTimePickable,
            locationFragmentArgs.departureTimePickable,
            locationFragmentArgs.locationVisible,
            locationFragmentArgs.noteVisible,
            locationFragmentArgs.editState,
            locationFragmentArgs.drive
        )
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LocationViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.eventShowArrivalTimePicker.observe(this, Observer { show ->
            if (show) {
                val calendar = Calendar.getInstance()

                TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
                        //TODO: to optimize duration calculation set date to date of drive?
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        viewModel.onArrivalTimePicked(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        })

        viewModel.eventShowDepartureTimePicker.observe(this, Observer { show ->
            if (show) {
                val calendar = Calendar.getInstance()

                TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
                        //TODO: to optimize duration calculation set date to date of drive?
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        viewModel.onDepartureTimePicked(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        })

        viewModel.eventLocationSaveClicked.observe(this, Observer { driveId ->
            if (driveId != 0L) {
                findNavController().navigate(LocationFragmentDirections.actionLocationFragmentToDriveFragment(driveId, editState = viewModel.editState))
            }
        })

        viewModel.nameError.observe(this, Observer {
            location_auto_complete_text_view.error = it
        })

        setUpAutoComplete()
    }

    private fun setUpAutoComplete() {
        val inputStream = resources.openRawResource(R.raw.municipalities)
        val inputStreamReader = InputStreamReader(inputStream)
        val municipalities = mutableListOf<String>()
        municipalities.addAll(inputStreamReader.readLines())
        val autoCompleteAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_item,
            municipalities
        )
        location_auto_complete_text_view.setAdapter(autoCompleteAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_location, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_save -> {
            viewModel.onSaveClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
