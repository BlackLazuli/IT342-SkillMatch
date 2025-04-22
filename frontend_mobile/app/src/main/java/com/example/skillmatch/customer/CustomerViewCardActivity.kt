package com.example.skillmatch.customer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
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

    companion object {
        private const val TAG = "CustomerViewCard"
        const val EXTRA_PROFESSIONAL_ID = "extra_professional_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_view_card)
        
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
        backButton.setOnClickListener {
            onBackPressed()
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
                // Get professional details
                val professional = professionalService.getProfessionalById(professionalId)
                
                if (professional != null) {
                    // Also try to get portfolio data if available
                    var portfolio: Portfolio? = null
                    try {
                        val response = ApiClient.apiService.getPortfolio("", professionalId.toString())
                        if (response.isSuccessful && response.body() != null) {
                            portfolio = response.body()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching portfolio", e)
                        // Continue without portfolio data
                    }
                    
                    withContext(Dispatchers.Main) {
                        displayProfessionalData(professional, portfolio)
                    }
                } else {
                    withContext(Dispatchers.Main) {
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
        
        // Display availability
        val daysText = if (professional.availableDays.isNotEmpty()) {
            professional.availableDays.joinToString(", ")
        } else {
            "Not specified"
        }
        availableDaysText.text = daysText
        
        availableTimeText.text = if (professional.availableHours.isNotEmpty()) {
            professional.availableHours
        } else {
            "Not specified"
        }
        
        // Display services and pricing from portfolio if available
        if (portfolio != null && !portfolio.servicesOffered.isNullOrEmpty()) {
            displayPortfolioServices(portfolio.servicesOffered)
        } else {
            // If no portfolio, show a message
            val noServicesText = TextView(this).apply {
                text = "No services information available"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            servicesContainer.addView(noServicesText)
            
            val noPricingText = TextView(this).apply {
                text = "No pricing information available"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            pricingContainer.addView(noPricingText)
        }
    }
    
    private fun displayPortfolioServices(services: List<Service>) {
        // Clear containers first
        servicesContainer.removeAllViews()
        pricingContainer.removeAllViews()
        
        if (services.isEmpty()) {
            val noServicesText = TextView(this).apply {
                text = "No services listed"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
            }
            servicesContainer.addView(noServicesText)
            return
        }
        
        // Add each service to the view
        services.forEach { service ->
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
    }
}