package com.example.skillmatch.customer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Base64
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
import com.bumptech.glide.Glide
import com.example.skillmatch.R
import com.example.skillmatch.adapter.ProfessionalsAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Professional
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
                    
                    // Always navigate to the detail view, regardless of portfolio fetch success
                    val intent = Intent(this@CustomerDashboard, CustomerViewCardActivity::class.java)
                    intent.putExtra(CustomerViewCardActivity.EXTRA_PROFESSIONAL_ID, professional.id)
                    
                    try {
                        val portfolioResponse = ApiClient.apiService.getPortfolio("Bearer $token", professional.id.toString())
                        if (portfolioResponse.isSuccessful && portfolioResponse.body() != null) {
                            val services = portfolioResponse.body()!!.servicesOffered ?: emptyList()
                            intent.putParcelableArrayListExtra(
                                "EXTRA_SERVICES_LIST",
                                ArrayList(services)
                            )
                        } else {
                            // Log the issue but continue with navigation
                            if (portfolioResponse.code() == 404) {
                                Log.w(TAG, "No portfolio found for professional ${professional.id} - proceeding without services")
                            } else {
                                Log.e(TAG, "Portfolio fetch failed: ${portfolioResponse.code()} - ${portfolioResponse.errorBody()?.string()}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching portfolio", e)
                    }
                    
                    // Start the activity with whatever data we have
                    withContext(Dispatchers.Main) {
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to professional details", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CustomerDashboard,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }

        settingsNavButton.setOnClickListener {
            // Navigate to settings screen
            val intent = Intent(this, CustomerSettingsActivity::class.java)
            startActivity(intent)
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
                   val professionalUsers = allUsers.filter { it.role == "SERVICE PROVIDER" }

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

                       // Get token from SessionManager instead of SharedPreferences
                       val token = sessionManager.getAuthToken()
                       
                       // Try to get service data for this professional
                       val services = try {
                           if (token.isNullOrEmpty()) {
                               Log.e(TAG, "Token is null or empty when fetching portfolio")
                               emptyList()
                           } else {
                               val portfolioResponse = ApiClient.apiService.getPortfolio("Bearer $token", user.id.toString())
                               if (portfolioResponse.isSuccessful && portfolioResponse.body() != null) {
                                   val portfolio = portfolioResponse.body()!!
                                   Log.d(TAG, "Portfolio for user ${user.id}: ${portfolio.servicesOffered?.size ?: 0} services")
                                   portfolio.servicesOffered ?: emptyList()
                               } else {
                                   // Log the error but continue with empty services
                                   when (portfolioResponse.code()) {
                                       403 -> Log.e(TAG, "Permission denied (403) when fetching portfolio for user ${user.id}")
                                       404 -> Log.d(TAG, "No portfolio found (404) for user ${user.id} - this is normal for new users")
                                       else -> Log.e(TAG, "Portfolio fetch failed: ${portfolioResponse.code()} - ${portfolioResponse.errorBody()?.string()}")
                                   }
                                   emptyList()
                               }
                           }
                       } catch (e: Exception) {
                           Log.e(TAG, "Error fetching portfolio for user ${user.id}", e)
                           emptyList()
                       }

                       // Debug logging for services
                       Log.d(TAG, "Services for user ${user.id}: ${services.size}")
                       services.forEach { service ->
                           Log.d(TAG, "Service: ${service.name}")
                       }

                       // Get portfolio data to access availability information
                       val portfolio = try {
                           if (token.isNullOrEmpty()) {
                               Log.e(TAG, "Token is null or empty when fetching portfolio for availability")
                               null
                           } else {
                               val portfolioResponse = ApiClient.apiService.getPortfolio("Bearer $token", user.id.toString())
                               if (portfolioResponse.isSuccessful && portfolioResponse.body() != null) {
                                   portfolioResponse.body()
                               } else {
                                   // Don't log errors again since we already logged them above
                                   null
                               }
                           }
                       } catch (e: Exception) {
                           Log.e(TAG, "Error fetching portfolio for availability", e)
                           null
                       }

                       // Get days and time from portfolio if available
                       val portfolioDaysAvailable = portfolio?.daysAvailable ?: emptyList()
                       val portfolioStartTime = portfolio?.startTime
                       val portfolioEndTime = portfolio?.endTime
                       val portfolioTime = portfolio?.time

                       Log.d(TAG, "User ${user.id} portfolio days: ${portfolioDaysAvailable.size}, startTime: $portfolioStartTime, endTime: $portfolioEndTime, time: $portfolioTime")

                       // Use portfolio data for days and hours if available, otherwise use user data
                       val finalAvailableDays = when {
                           portfolioDaysAvailable.isNotEmpty() -> {
                               Log.d(TAG, "Using portfolio days for user ${user.id}: $portfolioDaysAvailable")
                               formatDaysOfWeekCompact(portfolioDaysAvailable)
                           }
                           availableDays.isNotEmpty() -> {
                               Log.d(TAG, "Using user days for user ${user.id}: $availableDays")
                               formatDaysOfWeekCompact(availableDays)
                           }
                           else -> {
                               Log.d(TAG, "No available days found for user ${user.id}")
                               "Not specified"
                           }
                       }

                       val finalAvailableHours = when {
                           // First check for startTime and endTime (new format)
                           portfolioStartTime != null && portfolioEndTime != null -> {
                               val formattedStartTime = formatTo12HourTime(portfolioStartTime)
                               val formattedEndTime = formatTo12HourTime(portfolioEndTime)
                               val timeRange = "$formattedStartTime - $formattedEndTime"
                               Log.d(TAG, "Using portfolio startTime-endTime for user ${user.id}: $timeRange")
                               timeRange
                           }
                           // For backward compatibility, check time field
                           !portfolioTime.isNullOrEmpty() -> {
                               // Try to format the time string if it contains a range separator
                               if (portfolioTime.contains("-")) {
                                   try {
                                       val times = portfolioTime.split("-")
                                       if (times.size == 2) {
                                           val formattedStartTime = formatTo12HourTime(times[0].trim())
                                           val formattedEndTime = formatTo12HourTime(times[1].trim())
                                           "$formattedStartTime - $formattedEndTime"
                                       } else {
                                           portfolioTime
                                       }
                                   } catch (e: Exception) {
                                       Log.e(TAG, "Error formatting time range", e)
                                       portfolioTime
                                   }
                               } else {
                                   portfolioTime
                               }
                           }
                           availableHours.isNotEmpty() -> {
                               // Try to format the time string if it contains a range separator
                               if (availableHours.contains("-")) {
                                   try {
                                       val times = availableHours.split("-")
                                       if (times.size == 2) {
                                           val formattedStartTime = formatTo12HourTime(times[0].trim())
                                           val formattedEndTime = formatTo12HourTime(times[1].trim())
                                           "$formattedStartTime - $formattedEndTime"
                                       } else {
                                           availableHours
                                       }
                                   } catch (e: Exception) {
                                       Log.e(TAG, "Error formatting time range", e)
                                       availableHours
                                   }
                               } else {
                                   availableHours
                               }
                           }
                           else -> {
                               ""
                           }
                       }

                       Log.d(TAG, "User: ${user.firstName} ${user.lastName}, ProfilePicture: ${user.profilePicture}")
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
                           availableDays = finalAvailableDays.split(", "),
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

    // Add this helper method to format time to 12-hour format with AM/PM
    private fun formatTo12HourTime(timeString: String): String {
        try {
            // Parse the time string (handling both HH:mm and HH:mm:ss formats)
            val time = if (timeString.contains(":")) {
                if (timeString.count { it == ':' } > 1) {
                    // Format is HH:mm:ss
                    java.time.LocalTime.parse(timeString.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                } else {
                    // Format is HH:mm
                    java.time.LocalTime.parse(timeString.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                }
            } else {
                java.time.LocalTime.parse(timeString.trim())
            }
            
            // Format to 12-hour format with AM/PM
            return time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: $timeString", e)
            return timeString // Return original if parsing fails
        }
    }
    
    // Add this helper method to format days of week in a compact way
    private fun formatDaysOfWeekCompact(days: List<String>): String {
        if (days.isEmpty()) return "Not specified"
        
        // Sort days in correct order (Monday first)
        val sortedDays = days.sortedBy { 
            when (it.trim().uppercase()) {
                "MONDAY" -> 1
                "TUESDAY" -> 2
                "WEDNESDAY" -> 3
                "THURSDAY" -> 4
                "FRIDAY" -> 5
                "SATURDAY" -> 6
                "SUNDAY" -> 7
                else -> 8
            }
        }
        
        return sortedDays.joinToString(", ")
    }
    
    // Add this helper method to extract occupation from bio
    private fun extractOccupationFromBio(bio: String): String {
        // Simple implementation - you can enhance this based on your needs
        return if (bio.length > 20) bio.substring(0, 20) + "..." else bio
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

/**
 * Formats a list of days into a compact, readable format
 */
private fun formatDaysOfWeekCompact(days: List<String>): String {
    if (days.isEmpty()) return "Not specified"
    
    // Map of full day names to their abbreviated forms and their order in the week
    val dayMap = mapOf(
        "monday" to Pair("Mon", 1),
        "tuesday" to Pair("Tue", 2),
        "wednesday" to Pair("Wed", 3),
        "thursday" to Pair("Thu", 4),
        "friday" to Pair("Fri", 5),
        "saturday" to Pair("Sat", 6),
        "sunday" to Pair("Sun", 7)
    )
    
    // Convert days to their abbreviated forms and sort them by day order
    val sortedDays = days.mapNotNull { day ->
        dayMap[day.trim().lowercase()]
    }.sortedBy { it.second }
    
    if (sortedDays.isEmpty()) return "Not specified"
    
    // Check for common patterns
    val weekdays = listOf(1, 2, 3, 4, 5)
    val weekend = listOf(6, 7)
    
    val weekdaysPresent = weekdays.all { dayNum -> sortedDays.any { it.second == dayNum } }
    val weekendPresent = weekend.all { dayNum -> sortedDays.any { it.second == dayNum } }
    
    return when {
        // All days
        sortedDays.size == 7 -> "All days"
        
        // Weekdays + weekend
        weekdaysPresent && weekendPresent -> "All days"
        
        // Just weekdays
        weekdaysPresent && sortedDays.size == 5 -> "Weekdays"
        
        // Just weekend
        weekendPresent && sortedDays.size == 2 -> "Weekend"
        
        // Check for consecutive days to use ranges
        else -> {
            val ranges = mutableListOf<Pair<String, String>>()
            var rangeStart = sortedDays.first()
            var prevDay = sortedDays.first()
            
            for (i in 1 until sortedDays.size) {
                val currentDay = sortedDays[i]
                if (currentDay.second != prevDay.second + 1) {
                    // End of a range
                    if (rangeStart.second != prevDay.second) {
                        ranges.add(Pair(rangeStart.first, prevDay.first))
                    } else {
                        ranges.add(Pair(rangeStart.first, rangeStart.first))
                    }
                    rangeStart = currentDay
                }
                prevDay = currentDay
            }
            
            // Add the last range
            if (rangeStart.second != prevDay.second) {
                ranges.add(Pair(rangeStart.first, prevDay.first))
            } else {
                ranges.add(Pair(rangeStart.first, rangeStart.first))
            }
            
            // Format the ranges
            ranges.joinToString(", ") { (start, end) ->
                if (start == end) start else "$start-$end"
            }
        }
    }
}