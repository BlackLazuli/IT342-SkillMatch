package com.example.skillmatch.customer

import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.Service
import com.example.skillmatch.services.ProfessionalService
import com.example.skillmatch.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerViewCardActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var contactButton: Button
    private lateinit var profileImage: CircleImageView
    private lateinit var professionalNameText: TextView
    private lateinit var occupationText: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingText: TextView
    private lateinit var bioText: TextView
    private lateinit var servicesContainer: LinearLayout
    private lateinit var pricingContainer: LinearLayout
    private lateinit var availableDaysText: TextView
    private lateinit var availableTimeText: TextView
    private lateinit var locationText: TextView
    
    private lateinit var professionalService: ProfessionalService
    private var professionalId: Long = 0
    private var servicesList: List<Service> = emptyList()
    private lateinit var sessionManager: SessionManager  // Add SessionManager

    companion object {
        private const val TAG = "CustomerViewCard"
        const val EXTRA_PROFESSIONAL_ID = "extra_professional_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_view_card)
        
        // Initialize SessionManager
        sessionManager = SessionManager(this)
        
        // Initialize service
        professionalService = ProfessionalService()
        
        // Get professional ID from intent
        professionalId = intent.getLongExtra(EXTRA_PROFESSIONAL_ID, 0)
        if (professionalId == 0L) {
            Toast.makeText(this, "Error: Professional not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Set up listeners
        setupListeners()
        
        // Load professional data
        loadProfessionalData()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        contactButton = findViewById(R.id.contactButton)
        profileImage = findViewById(R.id.profileImage)
        professionalNameText = findViewById(R.id.professionalNameText)
        occupationText = findViewById(R.id.occupationText)
        ratingBar = findViewById(R.id.ratingBar)
        ratingText = findViewById(R.id.ratingText)
        bioText = findViewById(R.id.bioText)
        servicesContainer = findViewById(R.id.servicesContainer)
        pricingContainer = findViewById(R.id.pricingContainer)
        availableDaysText = findViewById(R.id.availableDaysText)
        availableTimeText = findViewById(R.id.availableTimeText)
        locationText = findViewById(R.id.locationText)
    }
    
    private fun setupListeners() {
        // Back button
        // In setupListeners() method
        backButton.setOnClickListener {
        // Replace onBackPressed() with finish()
        finish()
        }
        
        // Contact button
        contactButton.setOnClickListener {
            // TODO: Implement contact functionality
            Toast.makeText(this, "Contact feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadProfessionalData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val professional = professionalService.getProfessionalById(professionalId)
                var portfolio: Portfolio? = null
                try {
                    // Get token from SessionManager instead of SharedPreferences
                    val token = sessionManager.getToken()
                    if (token.isNullOrEmpty()) {
                        Log.e(TAG, "Token is missing or empty")
                    } else {
                        val response = ApiClient.apiService.getPortfolio("Bearer $token", professionalId.toString())
                        if (response.isSuccessful && response.body() != null) {
                            portfolio = response.body()
                        } else {
                            Log.e(TAG, "Portfolio fetch failed: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching portfolio", e)
                }
                withContext(Dispatchers.Main) {
                    if (professional != null) {
                        displayProfessionalData(professional, portfolio)
                    } else {
                        Toast.makeText(
                            this@CustomerViewCardActivity,
                            "Professional not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading professional data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CustomerViewCardActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
    private fun displayPortfolioServices(services: List<Service>) {
        // Clear existing views
        servicesContainer.removeAllViews()
        
        if (services.isEmpty()) {
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
    
        // Display each service
        services.forEach { service ->
            // Create service item for Services Offered section
            val serviceItemView = layoutInflater.inflate(R.layout.item_service_customer, servicesContainer, false)
            val serviceNameText = serviceItemView.findViewById<TextView>(R.id.serviceNameText)
            
            // Set service name with bullet point
            serviceNameText.text = "• ${service.name}"
            
            // Add to services container
            servicesContainer.addView(serviceItemView)
        }
        
        // Display pricing separately
        services.forEach { service ->
            if (!service.pricing.isNullOrEmpty()) {
                val pricingText = TextView(this).apply {
                    text = "• ${service.name}: ₱${service.pricing}"
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                    setPadding(16, 4, 16, 4)
                }
                servicesContainer.addView(pricingText)
            }
        }
    }

    private fun displayProfessionalData(professional: Professional, portfolio: Portfolio?) {
        // Display basic info
        professionalNameText.text = professional.getFullName()
        occupationText.text = professional.occupation

        // Display rating
        val rating = professional.rating?.toFloat() ?: 0f
        ratingBar.rating = rating
        ratingText.text = String.format("%.1f", rating)

        // Display bio
        bioText.text = professional.bio ?: "No bio available"

        // Display profile picture if available
        professional.profilePicture?.let { profilePicBase64 ->
            try {
                val imageBytes = Base64.decode(profilePicBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image", e)
            }
        }

        // Display location
        locationText.text = professional.location?.address.orEmpty().ifEmpty { "Location not specified" }

        // Display services from portfolio if available
        if (portfolio != null && !portfolio.servicesOffered.isNullOrEmpty()) {
            Log.d(TAG, "Displaying ${portfolio.servicesOffered.size} services from portfolio")
            displayPortfolioServices(portfolio.servicesOffered)
            
            // Get availability info from services
            val allServiceDays = portfolio.servicesOffered.flatMap { it.daysOfTheWeek ?: emptyList() }.distinct()
            val allServiceHours = portfolio.servicesOffered.mapNotNull { it.time }.firstOrNull { it.isNotEmpty() }
            
            // Display availability
            val availabilityText = if (allServiceDays.isNotEmpty() && !allServiceHours.isNullOrEmpty()) {
                "${allServiceDays.joinToString(", ")} - $allServiceHours"
            } else if (allServiceDays.isNotEmpty()) {
                allServiceDays.joinToString(", ")
            } else if (!allServiceHours.isNullOrEmpty()) {
                allServiceHours
            } else {
                professional.availableDays.joinToString(", ").ifEmpty { "Not specified" }
            }
            
            availableDaysText.text = availabilityText
            availableTimeText.visibility = View.GONE // Hide the time text view
        } else {
            // No portfolio services, display professional's availability
            Log.d(TAG, "No portfolio services available, using professional data")
            availableDaysText.text = professional.availableDays.joinToString(", ").ifEmpty { "Not specified" }
            availableTimeText.text = professional.availableHours.ifEmpty { "Not specified" }
            
            // Display empty services message
            val emptyList = listOf<Service>()
            displayPortfolioServices(emptyList)
        }
    }
}