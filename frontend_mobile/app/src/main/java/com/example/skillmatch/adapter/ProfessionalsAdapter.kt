package com.example.skillmatch.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Professional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.skillmatch.utils.SessionManager

class ProfessionalsAdapter(
    private var professionals: List<Professional>,
    private val onProfessionalClicked: (Professional) -> Unit
) : RecyclerView.Adapter<ProfessionalsAdapter.ProfessionalViewHolder>() {

    companion object {
        private const val TAG = "ProfessionalsAdapter"
    }

    class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val initialCircle: TextView = itemView.findViewById(R.id.initialCircle)
        val professionalName: TextView = itemView.findViewById(R.id.professionalName)
        val professionalOccupation: TextView = itemView.findViewById(R.id.professionalOccupation)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val scheduleText: TextView = itemView.findViewById(R.id.scheduleText)
        val hoursText: TextView = itemView.findViewById(R.id.hoursText)
        val profileImage: ImageView? = itemView.findViewById(R.id.profileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professional, parent, false)
        return ProfessionalViewHolder(view)
    }

    // In the ProfessionalsAdapter class, update the onBindViewHolder method
    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val professional = professionals[position]

        // Set professional name
        holder.professionalName.text = "${professional.firstName} ${professional.lastName}"

        // Set occupation
        holder.professionalOccupation.text = professional.occupation

        // Set initial circle
        val initial = professional.firstName.firstOrNull()?.toString() ?: "?"
        holder.initialCircle.text = initial

        // Set rating
        val rating = professional.rating?.toFloat() ?: 0f
        holder.ratingBar.rating = rating
        holder.ratingText.text = String.format("%.1f", rating)

        // Fetch portfolio data to get services with availability info
        fetchPortfolioData(professional.id, holder)

        // Set click listener
        holder.itemView.setOnClickListener {
            onProfessionalClicked(professional)
        }
    }

    private fun fetchPortfolioData(professionalId: Long, holder: ProfessionalViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(holder.itemView.context)
                val token = sessionManager.getToken()
    
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is missing or empty")
                    withContext(Dispatchers.Main) {
                        setDefaultAvailability(holder, professionals[holder.adapterPosition])
                    }
                    return@launch
                }
    
                val response = ApiClient.apiService.getPortfolio("Bearer $token", professionalId.toString())
    
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val portfolio = response.body()!!
                        Log.d(TAG, "Portfolio data for professional $professionalId: ${portfolio.servicesOffered?.size ?: 0} services")
    
                        // Get availability info directly from portfolio
                        val daysAvailable = portfolio.daysAvailable
                        val time = portfolio.time
    
                        if (daysAvailable.isNotEmpty() || !time.isNullOrEmpty()) {
                            // Display days in a compact format
                            val daysText = formatDaysOfWeek(daysAvailable)
                            holder.scheduleText.text = daysText
                            holder.hoursText.text = time ?: "Not specified"
                        } else {
                            setDefaultAvailability(holder, professionals[holder.adapterPosition])
                        }
                    } else {
                        // Handle 404 error specifically
                        if (response.code() == 404) {
                            Log.w(TAG, "No portfolio found for professional $professionalId - this is normal for new users")
                        } else {
                            Log.e(TAG, "Portfolio fetch failed: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                        setDefaultAvailability(holder, professionals[holder.adapterPosition])
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching portfolio", e)
                withContext(Dispatchers.Main) {
                    setDefaultAvailability(holder, professionals[holder.adapterPosition])
                }
            }
        }
    }

    private fun formatDaysOfWeek(days: List<String>): String {
        if (days.isEmpty()) return "Not specified"

        // Sort days in correct order
        val orderedDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val sortedDays = days.sortedBy { orderedDays.indexOf(it) }

        // If all weekdays are present, show "Weekdays"
        val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        if (sortedDays.containsAll(weekdays) && sortedDays.size == 5) {
            return "Weekdays"
        }

        // If all days are present, show "All days"
        if (sortedDays.size == 7) {
            return "All days"
        }

        // Otherwise, show comma-separated list
        return sortedDays.joinToString(", ")
    }

    private fun setDefaultAvailability(holder: ProfessionalViewHolder, professional: Professional) {
        // Use professional's availability data if available
        if (professional.availableDays.isNotEmpty()) {
            holder.scheduleText.text = formatDaysOfWeek(professional.availableDays)
        } else {
            holder.scheduleText.text = "Not specified"
        }

        holder.hoursText.text = if (professional.availableHours.isNotEmpty()) {
            professional.availableHours
        } else {
            "Not specified"
        }
    }

    override fun getItemCount(): Int = professionals.size

    fun updateProfessionals(newProfessionals: List<Professional>) {
        professionals = newProfessionals
        notifyDataSetChanged()
    }
}