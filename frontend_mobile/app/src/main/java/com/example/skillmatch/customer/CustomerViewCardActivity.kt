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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.adapters.CommentAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.CommentResponse
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.Service
import com.example.skillmatch.services.ProfessionalService
import com.example.skillmatch.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerViewCardActivity : AppCompatActivity(), OnMapReadyCallback {

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
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var noCommentsText: TextView
    
    private lateinit var professionalService: ProfessionalService
    private var professionalId: Long = 0
    private var servicesList: List<Service> = emptyList()
    private lateinit var sessionManager: SessionManager  // Add SessionManager
    private lateinit var commentAdapter: CommentAdapter
    
    // Google Maps variables
    private var googleMap: GoogleMap? = null
    private var professionalLocation: LatLng? = null

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
        
        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
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
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.adapter = commentAdapter
        
        noCommentsText = findViewById(R.id.noCommentsText)
    }
    
    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            // Replace onBackPressed() with finish()
            finish()
        }
        
        // Contact button
        contactButton.setOnClickListener {
            // Launch SetAppointmentActivity
            val intent = Intent(this, SetAppointmentActivity::class.java).apply {
                putExtra(SetAppointmentActivity.EXTRA_PROFESSIONAL_ID, professionalId)
                putExtra(SetAppointmentActivity.EXTRA_PROFESSIONAL_NAME, professionalNameText.text.toString())
                putExtra(SetAppointmentActivity.EXTRA_PROFESSIONAL_ROLE, occupationText.text.toString())
                putExtra(SetAppointmentActivity.EXTRA_PROFESSIONAL_RATING, ratingBar.rating)
            }
            startActivity(intent)
        }
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // If we already have the professional's location, update the map
        professionalLocation?.let { location ->
            updateMapLocation(location)
        }
    }
    
    private fun updateMapLocation(location: LatLng) {
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions()
            .position(location)
            .title(professionalNameText.text.toString()))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        
        // Log the location for debugging
        Log.d(TAG, "Updating map location to: ${location.latitude}, ${location.longitude}")
    }
    
    private fun loadProfessionalData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get token from SessionManager
                val token = sessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is missing or empty")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CustomerViewCardActivity,
                            "Authentication error. Please log in again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    return@launch
                }
                
                // Fetch portfolio data which contains the professional information
                val portfolioResponse = ApiClient.apiService.getPortfolio("Bearer $token", professionalId.toString())
                
                withContext(Dispatchers.Main) {
                    if (portfolioResponse.isSuccessful && portfolioResponse.body() != null) {
                        val portfolio = portfolioResponse.body()!!
                        
                        // Extract professional data from the portfolio's user field
                        val user = portfolio.user
                        if (user != null) {
                            // Convert User to Professional
                            val professional = Professional(
                                id = user.id?.toLong() ?: professionalId,
                                firstName = user.firstName ?: "",
                                lastName = user.lastName ?: "",
                                email = user.email ?: "",
                                occupation = user.role ?: "Professional",
                                bio = user.bio,
                                phoneNumber = user.phoneNumber,
                                rating = user.rating,
                                profilePicture = user.profilePicture,
                                location = user.location,
                                availableDays = portfolio.daysAvailable,
                                availableHours = portfolio.time ?: ""
                            )
                            
                            displayProfessionalData(professional, portfolio)
                            
                            // Load comments for this portfolio
                            loadComments(portfolio.id)
                        } else {
                            Toast.makeText(
                                this@CustomerViewCardActivity,
                                "Professional information not found",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } else {
                        Log.e(TAG, "Portfolio fetch failed: ${portfolioResponse.code()} - ${portfolioResponse.errorBody()?.string()}")
                        Toast.makeText(
                            this@CustomerViewCardActivity,
                            "Failed to load professional data",
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
    
    private fun loadComments(portfolioId: Long?) {
        if (portfolioId == null) {
            Log.e(TAG, "Cannot load comments: Portfolio ID is null")
            noCommentsText.visibility = View.VISIBLE
            commentsRecyclerView.visibility = View.GONE
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                            commentAdapter.updateComments(comments)
                            noCommentsText.visibility = View.GONE
                            commentsRecyclerView.visibility = View.VISIBLE
                        } else {
                            noCommentsText.visibility = View.VISIBLE
                            commentsRecyclerView.visibility = View.GONE
                        }
                    } else {
                        Log.e(TAG, "Failed to load comments: ${commentsResponse.code()} - ${commentsResponse.errorBody()?.string()}")
                        noCommentsText.visibility = View.VISIBLE
                        commentsRecyclerView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading comments", e)
                withContext(Dispatchers.Main) {
                    noCommentsText.visibility = View.VISIBLE
                    commentsRecyclerView.visibility = View.GONE
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
        
        // Update map with professional's location
        professional.location?.let { location ->
            // Since latitude and longitude are non-nullable Double in the Location class,
            // we can use them directly without conversion
            val lat = location.latitude
            val lng = location.longitude
            
            Log.d(TAG, "Professional location found: $lat, $lng")
            professionalLocation = LatLng(lat, lng)
            
            // Update map if it's already initialized
            googleMap?.let { map ->
                updateMapLocation(professionalLocation!!)
            }
        } ?: run {
            Log.d(TAG, "Professional location is null")
            // Set a default location (e.g., Manila, Philippines)
            professionalLocation = LatLng(14.5995, 120.9842)
            googleMap?.let { map ->
                updateMapLocation(professionalLocation!!)
            }
        }

        // Display services from portfolio if available
        if (portfolio != null && !portfolio.servicesOffered.isNullOrEmpty()) {
            Log.d(TAG, "Displaying ${portfolio.servicesOffered.size} services from portfolio")
            displayPortfolioServices(portfolio.servicesOffered)
            
            // Get availability info directly from portfolio
            val daysAvailable = portfolio.daysAvailable
            val time = portfolio.time
            
            // Display availability
            val daysText = if (daysAvailable.isNotEmpty()) {
                daysAvailable.joinToString(", ")
            } else {
                professional.availableDays.joinToString(", ").ifEmpty { "Not specified" }
            }
            
            val hoursText = if (!time.isNullOrEmpty()) {
                time
            } else {
                professional.availableHours ?: "Not specified"
            }
            availableDaysText.text = daysText
            availableTimeText.text = hoursText
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