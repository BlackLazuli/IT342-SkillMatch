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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                        val startTime = portfolio.startTime
                        val endTime = portfolio.endTime
                        val time = portfolio.time

                        // Display days in a compact format
                        val daysText = formatDaysOfWeek(daysAvailable)
                        holder.scheduleText.text = daysText
                        
                        // First check for startTime and endTime (new format)
                        if (startTime != null && endTime != null) {
                            val formattedStartTime = formatTo12HourTime(startTime)
                            val formattedEndTime = formatTo12HourTime(endTime)
                            holder.hoursText.text = "$formattedStartTime - $formattedEndTime"
                            Log.d(TAG, "Using startTime-endTime for professional $professionalId: $formattedStartTime - $formattedEndTime")
                        } else if (!time.isNullOrEmpty()) {
                            // For backward compatibility, use time field
                            // Try to format the time string if it contains a range separator
                            if (time.contains("-")) {
                                try {
                                    val times = time.split("-")
                                    if (times.size == 2) {
                                        val formattedStartTime = formatTo12HourTime(times[0].trim())
                                        val formattedEndTime = formatTo12HourTime(times[1].trim())
                                        holder.hoursText.text = "$formattedStartTime - $formattedEndTime"
                                    } else {
                                        holder.hoursText.text = time
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error formatting time range", e)
                                    holder.hoursText.text = time
                                }
                            } else {
                                holder.hoursText.text = time
                            }
                            Log.d(TAG, "Using time for professional $professionalId: ${holder.hoursText.text}")
                        } else {
                            // If no time information is available
                            holder.hoursText.text = "Not specified"
                            Log.d(TAG, "No time information for professional $professionalId")
                        }

                        // Fetch comments to calculate overall rating
                        portfolio.id?.let { portfolioId ->
                            fetchCommentsAndUpdateRating(portfolioId, holder)
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

    private fun fetchCommentsAndUpdateRating(portfolioId: Long, holder: ProfessionalViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(holder.itemView.context)
                val token = sessionManager.getToken()
                
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is missing or empty")
                    return@launch
                }
                
                val commentsResponse = ApiClient.apiService.getCommentsByPortfolio(
                    "Bearer $token",
                    portfolioId.toString()
                )
                
                withContext(Dispatchers.Main) {
                    if (commentsResponse.isSuccessful && commentsResponse.body() != null) {
                        val comments = commentsResponse.body()!!
                        
                        if (comments.isNotEmpty()) {
                            // Calculate average rating from comments
                            val averageRating = comments.map { it.rating }.average().toFloat()
                            holder.ratingBar.rating = averageRating
                            holder.ratingText.text = String.format("%.1f", averageRating)
                        } else {
                            // No comments, set rating to 0
                            holder.ratingBar.rating = 0f
                            holder.ratingText.text = "0.0"
                        }
                    } else {
                        // Set rating to 0 on error
                        holder.ratingBar.rating = 0f
                        holder.ratingText.text = "0.0"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments", e)
                withContext(Dispatchers.Main) {
                    // Set rating to 0 on error
                    holder.ratingBar.rating = 0f
                    holder.ratingText.text = "0.0"
                }
            }
        }
    }

    // Add a helper method to format time to 12-hour format with AM/PM
    private fun formatTo12HourTime(timeString: String): String {
        try {
            // Parse the time string (handling both HH:mm and HH:mm:ss formats)
            val time = if (timeString.count { it == ':' } > 1) {
                // Format is HH:mm:ss
                LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("HH:mm:ss"))
            } else {
                // Format is HH:mm
                LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("HH:mm"))
            }
            
            // Format to 12-hour format with AM/PM
            return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: $timeString", e)
            return timeString // Return original if parsing fails
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