package com.oliverdumhart.gemdrive.location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.oliverdumhart.gemdrive.R
import com.oliverdumhart.gemdrive.entities.Location
import kotlinx.android.synthetic.main.item_location.view.*
import java.text.SimpleDateFormat

class LocationAdapter(val context: Context, val itemClickListener: LocationListener) :
    RecyclerView.Adapter<LocationAdapter.LocationAdapterViewHolder>() {

    private var locations: List<Location> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapterViewHolder {
        return LocationAdapterViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_location,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return locations.count();
    }

    override fun onBindViewHolder(holder: LocationAdapterViewHolder, position: Int) {
        val sdf = SimpleDateFormat("HH:mm")
        holder.nameTextView!!.text = locations.get(position).name
        val departureTime = locations.get(position).departureTime
        if (departureTime != null) {
            holder.departureTimeTextView!!.text = sdf.format(departureTime)

            if (holder.departureLineView.visibility == View.INVISIBLE) {
                holder.departureLineView.visibility = View.VISIBLE
            }
            if (holder.departureTimeTextView.visibility == View.GONE) {
                holder.departureTimeTextView.visibility = View.VISIBLE
            }
        } else {
            holder.departureLineView.visibility = View.INVISIBLE
            holder.departureTimeTextView.visibility = View.GONE
        }
        val arrivalTime = locations.get(position).arrivalTime
        if (arrivalTime != null) {
            holder.arrivalTimeTextView!!.text = sdf.format(arrivalTime)

            if (holder.arrivalLineView.visibility == View.INVISIBLE) {
                holder.arrivalLineView.visibility = View.VISIBLE
            }
            if (holder.arrivalTimeTextView.visibility == View.GONE) {
                holder.arrivalTimeTextView.visibility = View.VISIBLE
            }
        } else {
            holder.arrivalLineView.visibility = View.INVISIBLE
            holder.arrivalTimeTextView.visibility = View.GONE
        }
        holder.noteIcon.isVisible = (locations[position].note != "")
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(locations[position])
        }
    }

    fun setLocations(locations: List<Location>) {
        this.locations = locations
        notifyDataSetChanged()
    }

    class LocationAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView = view.location_name
        val departureTimeTextView = view.location_departure_time
        val arrivalTimeTextView = view.location_arrival_time
        val arrivalLineView = view.line_arrival
        val departureLineView = view.line_departure
        val noteIcon = view.note_icon
    }
}

class LocationListener(val clickListener: (location: Location) -> Unit) {
    fun onClick(location: Location) = clickListener(location)
}