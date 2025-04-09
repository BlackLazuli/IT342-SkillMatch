package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.api.ApiService
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.User
import com.example.skillmatch.utils.SessionManager
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class EditCustomerProfile : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    
    // UI Elements
    private lateinit var backButton: ImageButton
    private lateinit var profileImage: CircleImageView
    private lateinit var saveButton: Button
    private lateinit var selectLocationButton: Button
    
    // Form fields
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var mapImage: ImageView
    
    // User data
    private var userId: String? = null
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editcustomerprofile)
        
        // Initialize API service and session manager
        apiService = ApiClient.getApiService(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()
        
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
        saveButton = findViewById(R.id.saveButton)
        
        // Initialize form fields
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        addressInput = findViewById(R.id.addressInput)
        cityInput = findViewById(R.id.cityInput)
        
        // Initialize map elements
        mapImage = findViewById(R.id.mapImage)
        selectLocationButton = findViewById(R.id.selectLocationButton)
    }
    
    private fun setupClickListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }
        
        // Save button
        saveButton.setOnClickListener {
            saveUserProfile()
        }
        
        // Select location button
        selectLocationButton.setOnClickListener {
            // Open map selection activity or dialog
            // For now, we'll just show a toast
            Toast.makeText(this, "Location selection coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadUserData() {
        userId?.let { id ->
            lifecycleScope.launch {
                try {
                    // Fetch user data
                    val user = apiService.getUserById(id)
                    
                    // Populate form fields
                    populateFormFields(user)
                    
                    // Load location data
                    loadLocationData(id)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditCustomerProfile,
                        "Error loading profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun populateFormFields(user: User) {
        // Set text fields
        firstNameInput.setText(user.firstName)
        lastNameInput.setText(user.lastName)
        emailInput.setText(user.email)
        phoneNumberInput.setText(user.phoneNumber)
        
        // Load profile image if available
        user.profileImageUrl?.let { imageUrl ->
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImage)
        }
    }
    
    private fun loadLocationData(userId: String) {
        lifecycleScope.launch {
            try {
                // Fetch location data
                val location = apiService.getLocationByUserId(userId)
                currentLocation = location
                
                // Populate location fields
                addressInput.setText(location.address)
                cityInput.setText(location.city)
                
                // Update map with location data
                displayLocationOnMap(location)
            } catch (e: Exception) {
                // Handle error - location might not exist yet
            }
        }
    }
    
    private fun displayLocationOnMap(location: Location) {
        // In a real app, you would use Google Maps or another mapping service
        // For this example, we'll just use a static map image
        
        // If you have Google Maps API key, you could load a static map like this:
        // val mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=${location.latitude},${location.longitude}&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C${location.latitude},${location.longitude}&key=YOUR_API_KEY"
        // Picasso.get().load(mapUrl).into(mapImage)
    }
    
    private fun saveUserProfile() {
        // Validate inputs
        if (firstNameInput.text.isBlank() || lastNameInput.text.isBlank() || emailInput.text.isBlank()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        userId?.let { id ->
            lifecycleScope.launch {
                try {
                    // Create updated user object
                    val updatedUser = User(
                        id = id,
                        firstName = firstNameInput.text.toString(),
                        lastName = lastNameInput.text.toString(),
                        email = emailInput.text.toString(),
                        phoneNumber = phoneNumberInput.text.toString(),
                        // Other fields remain unchanged
                        profileImageUrl = null, // Would be handled separately in a real app
                        role = "CUSTOMER" // Assuming this is a customer profile
                    )
                    
                    // Update user profile
                    val response = apiService.updateUser(id, updatedUser)
                    
                    // Update location if provided
                    if (addressInput.text.isNotBlank() && cityInput.text.isNotBlank()) {
                        val updatedLocation = Location(
                            userId = id,
                            address = addressInput.text.toString(),
                            city = cityInput.text.toString(),
                            latitude = currentLocation?.latitude ?: 0.0,
                            longitude = currentLocation?.longitude ?: 0.0
                        )
                        
                        apiService.saveOrUpdateLocation(id, updatedLocation)
                    }
                    
                    // Show success message and return to profile
                    Toast.makeText(this@EditCustomerProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    
                    // Return to profile screen
                    val intent = Intent(this@EditCustomerProfile, CustomerProfile::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditCustomerProfile,
                        "Error updating profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}