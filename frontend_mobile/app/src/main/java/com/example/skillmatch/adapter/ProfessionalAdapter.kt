package com.example.skillmatch.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.models.Professional
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfessionalAdapter(
    private val professionals: List<Professional>,
    private val onItemClick: (Professional) -> Unit
) : RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professional, parent, false)
        return ProfessionalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val professional = professionals[position]
        holder.bind(professional)
        holder.itemView.setOnClickListener { onItemClick(professional) }
    }

    override fun getItemCount(): Int = professionals.size

    class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val professionalAvatar: CircleImageView = itemView.findViewById(R.id.professionalAvatar)
        private val professionalName: TextView = itemView.findViewById(R.id.professionalName)
        private val professionalOccupation: TextView = itemView.findViewById(R.id.professionalOccupation)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val availableDays: TextView = itemView.findViewById(R.id.availableDays)
        private val availableHours: TextView = itemView.findViewById(R.id.availableHours)

        fun bind(professional: Professional) {
            professionalName.text = professional.name
            professionalOccupation.text = professional.occupation
            ratingBar.rating = professional.rating
            availableDays.text = "Available: ${professional.availableDays.joinToString(", ")}"
            availableHours.text = "Hours: ${professional.availableHours}"

            // Load avatar image
            professional.avatar?.let { avatarString ->
                try {
                    if (avatarString.startsWith("http")) {
                        // Load from URL
                        Picasso.get().load(avatarString).placeholder(R.drawable.user)
                            .error(R.drawable.user).into(professionalAvatar)
                    } else {
                        // Decode Base64 string
                        val imageBytes = Base64.decode(avatarString, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        professionalAvatar.setImageBitmap(decodedImage)
                    }
                } catch (e: Exception) {
                    professionalAvatar.setImageResource(R.drawable.user)
                }
            } ?: professionalAvatar.setImageResource(R.drawable.user)
        }
    }
}