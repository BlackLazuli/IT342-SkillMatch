package com.example.skillmatch.customer

import android.Manifest
import android.content.pm.PackageManager
// Remove this incorrect import
// import android.health.connect.datatypes.ExerciseRoute.Location
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.User
import com.example.skillmatch.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class EditCustomerProfile : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var addressInput: EditText // Added address input field
    private lateinit var profileImage: CircleImageView
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageButton
    
    private var currentUser: User? = null
    private var googleMap: GoogleMap? = null
    private var userLocation: LatLng = LatLng(14.5995, 120.9842) // Default to Manila, Philippines
    private var geocoder: Geocoder? = null // For address lookup
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editcustomerprofile)
        
        // Initialize SessionManager and Geocoder
        sessionManager = SessionManager(this)
        geocoder = Geocoder(this, Locale.getDefault())
        
        // Initialize UI components
        initializeViews()
        
        // Set click listeners
        setupClickListeners()
        
        // Initialize map
        initializeMap()
        
        // Load user data
        loadUserData()
    }
    
    private fun initializeViews() {
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        addressInput = findViewById(R.id.addressInput) // Initialize address input
        profileImage = findViewById(R.id.profileImage)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
    }
    
    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            
            // Set up map with user's location if available
            if (currentUser?.location != null) {
                val lat = currentUser?.location?.latitude ?: 0.0
                val lng = currentUser?.location?.longitude ?: 0.0
                userLocation = LatLng(lat, lng)
            }
            
            updateMapLocation()
            
            // Allow user to select location by tapping on map
            googleMap?.setOnMapClickListener { latLng ->
                userLocation = latLng
                updateMapLocation()
                // Get address for the selected location
                updateAddressFromLocation(latLng)
            }
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    // New method to update address from location
    private fun updateAddressFromLocation(latLng: LatLng) {
        try {
            val addresses = geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = address.getAddressLine(0) ?: ""
                addressInput.setText(addressText)
            }
        } catch (e: Exception) {
            // Handle exception
            Toast.makeText(this, "Could not determine address", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateMapLocation() {
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(userLocation).title("Selected Location"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize map
                onMapReady(googleMap ?: return)
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Some features may be limited.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        saveButton.setOnClickListener {
            saveUserProfile()
        }
        
        backButton.setOnClickListener {
            finish()
        }
        
        profileImage.setOnClickListener {
            Toast.makeText(this, "Profile image selection not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getUserProfile(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()
                    
                    withContext(Dispatchers.Main) {
                        populateUserData(currentUser!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun populateUserData(user: User) {
        firstNameInput.setText(user.firstName)
        lastNameInput.setText(user.lastName)
        emailInput.setText(user.email)
        phoneNumberInput.setText(user.phoneNumber)
        
        // Update address field if available
        user.location?.address?.let {
            addressInput.setText(it)
        }
        
        // Update map with user location if available
        user.location?.let { location ->
            val lat = location.latitude
            val lng = location.longitude
            userLocation = LatLng(lat, lng)
            updateMapLocation()
        }
    }
    
    private fun saveUserProfile() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        val address = addressInput.text.toString().trim() // Get address from input
        
        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create updated user object with location including address
        val updatedUser = currentUser?.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            location = Location(
                latitude = userLocation.latitude,
                longitude = userLocation.longitude,
                address = address
            )
        ) ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.updateUserProfile(sessionManager.getUserId()!!, updatedUser)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}