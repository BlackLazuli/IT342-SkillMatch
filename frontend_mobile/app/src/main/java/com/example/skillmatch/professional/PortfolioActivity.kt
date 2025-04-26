package com.example.skillmatch.professional


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            // Navigate to home screen
            // Intent to home activity
        }

        messagesButton.setOnClickListener {
            // Navigate to messages screen
            // Intent to messages activity
        }

        settingsNavButton.setOnClickListener {
            // Navigate to settings screen
            // Intent to settings activity
        }

        profileButton.setOnClickListener {
            // Already on profile screen
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

    private fun displayProfilePicture(profilePictureBase64: String?) {
        profilePictureBase64?.let {
            try {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("Portfolio", "Error loading profile image", e)
            }
        }
    }

    private fun displayPortfolioData(portfolio: Portfolio) {
        // Display work experience
        workExperienceText.text = portfolio.workExperience ?: "No work experience added"

        // Clear existing services and pricing
        servicesContainer.removeAllViews()

        // Display services
        if (portfolio.servicesOffered.isNullOrEmpty()) {
            val noServicesText = TextView(this).apply {
                text = "No services added yet"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setPadding(16, 16, 16, 16)
            }
            servicesContainer.addView(noServicesText)
        } else {
            // Display each service in its own container
            portfolio.servicesOffered.forEach { service ->
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
                        setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                        setPadding(0, 0, 0, 8)
                    }
                    serviceContent.addView(descriptionText)
                }
                
                // Add service pricing if available
                if (!service.pricing.isNullOrEmpty()) {
                    val pricingText = TextView(this).apply {
                        text = "Price: ₱${service.pricing}"
                        textSize = 14f
                        setTextColor(resources.getColor(android.R.color.black, theme))
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
    
        // Display availability information from portfolio
        val daysText = if (portfolio.daysAvailable.isNotEmpty()) {
            formatDaysOfWeek(portfolio.daysAvailable)
        } else {
            "Not specified"
        }
        availableDaysText.text = daysText
        
        availableTimeText.text = portfolio.time ?: "Not specified"
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

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        fetchPortfolioData()
    }
}