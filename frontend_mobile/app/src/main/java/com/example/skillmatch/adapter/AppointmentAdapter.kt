package com.example.skillmatch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.models.AppointmentResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentAdapter(
    private var appointments: List<AppointmentResponse>,
    private val listener: AppointmentClickListener
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    interface AppointmentClickListener {
        fun onAppointmentClick(appointment: AppointmentResponse)
    }

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.appointmentCard)
        val statusText: TextView = view.findViewById(R.id.appointmentStatusText)
        val dateTimeText: TextView = view.findViewById(R.id.appointmentDateTimeText)
        val providerNameText: TextView = view.findViewById(R.id.providerNameText)
        val notesText: TextView = view.findViewById(R.id.appointmentNotesText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        
        // Set status with appropriate color
        holder.statusText.text = appointment.status
        when (appointment.status) {
            "SCHEDULED" -> {
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            "RESCHEDULED" -> {
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            }
            "CANCELED" -> {
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
            "COMPLETED" -> {
                holder.statusText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
            }
        }
        
        // Format and set date/time
        holder.dateTimeText.text = formatDateTime(appointment.appointmentTime)
        
        // Set provider name
        holder.providerNameText.text = "With: ${appointment.userFirstName} ${appointment.userLastName}"
        
        // Set notes (if any)
        if (appointment.notes.isNullOrEmpty()) {
            holder.notesText.visibility = View.GONE
        } else {
            holder.notesText.visibility = View.VISIBLE
            holder.notesText.text = appointment.notes
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            listener.onAppointmentClick(appointment)
        }
    }

    override fun getItemCount() = appointments.size

    fun updateAppointments(newAppointments: List<AppointmentResponse>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

    private fun formatDateTime(dateTimeString: String): String {
        try {
            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
            val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
            return dateTime.format(outputFormatter)
        } catch (e: Exception) {
            return dateTimeString
        }
    }
}