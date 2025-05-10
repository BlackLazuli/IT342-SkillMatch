package com.example.skillmatch.professional


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.CommentResponse
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Service
import com.example.skillmatch.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PortfolioActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var backButton: ImageButton
    private lateinit var editPortfolioButton: Button
    private lateinit var profileImage: CircleImageView
    private lateinit var workExperienceText: TextView
    private lateinit var servicesContainer: LinearLayout
    private lateinit var pricingContainer: LinearLayout
    private lateinit var availableDaysText: TextView
    private lateinit var availableTimeText: TextView
    private lateinit var commentsContainer: LinearLayout
    private lateinit var overallRatingBar: RatingBar
    private lateinit var overallRatingText: TextView

    // Bottom navigation
    private lateinit var homeButton: ImageButton
    private lateinit var messagesButton: ImageButton
    private lateinit var settingsNavButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        editPortfolioButton = findViewById(R.id.editPortfolioButton)
        profileImage = findViewById(R.id.profileImage)
        workExperienceText = findViewById(R.id.workExperienceText)
        servicesContainer = findViewById(R.id.servicesContainer)
        pricingContainer = findViewById(R.id.pricingContainer)
        availableDaysText = findViewById(R.id.availableDaysText)
        availableTimeText = findViewById(R.id.availableTimeText)
        commentsContainer = findViewById(R.id.commentsContainer)
        overallRatingBar = findViewById(R.id.overallRatingBar)
        overallRatingText = findViewById(R.id.overallRatingText)

        // Initialize bottom navigation
        homeButton = findViewById(R.id.homeButton)
        messagesButton = findViewById(R.id.messagesButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)
        profileButton = findViewById(R.id.profileButton)

        setupListeners()
        fetchPortfolioData()
    }

    private fun setupListeners() {
        // Back button
        // In setupListeners() method
        backButton.setOnClickListener {
            // Replace onBackPressed() with finish()
            finish()
        }

        // Edit portfolio button
        editPortfolioButton.setOnClickListener {
            val intent = Intent(this, EditPortfolioActivity::class.java)
            startActivity(intent)
        }

        // Bottom navigation
        homeButton.setOnClickListener {
            val intent = Intent(this, AppointmentProfessionalActivity::class.java)
            startActivity(intent)
        }

        messagesButton.setOnClickListener {
            // Navigate to messages screen
            // Intent to messages activity
        }

        settingsNavButton.setOnClickListener {
            val intent = Intent(this, ProfessionalSettingsActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfessionalProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchPortfolioData() {
        val userId = sessionManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Add authentication token
                    val token = "Bearer ${sessionManager.getToken()}"
                    val response = ApiClient.apiService.getPortfolio(token, userId)

                    // Also fetch user data to get profile picture
                    val userResponse = ApiClient.apiService.getUserProfile(userId)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val portfolio = response.body()!!
                            displayPortfolioData(portfolio)

                            // Display profile picture if user data is available
                            if (userResponse.isSuccessful && userResponse.body() != null) {
                                val user = userResponse.body()!!
                                displayProfilePicture(user.profilePicture)
                            }
                            
                            // Fetch comments for this portfolio - use portfolio.id instead of userId
                            fetchComments(portfolio.id.toString())
                        } else {
                            // No portfolio found
                            Log.d("Portfolio", "No portfolio found")
                            Toast.makeText(
                                this@PortfolioActivity,
                                "No portfolio found. Please create one.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Portfolio", "Error fetching portfolio", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PortfolioActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    
    private fun fetchComments(portfolioId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = "Bearer ${sessionManager.getToken()}"
                val response = ApiClient.apiService.getCommentsByPortfolio(token, portfolioId)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val comments = response.body()!!
                        displayComments(comments)
                    } else {
                        Log.d("Portfolio", "No comments found or error fetching comments")
                    }
                }
            } catch (e: Exception) {
                Log.e("Portfolio", "Error fetching comments", e)
            }
        }
    }
    
    private fun displayComments(comments: List<CommentResponse>) {
        // Clear existing comments
        commentsContainer.removeAllViews()
        
        if (comments.isEmpty()) {
            val noCommentsText = TextView(this)
            noCommentsText.text = "No comments yet"
            noCommentsText.textSize = 14f
            noCommentsText.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            commentsContainer.addView(noCommentsText)
            
            // Set overall rating to 0
            overallRatingBar.rating = 0f
            overallRatingText.text = "0.0"
            return
        }
        
        // Calculate average rating
        val averageRating = comments.map { it.rating }.average().toFloat()
        overallRatingBar.rating = averageRating
        overallRatingText.text = String.format("%.1f", averageRating)
        
        // Add each comment to the container
        val inflater = LayoutInflater.from(this)
        
        for (comment in comments) {
            val commentView = inflater.inflate(R.layout.item_comment, commentsContainer, false)
            
            // Set comment data
            val authorImage = commentView.findViewById<CircleImageView>(R.id.authorImage)
            val authorNameText = commentView.findViewById<TextView>(R.id.authorNameText)
            val commentDateText = commentView.findViewById<TextView>(R.id.commentDateText)
            val commentRatingBar = commentView.findViewById<RatingBar>(R.id.commentRatingBar)
            val commentMessageText = commentView.findViewById<TextView>(R.id.commentMessageText)
            
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
                Log.e("Portfolio", "Error parsing date", e)
            }
            
            // Set author profile picture if available
            val backendBaseUrl = "http://3.107.23.86:8080"
            val profilePic = comment.profilePicture
            if (!profilePic.isNullOrEmpty()) {
                if (profilePic.startsWith("http")) {
                    Glide.with(this)
                        .load(profilePic)
                        .placeholder(R.drawable.user)
                        .into(authorImage)
                } else if (profilePic.startsWith("/uploads")) {
                    Glide.with(this)
                        .load(backendBaseUrl + profilePic)
                        .placeholder(R.drawable.user)
                        .into(authorImage)
                } else {
                    try {
                        val decodedBytes = Base64.decode(profilePic, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        authorImage.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("Portfolio", "Error decoding profile picture", e)
                    }
                }
            }
            
            // Add the comment view to the container
            commentsContainer.addView(commentView)
        }
    }
    
    private fun formatTimestamp(timestamp: String): String {
        try {
            // Parse the timestamp string to a LocalDateTime
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val dateTime = LocalDateTime.parse(timestamp, formatter)
            
            // Format to a more readable date
            val outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            return dateTime.format(outputFormatter)
        } catch (e: Exception) {
            Log.e("Portfolio", "Error formatting date", e)
            return "Unknown date"
        }
    }

    private fun displayProfilePicture(profilePic: String?) {
        profilePic?.let {
            val backendBaseUrl = "http://3.107.23.86:8080"
            if (profilePic.startsWith("http")) {
                Glide.with(this)
                    .load(profilePic)
                    .placeholder(R.drawable.user)
                    .into(profileImage)
            } else if (profilePic.startsWith("/uploads")) {
                Glide.with(this)
                    .load(backendBaseUrl + profilePic)
                    .placeholder(R.drawable.user)
                    .into(profileImage)
            } else {
                try {
                    val imageBytes = Base64.decode(profilePic, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("Portfolio", "Error loading profile image", e)
                }
            }
        }
    }

    private fun displayPortfolioData(portfolio: Portfolio) {
        // Set work experience
        workExperienceText.text = portfolio.workExperience ?: "No work experience added yet"
        
        // Set availability days
        // Display availability
        val daysText = formatDaysOfWeek(portfolio.daysAvailable)
        availableDaysText.text = daysText
        
        // Format time display
        if (portfolio.startTime != null && portfolio.endTime != null) {
            val formattedStartTime = formatTo12HourTime(portfolio.startTime)
            val formattedEndTime = formatTo12HourTime(portfolio.endTime)
            availableTimeText.text = "$formattedStartTime - $formattedEndTime"
        } else if (!portfolio.time.isNullOrEmpty()) {
            availableTimeText.text = portfolio.time
        } else {
            availableTimeText.text = "Not specified"
        }
        
        // Update services
        updateServicesUI(portfolio.servicesOffered)
        
        // Update availability days
        if (portfolio.daysAvailable.isNotEmpty()) {
            availableDaysText.text = portfolio.daysAvailable.joinToString(", ")
        } else {
            availableDaysText.text = "Not specified"
        }
        
        // Update availability time with better null/empty checking and logging
        // First check for startTime and endTime (new format)
        if (portfolio.startTime != null && portfolio.endTime != null) {
            val formattedStartTime = formatTo12HourTime(portfolio.startTime)
            val formattedEndTime = formatTo12HourTime(portfolio.endTime)
            availableTimeText.text = "$formattedStartTime - $formattedEndTime"
            Log.d("Portfolio", "Setting time text to: $formattedStartTime - $formattedEndTime")
        }
        // For backward compatibility, check time field
        else if (portfolio.time != null && portfolio.time.isNotEmpty()) {
            // Try to format the time string if it contains a range separator
            if (portfolio.time.contains("-")) {
                try {
                    val times = portfolio.time.split("-")
                    if (times.size == 2) {
                        val formattedStartTime = formatTo12HourTime(times[0].trim())
                        val formattedEndTime = formatTo12HourTime(times[1].trim())
                        availableTimeText.text = "$formattedStartTime - $formattedEndTime"
                    } else {
                        availableTimeText.text = portfolio.time
                    }
                } catch (e: Exception) {
                    Log.e("Portfolio", "Error formatting time range", e)
                    availableTimeText.text = portfolio.time
                }
            } else {
                availableTimeText.text = portfolio.time
            }
            Log.d("Portfolio", "Setting time text to: ${availableTimeText.text}")
        } else {
            availableTimeText.text = "Not specified"
            Log.d("Portfolio", "Time value is null or empty")
        }
    }

    private fun updateServicesUI(services: List<Service>?) {
        // Clear existing views
        servicesContainer.removeAllViews()
        
        if (services.isNullOrEmpty()) {
            // Add a message when no services are available
            val noServicesText = TextView(this).apply {
                text = "No services available"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setPadding(16, 16, 16, 16)
            }
            servicesContainer.addView(noServicesText)
            return
        }
        
        // Display each service in its own container
        services.forEach { service ->
            // Create a card for each service
            val serviceCard = androidx.cardview.widget.CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16) // Add bottom margin
                }
                radius = resources.getDimension(R.dimen.card_corner_radius) ?: 8f
                cardElevation = 2f
            }
            
            // Create container for service content
            val serviceContent = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(resources.getColor(R.color.colorPrimary, theme))
            }
            
            // Add service name
            val serviceNameText = TextView(this).apply {
                text = service.name
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.white, theme))
                setPadding(0, 0, 0, 8)
            }
            serviceContent.addView(serviceNameText)
            
            // Add service description if available
            if (!service.description.isNullOrEmpty()) {
                val descriptionText = TextView(this).apply {
                    text = service.description
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.white, theme))
                    setPadding(0, 0, 0, 8)
                }
                serviceContent.addView(descriptionText)
            }
            
            // Add service pricing if available
            if (!service.pricing.isNullOrEmpty()) {
                val pricingText = TextView(this).apply {
                    text = "Price: ₱${service.pricing}"
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.white, theme))
                    setPadding(0, 8, 0, 0)
                }
                serviceContent.addView(pricingText)
            }
            
            // Add the content to the card
            serviceCard.addView(serviceContent)
            
            // Add the card to the services container
            servicesContainer.addView(serviceCard)
        }
    }

    // Remove this method as it's not needed
    private fun updateCommentsUI(comments: List<CommentResponse>?) {
    // This method is redundant since we have the displayComments method
    // that is called directly after fetching comments
    }

    // Add helper method to format time to 12-hour format with AM/PM
    private fun formatTo12HourTime(timeString: String): String {
        try {
            // Parse the time string (assuming it's in HH:mm or HH:mm:ss format)
            val time = if (timeString.contains(":")) {
                if (timeString.count { it == ':' } > 1) {
                    // Format is HH:mm:ss
                    LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("HH:mm:ss"))
                } else {
                    // Format is HH:mm
                    LocalTime.parse(timeString.trim(), DateTimeFormatter.ofPattern("HH:mm"))
                }
            } else {
                LocalTime.parse(timeString.trim())
            }
            
            // Format to 12-hour format with AM/PM
            return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        } catch (e: Exception) {
            Log.e("PortfolioActivity", "Error formatting time: $timeString", e)
            return timeString // Return original if parsing fails
        }
    }

    private fun formatDaysOfWeek(days: List<String>): String {
        if (days.isEmpty()) return "Not specified"
        
        // Sort days in correct order (Monday first)
        val sortedDays = days.sortedBy { 
            when (it.trim().uppercase()) {
                "MONDAY" -> 1
                "TUESDAY" -> 2
                "WEDNESDAY" -> 3
                "THURSDAY" -> 4
                "FRIDAY" -> 5
                "SATURDAY" -> 6
                "SUNDAY" -> 7
                else -> 8
            }
        }
        
        return sortedDays.joinToString(", ")
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        fetchPortfolioData()
    }
}