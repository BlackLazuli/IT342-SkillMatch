package com.example.skillmatch.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.models.CommentResponse
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CommentAdapter(
    private var comments: List<CommentResponse>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<CommentResponse>) {
        comments = newComments
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorImage: CircleImageView = itemView.findViewById(R.id.authorImage)
        private val authorNameText: TextView = itemView.findViewById(R.id.authorNameText)
        private val commentDateText: TextView = itemView.findViewById(R.id.commentDateText)
        private val commentRatingBar: RatingBar = itemView.findViewById(R.id.commentRatingBar)
        private val commentMessageText: TextView = itemView.findViewById(R.id.commentMessageText)

        fun bind(comment: CommentResponse) {
            // Set author name and comment message
            authorNameText.text = comment.authorName
            commentMessageText.text = comment.message
            commentRatingBar.rating = comment.rating.toFloat()
            
            // Format and set date
            try {
                val timestamp = comment.timestamp
                val formattedDate = formatTimestamp(timestamp)
                commentDateText.text = formattedDate
            } catch (e: Exception) {
                commentDateText.text = "Unknown date"
                Log.e("CommentAdapter", "Error parsing date", e)
            }
            
            // Set author profile picture if available
            if (!comment.profilePicture.isNullOrEmpty()) {
                try {
                    val decodedBytes = Base64.decode(comment.profilePicture, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    authorImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("CommentAdapter", "Error decoding profile picture", e)
                }
            }
        }
        
        private fun formatTimestamp(timestamp: String): String {
            try {
                // Parse the timestamp string to a LocalDateTime
                val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
                val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                val dateTime = LocalDateTime.parse(timestamp, inputFormatter)
                return dateTime.format(outputFormatter)
            } catch (e: Exception) {
                // If parsing with LocalDateTime fails, try SimpleDateFormat as fallback
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = inputFormat.parse(timestamp)
                    return outputFormat.format(date!!)
                } catch (e2: Exception) {
                    Log.e("CommentAdapter", "Error formatting date", e2)
                    return timestamp
                }
            }
        }
    }
}