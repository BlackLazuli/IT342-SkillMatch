package com.example.skillmatch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.AppointmentResponse
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentAdapter(
    private var appointments: List<AppointmentResponse>,
    private val listener: AppointmentClickListener
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    // Cache for portfolio data to avoid repeated API calls
    private val portfolioCache = mutableMapOf<Long, Portfolio?>()

    interface AppointmentClickListener {
        fun onAppointmentClick(appointment: AppointmentResponse)
        fun onRateAppointmentClick(appointment: AppointmentResponse)
    }

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.appointmentCard)
        val statusText: TextView = view.findViewById(R.id.appointmentStatusText)
        val dateTimeText: TextView = view.findViewById(R.id.appointmentDateTimeText)
        val providerNameText: TextView = view.findViewById(R.id.providerNameText)
        val notesText: TextView = view.findViewById(R.id.appointmentNotesText)
        val rateButton: TextView? = view.findViewById(R.id.rateButton)
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
        
        // Get the current user's role from SessionManager
        val sessionManager = SessionManager(holder.itemView.context)
        val userRole = sessionManager.getUserRole()
        
        // Set the appropriate name based on user role
        if (userRole == "SERVICE PROVIDER" || userRole == "PROFESSIONAL") {
            // For service providers, show the customer's name
            holder.providerNameText.text = "With: ${appointment.userFirstName ?: ""} ${appointment.userLastName ?: ""}"
        } else {
            // For customers, show the professional's name if available, otherwise fetch it
            if (appointment.providerFirstName != null && appointment.providerLastName != null) {
                holder.providerNameText.text = "With: ${appointment.providerFirstName} ${appointment.providerLastName}"
            } else if (appointment.portfolioId != null) {
                // If provider name is not available but we have portfolioId, fetch the info
                fetchProfessionalInfo(appointment.portfolioId, holder.providerNameText)
            } else {
                // Fallback to showing just the ID
                holder.providerNameText.text = "With: Professional (ID: ${appointment.portfolioId})"
            }
        }
        
        // Set notes (if any)
        if (appointment.notes.isNullOrEmpty()) {
            holder.notesText.visibility = View.GONE
        } else {
            holder.notesText.visibility = View.VISIBLE
            holder.notesText.text = appointment.notes
        }
        
        // Show rate button only for completed appointments that haven't been rated yet
        holder.rateButton?.let {
            if (appointment.status == "COMPLETED" && appointment.rated != true) {
                it.visibility = View.VISIBLE
                it.setOnClickListener { _ ->
                    listener.onRateAppointmentClick(appointment)
                }
            } else {
                it.visibility = View.GONE
            }
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            listener.onAppointmentClick(appointment)
        }
    }

    private fun fetchProfessionalInfo(portfolioId: Long, textView: TextView) {
        // Check if we already have this portfolio in cache
        if (portfolioCache.containsKey(portfolioId)) {
            updateProviderNameText(portfolioCache[portfolioId], portfolioId, textView)
            return
        }
        
        // Fetch all portfolios and find the one we need
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getAllPortfolios()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val portfolios = response.body()
                        // Find the portfolio with matching ID
                        val portfolio = portfolios?.find { it.id == portfolioId }
                        // Cache the result
                        portfolioCache[portfolioId] = portfolio
                        // Update the UI
                        updateProviderNameText(portfolio, portfolioId, textView)
                    } else {
                        // If API call fails, show portfolio ID
                        textView.text = "With: Professional #$portfolioId"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // If exception occurs, show portfolio ID
                    textView.text = "With: Professional #$portfolioId"
                }
            }
        }
    }
    
    private fun updateProviderNameText(portfolio: Portfolio?, portfolioId: Long, textView: TextView) {
        if (portfolio?.user != null) {
            val firstName = portfolio.user.firstName ?: ""
            val lastName = portfolio.user.lastName ?: ""
            textView.text = "With: $firstName $lastName"
        } else {
            textView.text = "With: Professional #$portfolioId"
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