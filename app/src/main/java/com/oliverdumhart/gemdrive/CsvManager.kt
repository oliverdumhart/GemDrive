package com.oliverdumhart.gemdrive

import android.os.Environment
import com.oliverdumhart.gemdrive.entities.DriveWithLocations
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvManager {
    companion object {
        private fun convertToCsv(drive: DriveWithLocations): List<String> {
            val csvDrive = mutableListOf<String>()
            val sdfDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

            drive.locations.forEachIndexed { index, location ->
                if (index == 0) {
                    csvDrive.add(String.format("%s;%s;;%s;;\n", sdfDate.format(drive.drive.date), sdfTime.format(location.departureTime), location.name))
                } else {
                    val departureTime = location.departureTime
                    val arrivalTime = location.arrivalTime
                    var locationTimeString = ""
                    if (departureTime != null && arrivalTime != null) {
                        locationTimeString = sdfTime.format(arrivalTime - departureTime)
                    }

                    var arrivalTimeString = ""
                    if (arrivalTime != null) {
                        arrivalTimeString = sdfTime.format(arrivalTime)
                    }

                    var departureTimeString = ""
                    if (location.departureTime != null) {
                        departureTimeString = sdfTime.format(location.departureTime)
                    }

                    val csvLine: String
                    if (index == drive.locations.size - 1) {
                        csvLine = String.format(";%s;%s;%s;%s;%s;%d\n",
                                departureTimeString,
                                arrivalTimeString,
                                location.name,
                                locationTimeString,
                                location.note,
                                drive.drive.distance)
                    } else {
                        csvLine = String.format(";%s;%s;%s;%s;%s\n",
                                departureTimeString,
                                arrivalTimeString,
                                location.name,
                                locationTimeString,
                                location.note)
                    }

                    csvDrive.add(csvLine)
                }
            }

            return csvDrive
        }

        fun createCsvFile(drives: List<DriveWithLocations>, filenamePostfix: String) {
            val filename = Environment.getExternalStorageDirectory().toString() + "/GemDrive/Drives" + filenamePostfix + ".csv"
            val folder = File(Environment.getExternalStorageDirectory().toString() + "/GemDrive")
            var folderExisting = folder.exists();
            if (!folderExisting) {
                folderExisting = folder.mkdir()
                // TODO: if result is false check for permission granted on phone
            }
            if (folderExisting) {
                val file = File(filename)
                var fileExisting = file.exists()
                if (fileExisting) {
                    fileExisting = !file.delete()
                }
                if (!fileExisting) {

                    file.createNewFile()
                    //because createNewFile() returns false
                    //TODO: FileWriter optimaler nutzen
                    fileExisting = file.exists()

                    if (fileExisting) {
                        val writer = FileWriter(filename)

                        writer.append("Datum;Uhrzeit (Abfahrt);Uhrzeit (Ankunft);Ort;Leistungsschein;Notiz;Distanz(km)\n")
                        drives.forEach { drive ->
                            val csvDrive = convertToCsv(drive)
                            csvDrive.forEach { csvLine ->
                                writer.append(csvLine)
                            }
                        }
                        writer.close()
                    }
                    // TODO: else warning

                }
                // TODO: else warning
            }
            // TODO: else warning
        }
    }
}