package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.api.ApiService
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.User
import com.example.skillmatch.utils.SessionManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import de.hdodenhof.circleimageview.CircleImageView

class CustomerProfile : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    
    // UI Elements
    private lateinit var backButton: ImageButton
    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var editProfileButton: Button
    private lateinit var appointmentsCard: CardView
    private lateinit var mapImage: ImageView
    
    // Navigation Icons
    private lateinit var homeIcon: ImageView
    private lateinit var appointmentsIcon: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var profileIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customerprofile)
        
        // Initialize API service and session manager
        apiService = ApiClient.getApiService(this)
        sessionManager = SessionManager(this)
        
        // Initialize UI elements
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
        
        // Load user data
        loadUserData()
    }
    
    private fun initializeViews() {
        // Initialize header elements
        backButton = findViewById(R.id.backButton)
        
        // Initialize profile elements
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        editProfileButton = findViewById(R.id.editProfileButton)
        
        // Initialize cards
        appointmentsCard = findViewById(R.id.appointmentsCard)
        mapImage = findViewById(R.id.mapImage)
        
        // Initialize navigation icons
        homeIcon = findViewById(R.id.homeIcon)
        appointmentsIcon = findViewById(R.id.appointmentsIcon)
        settingsIcon = findViewById(R.id.settingsIcon)
        profileIcon = findViewById(R.id.profileIcon)
    }
    
    private fun setupClickListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }
        
        // Edit profile button
        editProfileButton.setOnClickListener {
            // Navigate to edit profile screen
            // Intent to EditProfileActivity
        }
        
        // Appointments card
        appointmentsCard.setOnClickListener {
            // Navigate to appointments screen
            // Intent to AppointmentsActivity
        }
        
        // Navigation icons
        homeIcon.setOnClickListener {
            // Navigate to home/dashboard
            val intent = Intent(this, CustomerDashboard::class.java)
            startActivity(intent)
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
        
        // Profile icon is current screen
    }
    
    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // Fetch user data
                    val user = apiService.getUserById(userId)
                    
                    // Update UI with user data
                    displayUserData(user)
                    
                    // Load location data if available
                    loadLocationData(userId)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    private fun displayUserData(user: User) {
        // Set user name
        userName.text = "${user.firstName} ${user.lastName}"
        
        // Load profile image if available
        user.profileImageUrl?.let { imageUrl ->
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(profileImage)
        }
    }
    
    private fun loadLocationData(userId: String) {
        lifecycleScope.launch {
            try {
                // Fetch location data
                val location = apiService.getLocationByUserId(userId)
                
                // Update map with location data
                displayLocationOnMap(location)
            } catch (e: Exception) {
                // Handle error - use default map image
            }
        }
    }
    
    private fun displayLocationOnMap(location: Location) {
        // In a real app, you would use Google Maps or another mapping service
        // For this example, we'll just use a static map image
        
        // If you have Google Maps API key, you could load a static map like this:
        // val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=${location.latitude},${location.longitude}&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C${location.latitude},${location.longitude}&key=YOUR_API_KEY"
        // Picasso.get().load(mapUrl).into(mapImage)
        
        // For now, we'll just use the placeholder
    }
}