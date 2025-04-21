package com.example.skillmatch.customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.User
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

class CustomerProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var sessionManager: SessionManager
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var editProfileButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var appointmentsCard: CardView
    private lateinit var favoritesCard: CardView
    private lateinit var paymentHistoryCard: CardView
    private lateinit var homeButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var settingsNavButton: ImageButton
    private lateinit var profileButton: ImageButton

    private var googleMap: GoogleMap? = null
    private var userLocation: LatLng? = null
    private var currentUser: User? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customerprofile)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize views
        initializeViews()

        // Set up map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set click listeners
        setupClickListeners()

        // Load user profile data
        loadUserProfile()
    }

    private fun initializeViews() {
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        profileImage = findViewById(R.id.profileImage)
        editProfileButton = findViewById(R.id.editProfileButton)
        backButton = findViewById(R.id.backButton)
        appointmentsCard = findViewById(R.id.appointmentsCard)
        favoritesCard = findViewById(R.id.favoritesCard)
        paymentHistoryCard = findViewById(R.id.paymentHistoryCard)
        homeButton = findViewById(R.id.homeButton)
        calendarButton = findViewById(R.id.calendarButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)
        profileButton = findViewById(R.id.profileButton)
    }

    private fun setupClickListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            finish()
        }

        // Edit profile button click listener
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditCustomerProfile::class.java)
            startActivity(intent)
        }

        // Appointments card click listener
        appointmentsCard.setOnClickListener {
            Toast.makeText(this, "CustomerAppointments feature coming soon", Toast.LENGTH_SHORT).show()
          /*  val intent = Intent(this, CustomerAppointments::class.java)
            startActivity(intent)*/
        }

        // Favorites card click listener
        favoritesCard.setOnClickListener {
            // Navigate to favorites screen
            Toast.makeText(this, "Favorites feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Payment History card click listener
        paymentHistoryCard.setOnClickListener {
            // Navigate to payment history screen
            Toast.makeText(this, "Payment History feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Bottom navigation buttons
        homeButton.setOnClickListener {
            // Navigate to home screen (CustomerDashboard)
 val intent = Intent(this, CustomerDashboard::class.java)
 intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
 startActivity(intent)
}

calendarButton.setOnClickListener {
 // Navigate to appointments screen
/* val intent = Intent(this, CustomerAppointments::class.java)
 startActivity(intent)*/
}

settingsNavButton.setOnClickListener {
 // Navigate to settings screen
 Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show()
}

profileButton.setOnClickListener {
 // Already on profile screen
 Toast.makeText(this, "Already on Profile screen", Toast.LENGTH_SHORT).show()
}
}

private fun loadUserProfile() {
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
             Log.e("CustomerProfileActivity", "Failed to load user data: ${response.code()} - ${response.errorBody()?.string()}")
             Toast.makeText(applicationContext, "Failed to load user data: ${response.code()}", Toast.LENGTH_SHORT).show()
         }
     }
 } catch (e: Exception) {
     Log.e("CustomerProfileActivity", "Exception loading user data", e)
     withContext(Dispatchers.Main) {
         Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
     }
 }
}
}

private fun populateUserData(user: User) {
// Set user name (combine first and last name)
val fullName = "${user.firstName} ${user.lastName}"
nameText.text = fullName

// Set user email
emailText.text = user.email

// Load profile image if available
user.profilePicture?.let { imageBase64 ->
 try {
     val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
     val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
     profileImage.setImageBitmap(bitmap)
 } catch (e: Exception) {
     // If there's an error loading the image, just use the default
     Log.e("CustomerProfileActivity", "Error loading profile image", e)
 }
}

// Update map with user location if available
user.location?.let { location ->
 val lat = location.latitude
 val lng = location.longitude
 userLocation = LatLng(lat, lng)
 updateMapLocation()
}
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

 // Update map with user location if available
 userLocation?.let {
     updateMapLocation()
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

private fun updateMapLocation() {
userLocation?.let { location ->
 googleMap?.clear()
 googleMap?.addMarker(MarkerOptions().position(location).title("Your Location"))
 googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
}
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

override fun onResume() {
super.onResume()
// Reload user profile when returning to this screen (in case it was updated)
loadUserProfile()
}
}