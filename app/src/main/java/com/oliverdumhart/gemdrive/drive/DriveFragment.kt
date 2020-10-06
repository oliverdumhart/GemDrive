package com.oliverdumhart.gemdrive.drive

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.oliverdumhart.android.gemdrive.database.DriveDatabase
import com.oliverdumhart.gemdrive.R
import com.oliverdumhart.gemdrive.databinding.DriveFragmentBinding
import com.oliverdumhart.gemdrive.location.LocationAdapter
import com.oliverdumhart.gemdrive.location.LocationListener


class DriveFragment : Fragment() {

    private lateinit var viewModel: DriveViewModel
    private lateinit var binding: DriveFragmentBinding
    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.drive_fragment,
            container,
            false
        )
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args by navArgs<DriveFragmentArgs>()

        val application = requireNotNull(activity).application

        val dataSource = DriveDatabase.getInstance(application).driveDao

        val viewModelFactory = DriveViewModelFactory(args.driveId, dataSource, args.editState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DriveViewModel::class.java)

        val adapter = LocationAdapter(requireContext(), LocationListener { location ->
            viewModel.onLocationClicked(location);
        })

        binding.locationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.locationsRecyclerView.adapter = adapter

        viewModel.locations.observe(this, Observer { locations ->
            adapter.setLocations(locations)
        })

        viewModel.eventAddLocation.observe(this, Observer { location ->
            if (location != null) {
                findNavController().navigate(
                    DriveFragmentDirections.actionDriveFragmentToLocationFragment(
                        location = location,
                        departureTimePickable = false,
                        noteVisible = false,
                        editState = viewModel.editState.value ?: false
                    )
                )
                viewModel.eventAddLocationCompleted()
            }
        })

        viewModel.eventUpdateLocation.observe(this, Observer { location ->
            if (location != null) {
                findNavController().navigate(
                    DriveFragmentDirections.actionDriveFragmentToLocationFragment(
                        location = location,
                        arrivalTimePickable = false,
                        locationVisible = false,
                        editState = viewModel.editState.value ?: false
                    )
                )
                viewModel.eventUpdateLocationCompleted()
            }
        })
        viewModel.eventEditLocation.observe(this, Observer { location ->
            if (location != null) {
                findNavController().navigate(
                    DriveFragmentDirections.actionDriveFragmentToLocationFragment(
                        location = location,
                        editState = viewModel.editState.value ?: false,
                        arrivalTimePickable = (location.arrivalTime != null),
                        departureTimePickable = (location.departureTime != null)
                    )
                )
                viewModel.eventEditLocationCompleted()
            }
        })

        viewModel.eventShowNoteDialog.observe(this, Observer { note ->
            if(note.isNotEmpty()){
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Notiz")
                    setMessage(note)
                    show()
                }
                viewModel.onEventShowNoteDialogCompleted()
            }
        })

        viewModel.eventShowDistanceDialog.observe(this, Observer { show ->
            if (show) {
                showDistanceDialog();
                viewModel.eventShowDistanceDialogCompleted()
            }
        })

        viewModel.title.observe(this, Observer { title ->
            (activity as AppCompatActivity).supportActionBar?.title = title
        })

        viewModel.menuItemFinishVisible.observe(this, Observer { visibility ->
            menu?.findItem(R.id.menu_item_finish_drive)?.isVisible = visibility
        })
        viewModel.menuItemEditVisible.observe(this, Observer { visibility ->
            menu?.findItem(R.id.menu_item_edit_drive)?.isVisible = visibility
        })
        viewModel.menuItemEditCompleteVisible.observe(this, Observer { visibility ->
            menu?.findItem(R.id.menu_item_edit_complete_drive)?.isVisible = visibility
        })

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showDistanceDialog() {

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setPadding(
            input.paddingLeft + 32,
            input.paddingTop,
            input.paddingRight + 32,
            input.paddingBottom
        )

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Gefahrene Kilometer")
            .setPositiveButton("Speichern") { dialog, whichButton ->
                val distance = input.text.toString()
                if (distance.isNotEmpty()) {
                    viewModel.onDistanceSaved(distance.toInt());
                } else {
                    Toast.makeText(requireContext(), "Bitte Kilometer eingeben", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            .setNegativeButton("Abbrechen", null)
            .setView(input)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.menu_drive, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_finish_drive -> {
            viewModel.onFinishDriveClicked()
            true
        }
        R.id.menu_item_edit_drive ->{
            viewModel.onEditDriveClicked()
            true
        }
        R.id.menu_item_edit_complete_drive ->{
            viewModel.onEditCompleteClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
