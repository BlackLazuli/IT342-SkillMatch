package com.example.skillmatch

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import android.widget.RatingBar
import androidx.core.widget.NestedScrollView
import com.example.skillmatch.api.ApiService
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.User
import com.example.skillmatch.models.Comment
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerViewCard : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    
    // UI Elements
    private lateinit var profileInitial: TextView
    private lateinit var professionalInitial: TextView
    private lateinit var professionalName: TextView
    private lateinit var professionalOccupation: TextView
    private lateinit var professionalRating: RatingBar
    private lateinit var availableDays: TextView
    private lateinit var availableHours: TextView
    private lateinit var locationAddress: TextView
    private lateinit var commentsContainer: LinearLayout
    
    // Navigation Icons
    private lateinit var homeIcon: ImageView
    private lateinit var appointmentsIcon: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var profileIcon: ImageView
    
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customerviewcard)
        
        // Initialize API service and session manager
        apiService = ApiClient.getApiService(this)
        sessionManager = SessionManager(this)
        
        // Get user ID from intent (this is the ID of the service provider)
        userId = intent.getStringExtra("USER_ID")
        
        // Initialize UI elements
        initializeViews()
        
        // Set up navigation
        setupNavigation()
        
        // Load professional data
        userId?.let {
            loadServiceProviderData(it)
        } ?: run {
            // Handle case when user ID is not provided
            showError("User ID not found")
        }
    }
    
    private fun initializeViews() {
        try {
            // Initialize header elements
            profileInitial = findViewById(R.id.profileInitial)
            
            // Initialize professional info elements
            professionalInitial = findViewById(R.id.professionalInitial)
            professionalName = findViewById(R.id.professionalName)
            professionalOccupation = findViewById(R.id.professionalOccupation)
            professionalRating = findViewById(R.id.professionalRating)
            availableDays = findViewById(R.id.availableDays)
            availableHours = findViewById(R.id.availableHours)
            
            // Initialize location elements
            locationAddress = findViewById(R.id.locationAddress)
            
            // Initialize comments container - fixed to handle potential layout issues
            val commentsScrollView = findViewById<NestedScrollView>(R.id.commentsContainer)
            commentsContainer = commentsScrollView.getChildAt(0) as LinearLayout
            
            // Initialize navigation icons
            homeIcon = findViewById(R.id.homeIcon)
            appointmentsIcon = findViewById(R.id.appointmentsIcon)
            settingsIcon = findViewById(R.id.settingsIcon)
            profileIcon = findViewById(R.id.profileIcon)
            
            // Set user initial in profile circle
            val userFirstName = sessionManager.getUserFirstName() ?: ""
            if (userFirstName.isNotEmpty()) {
                profileInitial.text = userFirstName.first().toString()
            }
        } catch (e: Exception) {
            showError("Error initializing views: ${e.message}")
        }
    }
    
    private fun setupNavigation() {
        homeIcon.setOnClickListener {
            // Navigate to customer dashboard
            finish()
        }
        
        appointmentsIcon.setOnClickListener {
            // Navigate to appointments screen
            // Intent to AppointmentsActivity
        }
        
        settingsIcon.setOnClickListener {
            // Navigate to settings screen
            // Intent to SettingsActivity
        }
        
        profileIcon.setOnClickListener {
            // Navigate to profile screen
            // Intent to ProfileActivity
        }
    }
    
    private fun loadServiceProviderData(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch user data (service provider)
                val user = apiService.getUserById(userId)
                
                // Verify this user is a service provider
                if (user.role != "SERVICE_PROVIDER") {
                    withContext(Dispatchers.Main) {
                        showError("This user is not a service provider")
                    }
                    return@launch
                }
                
                // Fetch comments for this service provider
                val comments = apiService.getCommentsByUserId(userId)
                
                withContext(Dispatchers.Main) {
                    displayServiceProviderData(user)
                    displayComments(comments)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to load data: ${e.message}")
                }
            }
        }
    }
    
    private fun displayServiceProviderData(user: User) {
        try {
            // Set professional initial
            val initial = user.firstName?.firstOrNull()?.toString() ?: "P"
            professionalInitial.text = initial
            
            // Set professional name
            val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}"
            professionalName.text = fullName
            
            // Set professional occupation
            professionalOccupation.text = user.occupation ?: "Professional"
            
            // Set rating
            professionalRating.rating = user.rating?.toFloat() ?: 0f
            
            // Set availability
            availableDays.text = user.availableDays ?: "Monday - Friday"
            availableHours.text = user.availableHours ?: "9:00 AM - 5:00 PM"
            
            // Set location
            locationAddress.text = user.location?.address ?: "Address not available"
        } catch (e: Exception) {
            showError("Error displaying data: ${e.message}")
        }
    }
    
    private fun displayComments(comments: List<Comment>) {
        try {
            // Clear existing comments
            commentsContainer.removeAllViews()
            
            if (comments.isEmpty()) {
                // Add a message when there are no comments
                val noCommentsView = TextView(this)
                noCommentsView.text = "No comments yet"
                noCommentsView.textSize = 14f
                commentsContainer.addView(noCommentsView)
                return
            }
            
            // Add each comment to the container
            for (comment in comments) {
                val commentView = layoutInflater.inflate(R.layout.item_comment, commentsContainer, false)
                
                // Set comment data
                val commentInitial = commentView.findViewById<TextView>(R.id.commentInitial)
                val commentName = commentView.findViewById<TextView>(R.id.commentName)
                val commentRating = commentView.findViewById<TextView>(R.id.commentRating)
                val commentText = commentView.findViewById<TextView>(R.id.commentText)
                
                // Set the initial letter of the commenter's name
                commentInitial.text = comment.userName?.firstOrNull()?.toString() ?: "U"
                
                // Set commenter name
                commentName.text = comment.userName ?: "Anonymous"
                
                // Set rating
                commentRating.text = comment.rating?.toString() ?: "0.0"
                
                // Set comment text
                commentText.text = comment.content ?: "No comment"
                
                // Add the comment view to the container
                commentsContainer.addView(commentView)
            }
        } catch (e: Exception) {
            showError("Error displaying comments: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        // Display error with Toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}