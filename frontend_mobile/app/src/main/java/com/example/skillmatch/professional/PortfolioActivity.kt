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
import com.example.skillmatch.models.Service
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
        workExperienceText.text = portfolio.workExperience ?: "No work experience added yet."
        
        // Clear and display services
        servicesContainer.removeAllViews()
        pricingContainer.removeAllViews()
        
        // Get first service for availability info
        val firstService = portfolio.servicesOffered?.firstOrNull()
        
        // Display availability
        val availableDays = firstService?.daysOfTheWeek ?: emptyList()
        if (availableDays.isNotEmpty()) {
            availableDaysText.text = availableDays.joinToString(", ")
        } else {
            availableDaysText.text = "No availability set"
        }
        
        availableTimeText.text = firstService?.time ?: "No time set"
        
        // Display services
        portfolio.servicesOffered?.forEach { service ->
            addServiceToView(service)
        }
    }
    
    private fun addServiceToView(service: Service) {
        // Add service name and description
        val serviceTextView = TextView(this).apply {
            text = "• ${service.name}"
            if (!service.description.isNullOrEmpty()) {
                text = "$text\nDescription: ${service.description}"
            }
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.black, theme))
            setPadding(0, 4, 0, 4)
        }
        servicesContainer.addView(serviceTextView)
        
        // Add pricing
        if (!service.pricing.isNullOrEmpty()) {
            val pricingTextView = TextView(this).apply {
                text = "• ${service.name}: ${service.pricing}"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.black, theme))
                setPadding(0, 4, 0, 4)
            }
            pricingContainer.addView(pricingTextView)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        fetchPortfolioData()
    }
}