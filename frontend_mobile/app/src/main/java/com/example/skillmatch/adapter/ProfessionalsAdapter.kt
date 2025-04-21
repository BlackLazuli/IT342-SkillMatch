package com.example.skillmatch.adapter

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

class ProfessionalsAdapter(
    private var professionals: List<Professional>,
    private val onProfessionalClicked: (Professional) -> Unit
) : RecyclerView.Adapter<ProfessionalsAdapter.ProfessionalViewHolder>() {

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

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val professional = professionals[position]
        
        // Set professional name
        holder.professionalName.text = professional.getFullName()
        
        // Set occupation
        holder.professionalOccupation.text = professional.occupation
        
        // Set rating
        val rating = professional.rating?.toFloat() ?: 0f
        holder.ratingBar.rating = rating
        holder.ratingText.text = String.format("%.1f", rating)
        
        // Set schedule
        val daysText = if (professional.availableDays.isNotEmpty()) {
            professional.availableDays.joinToString(", ")
        } else {
            "Not specified"
        }
        holder.scheduleText.text = daysText
        
        // Set hours
        holder.hoursText.text = if (professional.availableHours.isNotEmpty()) {
            professional.availableHours
        } else {
            "Not specified"
        }
        
        // Set profile image or initial circle
        if (professional.profilePicture != null && holder.profileImage != null) {
            // If we have a profile image view and a profile picture URL
            Glide.with(holder.itemView.context)
                .load(ApiClient.BASE_URL + professional.profilePicture)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(holder.profileImage)
            
            // Hide the initial circle if we're showing the profile image
            holder.initialCircle.visibility = View.GONE
        } else {
            // Show the initial circle with the first letter of the name
            holder.initialCircle.text = professional.getInitial()
            holder.initialCircle.visibility = View.VISIBLE
            
            // Hide the profile image if it exists
            holder.profileImage?.visibility = View.GONE
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onProfessionalClicked(professional)
        }
    }

    override fun getItemCount(): Int = professionals.size

    fun updateProfessionals(newProfessionals: List<Professional>) {
        professionals = newProfessionals
        notifyDataSetChanged()
    }
}