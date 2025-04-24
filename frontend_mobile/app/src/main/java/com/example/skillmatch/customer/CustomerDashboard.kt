package com.example.skillmatch.customer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.skillmatch.R
import com.example.skillmatch.adapter.ProfessionalsAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.Service
import com.example.skillmatch.models.User
import com.example.skillmatch.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDashboard : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var homeButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var settingsNavButton: ImageButton
    private lateinit var profileButton: ImageButton
    
    private lateinit var professionalsAdapter: ProfessionalsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var userLocation: com.example.skillmatch.models.Location? = null
    private var professionals = mutableListOf<Professional>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "CustomerDashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize views
        initializeViews()

        // Set up RecyclerView
        setupRecyclerView()

        // Set up SwipeRefreshLayout
        setupSwipeRefresh()

        // Set click listeners
        setupClickListeners()

        // Check location permission and get user location
        checkLocationPermission()
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        // Remove the filterButton initialization
        recyclerView = findViewById(R.id.professionalsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)
        calendarButton = findViewById(R.id.calendarButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)
        profileButton = findViewById(R.id.profileButton)
    }

    private fun setupRecyclerView() {
        professionalsAdapter = ProfessionalsAdapter(professionals) { professional ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get token from SessionManager
                    val token = sessionManager.getToken()
                    if (token.isNullOrEmpty()) {
                        Log.e(TAG, "Token is missing or empty")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CustomerDashboard, "Please log in again", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    
                    val response = ApiClient.apiService.getPortfolio("Bearer $token", professional.id.toString())
                    if (response.isSuccessful && response.body() != null) {
                        val services = response.body()!!.servicesOffered ?: emptyList()
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@CustomerDashboard, CustomerViewCardActivity::class.java)
                            intent.putExtra(CustomerViewCardActivity.EXTRA_PROFESSIONAL_ID, professional.id)
                            intent.putParcelableArrayListExtra(
                                "EXTRA_SERVICES_LIST",
                                ArrayList(services)
                            )
                            startActivity(intent)
                        }
                    } else {
                        Log.e(TAG, "Portfolio fetch failed: ${response.code()} - ${response.errorBody()?.string()}")
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@CustomerDashboard, CustomerViewCardActivity::class.java)
                            intent.putExtra(CustomerViewCardActivity.EXTRA_PROFESSIONAL_ID, professional.id)
                            startActivity(intent)
                        }
                    }
                } catch (e: Exception) {
                    // ... existing error handling
                }
            }
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CustomerDashboard)
            adapter = professionalsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh professionals list
            loadProfessionals()
        }
        
        // Set colors for the refresh animation
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorPrimaryDark
        )
    }

    private fun setupClickListeners() {
        // Remove the filter button click listener section
        
        // Bottom navigation buttons
        homeButton.setOnClickListener {
            // Already on home screen
            Toast.makeText(this, "Already on Home screen", Toast.LENGTH_SHORT).show()
        }

        calendarButton.setOnClickListener {
            // Navigate to appointments screen
            Toast.makeText(this, "Appointments feature coming soon", Toast.LENGTH_SHORT).show()
        }

        settingsNavButton.setOnClickListener {
            // Navigate to settings screen
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }

        profileButton.setOnClickListener {
            // Navigate to profile screen
            val intent = Intent(this, CustomerProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, get location
            getUserLocation()
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = com.example.skillmatch.models.Location(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                    
                    // Save user location to backend
                    saveUserLocation()
                    
                    // Load professionals sorted by distance
                    loadProfessionals()
                } ?: run {
                    // If location is null, just load professionals without sorting
                    loadProfessionals()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting location", e)
                // If there's an error getting location, just load professionals without sorting
                loadProfessionals()
            }
    }

    private fun saveUserLocation() {
        val userId = sessionManager.getUserId() ?: return
        
        userLocation?.let { location ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiClient.apiService.updateLocation(
                        userId,
                        location
                    )
                    
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to save location: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception saving location", e)
                }
            }
        }
    }

   private fun loadProfessionals() {
       // Show the refresh indicator
       swipeRefreshLayout.isRefreshing = true

       CoroutineScope(Dispatchers.IO).launch {
           try {
               // Get all users
               val response = ApiClient.apiService.getAllUsers()

               if (response.isSuccessful && response.body() != null) {
                   // Filter users to get only professionals
                   val allUsers = response.body()!!
                   val professionalUsers = allUsers.filter { it.role == "SERVICE_PROVIDER" }

                   // Convert User objects to Professional objects
                   val professionalsList = professionalUsers.map { user ->
                       // Extract occupation from bio or occupation field
                       val occupation = when {
                           !user.occupation.isNullOrBlank() -> user.occupation
                           !user.bio.isNullOrBlank() -> extractOccupationFromBio(user.bio)
                           else -> "Professional"
                       }

                       // Parse available days and hours from user or service data
                       val availableDays = user.availableDays?.split(",")?.map { it.trim() } ?: emptyList()
                       val availableHours = user.availableHours ?: ""

                       // Try to get service data for this professional
                       val services = try {
                           // Get token from SharedPreferences
                           val sharedPreferences = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                           val token = sharedPreferences.getString("token", "") ?: ""

                           val portfolioResponse = ApiClient.apiService.getPortfolio("Bearer $token", user.id.toString())
                           if (portfolioResponse.isSuccessful && portfolioResponse.body() != null) {
                               val portfolio = portfolioResponse.body()!!
                               Log.d(TAG, "Portfolio for user ${user.id}: ${portfolio.servicesOffered?.size ?: 0} services")
                               portfolio.servicesOffered ?: emptyList()
                           } else {
                               Log.e(TAG, "Portfolio fetch failed: ${portfolioResponse.code()} - ${portfolioResponse.errorBody()?.string()}")
                               emptyList()
                           }
                       } catch (e: Exception) {
                           Log.e(TAG, "Error fetching portfolio for user ${user.id}", e)
                           emptyList()
                       }

                       // Debug logging for services
                       Log.d(TAG, "Services for user ${user.id}: ${services.size}")
                       services.forEach { service ->
                           Log.d(TAG, "Service: ${service.name}, days: ${service.daysOfTheWeek}, time: ${service.time}")
                       }

                       // Collect all available days from all services - handle null safely
                       val allServiceDays = services.flatMap { service -> 
                           service.daysOfTheWeek ?: emptyList() 
                       }.distinct()

                       // Collect all available hours from services
                       val allServiceHours = services.mapNotNull { it.time }
                           .filter { it.isNotEmpty() }
                           .joinToString(", ")

                       Log.d(TAG, "User ${user.id} has ${allServiceDays.size} service days and hours: $allServiceHours")

                       // Use service data for days and hours if available, otherwise use user data
                       val finalAvailableDays = when {
                           allServiceDays.isNotEmpty() -> {
                               Log.d(TAG, "Using combined service days for user ${user.id}: $allServiceDays")
                               allServiceDays
                           }
                           availableDays.isNotEmpty() -> {
                               Log.d(TAG, "Using user days for user ${user.id}: $availableDays")
                               availableDays
                           }
                           else -> {
                               Log.d(TAG, "No available days found for user ${user.id}")
                               emptyList()
                           }
                       }

                       val finalAvailableHours = when {
                           allServiceHours.isNotEmpty() -> {
                               Log.d(TAG, "Using combined service hours for user ${user.id}: $allServiceHours")
                               allServiceHours
                           }
                           availableHours.isNotEmpty() -> {
                               Log.d(TAG, "Using user hours for user ${user.id}: $availableHours")
                               availableHours
                           }
                           else -> {
                               Log.d(TAG, "No available hours found for user ${user.id}")
                               ""
                           }
                       }

                       Professional(
                           id = user.id?.toLong() ?: 0L,
                           firstName = user.firstName ?: "",
                           lastName = user.lastName ?: "",
                           email = user.email ?: "",
                           occupation = occupation,
                           bio = user.bio,
                           phoneNumber = user.phoneNumber,
                           rating = user.rating,
                           profilePicture = user.profilePicture,
                           location = user.location,
                           availableDays = finalAvailableDays,
                           availableHours = finalAvailableHours
                       )
                   }

                   // Sort professionals by distance if user location is available
                   val sortedProfessionals = if (userLocation != null) {
                       professionalsList.sortedBy { professional ->
                           professional.location?.let { location ->
                               calculateDistance(
                                   userLocation!!.latitude,
                                   userLocation!!.longitude,
                                   location.latitude,
                                   location.longitude
                               )
                           } ?: Double.MAX_VALUE
                       }
                   } else {
                       professionalsList
                   }

                   withContext(Dispatchers.Main) {
                       professionals.clear()
                       professionals.addAll(sortedProfessionals)
                       professionalsAdapter.updateProfessionals(professionals)
                       swipeRefreshLayout.isRefreshing = false
                   }
               } else {
                   withContext(Dispatchers.Main) {
                       Log.e(TAG, "Failed to load professionals: ${response.code()} - ${response.errorBody()?.string()}")
                       Toast.makeText(applicationContext, "Failed to load professionals", Toast.LENGTH_SHORT).show()
                       swipeRefreshLayout.isRefreshing = false
                   }
               }
           } catch (e: Exception) {
               Log.e(TAG, "Exception loading professionals", e)
               withContext(Dispatchers.Main) {
                   Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                   swipeRefreshLayout.isRefreshing = false
               }
           }
       }
   }

    // Helper function to extract occupation from bio
    private fun extractOccupationFromBio(bio: String): String {
        // Simple extraction - look for common occupation indicators
        val occupationIndicators = listOf(
            "I am a ", "I'm a ", "I work as a ", "I'm working as a ",
            "I am an ", "I'm an ", "I work as an ", "I'm working as an "
        )
        
        for (indicator in occupationIndicators) {
            val index = bio.indexOf(indicator, ignoreCase = true)
            if (index >= 0) {
                val start = index + indicator.length
                val end = bio.indexOf(".", start).takeIf { it > 0 } 
                    ?: bio.indexOf(",", start).takeIf { it > 0 }
                    ?: bio.indexOf("\n", start).takeIf { it > 0 }
                    ?: minOf(start + 20, bio.length)
                
                if (end > start) {
                    return bio.substring(start, end).trim()
                }
            }
        }
        
        return "Professional" // Default if no occupation found
    }

    // Calculate distance between two points using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c // Distance in km
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getUserLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Professionals will not be sorted by distance.",
                    Toast.LENGTH_SHORT
                ).show()
                // Load professionals without sorting by distance
                loadProfessionals()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload professionals when returning to this screen
        loadProfessionals()
    }
}