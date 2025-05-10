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
import com.example.skillmatch.professional.EditProfessionalProfile
import com.example.skillmatch.professional.ProfessionalProfileActivity
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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
    private var userLocation: LatLng = LatLng(10.3157, 123.8854) // Default to Cebu, Philippines
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
        
        // Add text watcher to address input
        addressInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: android.text.Editable?) {
                // Don't trigger for every keystroke to avoid excessive API calls
            }
        })
        
        // Add focus change listener to address input
        addressInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val addressText = addressInput.text.toString().trim()
                if (addressText.isNotEmpty()) {
                    updateLocationFromAddress(addressText)
                }
            }
        }
        
        // No search button needed as we're using focus change listener
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
        // In setupClickListeners() method or wherever backButton click listener is defined
        backButton.setOnClickListener {
        // Replace onBackPressed() with finish()
        finish()
        }
        
        // Save button
        saveButton.setOnClickListener {
            saveUserProfile()
        }
        
        // Profile image click listener
        profileImage.setOnClickListener {
            showImagePickerDialog()
        }
        
        // Add a button to manually trigger address search
        addressInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == android.view.KeyEvent.ACTION_DOWN && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                val addressText = addressInput.text.toString().trim()
                if (addressText.isNotEmpty()) {
                    updateLocationFromAddress(addressText)
                    // Hide keyboard
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(addressInput.windowToken, 0)
                    return@setOnEditorActionListener true
                }
            }
            false
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
                        val file = selectedImageUri?.let { uri ->
                            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                            val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
                            cursor?.moveToFirst()
                            val columnIndex = cursor?.getColumnIndex(filePathColumn[0]) ?: -1
                            val picturePath = if (columnIndex != -1) cursor?.getString(columnIndex) else null
                            cursor?.close()
                            picturePath?.let { File(it) }
                        }
                        if (file != null) {
                            uploadProfilePictureFile(file)
                        } else {
                            Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Error picking image", e)
                        Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    try {
                        val bitmap = data?.extras?.get("data") as Bitmap
                        // Save bitmap to file
                        val file = File(cacheDir, "profile_temp.jpg")
                        val outputStream = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        uploadProfilePictureFile(file)
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Error taking photo", e)
                        Toast.makeText(this, "Error capturing photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun uploadProfilePictureFile(file: File) {
        val userId = sessionManager.getUserId() ?: return
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.uploadProfilePicture(userId, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        user?.profilePicture?.let { url ->
                            Glide.with(this@EditCustomerProfile)
                                .load(url)
                                .placeholder(R.drawable.user)
                                .into(profileImage)
                        }
                        Toast.makeText(this@EditCustomerProfile, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditCustomerProfile, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditCustomerProfile, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        currentUser?.profilePicture?.let { profilePic ->
                            val backendBaseUrl = "http://3.107.23.86:8080"
                            if (profilePic.startsWith("http")) {
                                Glide.with(this@EditCustomerProfile)
                                    .load(profilePic)
                                    .placeholder(R.drawable.user)
                                    .into(profileImage)
                            } else if (profilePic.startsWith("/uploads")) {
                                Glide.with(this@EditCustomerProfile)
                                    .load(backendBaseUrl + profilePic)
                                    .placeholder(R.drawable.user)
                                    .into(profileImage)
                            } else {
                                try {
                                    val imageBytes = Base64.decode(profilePic, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    profileImage.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                    Log.e("EditProfile", "Error loading profile image", e)
                                }
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
                            val intent = Intent(this@EditCustomerProfile, CustomerProfileActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
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
    
    // Method to update location from address - moved inside the class
    private fun updateLocationFromAddress(address: String) {
        // Show a loading indicator
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Finding location...")
        progressDialog.setCancelable(true)
        progressDialog.show()
        
        // Use a coroutine to perform geocoding off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Handle both older and newer Android versions
                // For Android SDK 33+ (Tiramisu), we need to use the new API
                val addresses = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    // New API with callback
                    var locationList: List<android.location.Address>? = null
                    geocoder?.getFromLocationName(address, 1) { results ->
                        locationList = results
                    }
                    // Wait a bit for the callback to complete
                    kotlinx.coroutines.delay(1000)
                    locationList
                } else {
                    // Legacy API for older versions
                    @Suppress("DEPRECATION")
                    geocoder?.getFromLocationName(address, 1)
                }
                
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (!addresses.isNullOrEmpty()) {
                        val location = addresses[0]
                        val latLng = LatLng(location.latitude, location.longitude)
                        userLocation = latLng
                        updateMapLocation()
                        Toast.makeText(this@EditCustomerProfile, "Location updated", Toast.LENGTH_SHORT).show()
                        
                        // Log for debugging
                        Log.d("EditProfile", "Updated location from address: $latLng")
                    } else {
                        Toast.makeText(this@EditCustomerProfile, "Could not find location for this address", Toast.LENGTH_SHORT).show()
                        Log.d("EditProfile", "No location found for address: $address")
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Error finding location from address", e)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@EditCustomerProfile, "Error finding location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}