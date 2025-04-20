package com.example.skillmatch.professional

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.User
import com.example.skillmatch.professional.EditPortfolioActivity
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
import java.io.ByteArrayOutputStream
import java.util.Locale

class EditProfessionalProfile : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var bioInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var profileImage: CircleImageView
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageButton
    
    private var currentUser: User? = null
    private var googleMap: GoogleMap? = null
    private var userLocation: LatLng = LatLng(24.7136, 46.6753) // Default location (Riyadh)
    private var geocoder: Geocoder? = null // For address lookup
    private var selectedImageUri: android.net.Uri? = null
    private var profileImageBase64: String? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val PICK_IMAGE_REQUEST = 1002
        private const val TAKE_PHOTO_REQUEST = 1003
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_professional_profile)
        
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
        bioInput = findViewById(R.id.bioInput)
        addressInput = findViewById(R.id.addressInput)
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
    
    // Method to update address from location
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
            Toast.makeText(this, "Saving profile...", Toast.LENGTH_SHORT).show()
            saveUserProfile()
        }
        
        profileImage.setOnClickListener {
            showImageSelectionOptions()
        }
    }
    
    private fun showImageSelectionOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Profile Picture")
        
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    try {
                        selectedImageUri = data?.data
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                        profileImage.setImageBitmap(bitmap)
                        profileImageBase64 = encodeImageToBase64(bitmap)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    profileImage.setImageBitmap(imageBitmap)
                    profileImageBase64 = encodeImageToBase64(imageBitmap)
                }
            }
        }
    }
    
    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
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
                        Log.e("EditProfile", "Failed to load user data: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(applicationContext, "Failed to load user data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Exception loading user data", e)
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
        bioInput.setText(user.bio)
        
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
        
        // Load profile image if available
        user.profileImage?.let { imageBase64 ->
            try {
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                profileImage.setImageBitmap(bitmap)
                profileImageBase64 = imageBase64
            } catch (e: Exception) {
                // If there's an error loading the image, just use the default
            }
        }
    }
    
    private fun saveUserProfile() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        val bio = bioInput.text.toString().trim()
        val address = addressInput.text.toString().trim()
        
        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get the user ID from session manager
        val userId = sessionManager.getUserId() ?: ""
        
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create updated user object with location including address
        val updatedUser = currentUser?.copy(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            bio = bio,
            profileImage = profileImageBase64,
            location = Location(
                id = currentUser?.location?.id,
                latitude = userLocation.latitude,
                longitude = userLocation.longitude,
                address = address
            )
        ) ?: return
        
        // Show a loading indicator
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Saving profile...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the user profile
                val response = ApiClient.apiService.updateUserProfile(userId, updatedUser)
                
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        
                        // Store the updated token if it's in the response
                        response.body()?.let { user ->
                            // Make sure the session is still valid - using getAuthToken instead of fetchAuthToken
                            val token = sessionManager.getAuthToken() // Use getAuthToken instead of fetchAuthToken
                            if (token != null) {
                                sessionManager.saveAuthToken(token)

                            }
                        }
                        
                        // Add a small delay before navigation to ensure the toast is visible
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Explicitly navigate to EditPortfolioActivity with flags to clear previous activities
                            val intent = Intent(this@EditProfessionalProfile, ProfessionalProfileActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }, 500) // 500ms delay

                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("EditProfile", "Update failed: $errorCode - $errorBody")
                        Toast.makeText(applicationContext, "Failed to update profile (Error $errorCode)", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Exception during update", e)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}