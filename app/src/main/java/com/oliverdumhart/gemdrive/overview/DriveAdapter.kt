package com.oliverdumhart.gemdrive.overview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oliverdumhart.gemdrive.R
import com.oliverdumhart.gemdrive.entities.Drive
import com.oliverdumhart.gemdrive.entities.DriveWithLocations
import kotlinx.android.synthetic.main.item_drive.view.*

class DriveAdapter(val context: Context, private val clickListener: DriveListener) : RecyclerView.Adapter<DriveAdapter.DriveAdapterViewHolder>() {

    private var drives: List<DriveWithLocations> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriveAdapterViewHolder {
        return DriveAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.item_drive, parent, false))
    }

    override fun getItemCount() = drives.size

    override fun onBindViewHolder(holder: DriveAdapterViewHolder, position: Int) {
        val drive = drives[position]
        holder.dateTextView?.text = drive.drive.getDateString()
        holder.timeTextView?.text = drive.getTimeSpanString()
        holder.driveTextView?.text = drive.getLocationsString()
        holder.distanceTextView?.text = if (drive.drive.distance > 0) drive.drive.getDistanceString() else ""
        holder.itemView.setOnClickListener {
            clickListener.onClick(drive.drive.id)
        }
    }

    fun setDrives(drives: List<DriveWithLocations>?) {
        if (drives != null) {
            this.drives = drives
            notifyDataSetChanged()
        }
    }

    class DriveAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView = view.dateTextView
        val timeTextView = view.timeTextView
        val driveTextView = view.driveTextView
        val distanceTextView = view.distanceTextView
    }
}

class DriveListener(val clickListener: (driveId: Long) -> Unit) {
    fun onClick(driveId: Long) = clickListener(driveId)
}