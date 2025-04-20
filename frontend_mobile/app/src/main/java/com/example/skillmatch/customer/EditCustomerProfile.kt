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
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

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
    private var selectedImageUri: Uri? = null
    private var profileImageBase64: String? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val PICK_IMAGE_REQUEST = 1002
        private const val TAKE_PHOTO_REQUEST = 1003
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
        // Back button
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Save button
        saveButton.setOnClickListener {
            saveUserProfile()
        }
        
        // Profile image click listener
        profileImage.setOnClickListener {
            showImagePickerDialog()
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Choose Profile Picture")
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
                        
                        // Convert bitmap to base64
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                        val imageBytes = outputStream.toByteArray()
                        profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Error picking image", e)
                        Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    try {
                        val bitmap = data?.extras?.get("data") as Bitmap
                        profileImage.setImageBitmap(bitmap)
                        
                        // Convert bitmap to base64
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                        val imageBytes = outputStream.toByteArray()
                        profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Error taking photo", e)
                        Toast.makeText(this, "Error capturing photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                        // Populate form fields with user data
                        firstNameInput.setText(currentUser?.firstName)
                        lastNameInput.setText(currentUser?.lastName)
                        emailInput.setText(currentUser?.email)
                        phoneNumberInput.setText(currentUser?.phoneNumber)
                        
                        // Set address if available
                        currentUser?.location?.address?.let { address ->
                            addressInput.setText(address)
                        }
                        
                        // Load profile image if available
                        currentUser?.profilePicture?.let { imageBase64 ->
                            try {
                                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                profileImage.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                Log.e("EditProfile", "Error loading profile image", e)
                            }
                        }
                        
                        // Update map with user location
                        currentUser?.location?.let { location ->
                            val lat = location.latitude
                            val lng = location.longitude
                            userLocation = LatLng(lat, lng)
                            updateMapLocation()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Error loading user data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Also update the saveUserProfile method to remove the token parameter
    private fun saveUserProfile() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val phoneNumber = phoneNumberInput.text.toString().trim()
        val address = addressInput.text.toString().trim() // Get address from input
        
        // Add debug logging
        Log.d("EditProfile", "Starting save: $firstName, $lastName, $email")
        Log.d("EditProfile", "User ID: ${sessionManager.getUserId()}")
        Log.d("EditProfile", "Address: $address")
        Log.d("EditProfile", "Location: ${userLocation.latitude}, ${userLocation.longitude}")
        
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
            profilePicture = profileImageBase64,
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
                Log.d("EditProfile", "Making API call to update profile")
                Log.d("EditProfile", "Using user ID: $userId")
                Log.d("EditProfile", "User data: $updatedUser")
                
                // Just update the user profile with the location included
                val response = ApiClient.apiService.updateUserProfile(userId, updatedUser)
                
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (response.isSuccessful) {
                        Log.d("EditProfile", "Update successful: ${response.body()}")
                        Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        
                        // Force a refresh of the data
                        loadUserData()
                        
                        // Delay finish to show success message
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("EditProfile", "Update failed: $errorCode - $errorBody")
                        Toast.makeText(applicationContext, "Failed to update profile (Error $errorCode): $errorBody", Toast.LENGTH_LONG).show()
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