package com.oliverdumhart.gemdrive.overview

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.oliverdumhart.android.gemdrive.database.DriveDatabase
import com.oliverdumhart.gemdrive.BuildConfig
import com.oliverdumhart.gemdrive.R
import com.oliverdumhart.gemdrive.databinding.MainFragmentBinding
import com.oliverdumhart.gemdrive.entities.Location
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: MainFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.main_fragment,
            container,
            false
        )
        setHasOptionsMenu(true)

        val application = requireNotNull(activity).application
        val dataSource = DriveDatabase.getInstance(application).driveDao

        val viewModelFactory = MainViewModelFactory(dataSource)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        binding.mainViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.eventShowDatePicker.observe(this, Observer { show ->
            if (show) {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    requireContext(),
                    DatePickerDialog.OnDateSetListener { datePicker: DatePicker, year: Int, month: Int, day: Int ->
                        viewModel.onDatePicked(year, month, day)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(
                        Calendar.DAY_OF_MONTH
                    )
                ).show()

                viewModel.onEventShowDatePickerComplete()
            }
        })

        viewModel.eventDatePickingFinished.observe(this, Observer { drive ->
            if (drive != null) {
                findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToLocationFragment(
                        drive = drive,
                        arrivalTimePickable = false
                    )
                )
                viewModel.onEventDatePickerFinishedComplete()
            }
        })

        viewModel.navigateToDriveEvent.observe(this, Observer { driveId ->
            if (driveId != null && driveId > 0) {
                findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToDriveFragment(
                        driveId
                    )
                )
                viewModel.doneNavigatingToDrive()
            }
        })

        val adapter = DriveAdapter(requireContext(), DriveListener { id ->
            viewModel.onDriveClicked(id)
        })

        viewModel.drives.observe(this, Observer { drives ->
            adapter.setDrives(drives)
        })

        viewModel.eventPickMonth.observe(this, Observer {
            if (it) {
                if (Build.VERSION.SDK_INT >= 23) {
                    val result = ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                    if (result == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            5089
                        )
                    } else {
                        viewModel.onWritePermissionGranted()
                    }
                } else {
                    viewModel.onWritePermissionGranted()
                }
                viewModel.onEventPickMonthCompleted()
            }
        })

        viewModel.eventShowCsvMonthDialog.observe(this, Observer { show ->
            if (show) {
                showMonthDialog()
                viewModel.onEventShowCsvMonthDialogComplete()
            }
        })

        viewModel.csvFileCreated.observe(this, Observer { resp ->
            if (resp != null) {
                if (resp.isNotEmpty()) {
                    Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        "Datei erstellt!",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Öffnen") {
                            val file = File(resp)
                            if (file.exists()) {
                                val intent = Intent(Intent.ACTION_VIEW)
                                val uri = FileProvider.getUriForFile(
                                    application.applicationContext,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    file
                                )
                                intent.setDataAndType(uri, application.contentResolver.getType(uri))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                        .setActionTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimary
                            )
                        )
                        .show()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Keine Einträge in diesem Monat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.csvFileCreatedComplete()
            }
        })


        binding.drivesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.drivesRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 5089 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.onWritePermissionGranted()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun showMonthDialog() {
        val listOfYears = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        for (i in 0..9) {
            listOfYears.add((calendar.get(Calendar.YEAR) - i).toString())
        }
        //listOfYears.reverse()

        val monthList = listOf(
            "Jänner",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember"
        )

        var listOfMonths = monthList.subList(
            0,
            Calendar.getInstance().get(Calendar.MONTH) + 1
        )

        val monthAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)
        monthAdapter.addAll(listOfMonths)
        val yearAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)
        yearAdapter.addAll(listOfYears)

        val view = View.inflate(requireContext(), R.layout.month_dialog, null)
        val monthSpinner = view.findViewById<Spinner>(R.id.month_spinner)
        monthSpinner.adapter = monthAdapter
        monthSpinner.setSelection(listOfMonths.size - 1)
        val yearSpinner = view.findViewById<Spinner>(R.id.year_spinner)
        yearSpinner.adapter = yearAdapter

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                //what if same item selected? duplicate?
                if (listOfYears.size > position) {
                    if (listOfYears[position] == Calendar.getInstance().get(Calendar.YEAR).toString()) {
                        listOfMonths = monthList.subList(
                            0,
                            Calendar.getInstance().get(Calendar.MONTH) + 1
                        )
                        monthAdapter.clear()
                        monthAdapter.addAll(listOfMonths)
                        monthAdapter.notifyDataSetChanged()
                    } else {
                        monthAdapter.clear()
                        monthAdapter.addAll(monthList)
                        monthAdapter.notifyDataSetChanged()
                    }
                }
                //else exception
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Monat auswählen")
            .setView(view)
            .setPositiveButton("Erstellen") { dialog, whichButton ->


                val selectedMonth =
                    view.findViewById<Spinner>(R.id.month_spinner).selectedItem as String
                val selectedYear =
                    view.findViewById<Spinner>(R.id.year_spinner).selectedItem as String

                val month = getMonthValue(selectedMonth)
                val year = selectedYear.toInt()
                val calendar = Calendar.getInstance()
                calendar.set(year, month, 1, 0, 0, 0)
                val timespanBegin = calendar.time.time
                calendar.set(
                    year,
                    month,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                    23,
                    59,
                    59
                )
                val timespanEnd = calendar.time.time

                if (month != -1) {
                    viewModel.createCsvFile(timespanBegin, timespanEnd)
                }
                //else exception
            }
            .show()
    }

    private fun getMonthValue(selectedMonth: String): Int {
        var month = -1
        when (selectedMonth) {
            "Jänner" -> month = 0
            "Februar" -> month = 1
            "März" -> month = 2
            "April" -> month = 3
            "Mai" -> month = 4
            "Juni" -> month = 5
            "Juli" -> month = 6
            "August" -> month = 7
            "September" -> month = 8
            "Oktober" -> month = 9
            "November" -> month = 10
            "Dezember" -> month = 11
        }
        return month
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_delete_entries -> {
            viewModel.onDeleteEntriesClicked()
            true
        }
        R.id.menu_item_create_csv -> {
            viewModel.onCreateCsvClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}