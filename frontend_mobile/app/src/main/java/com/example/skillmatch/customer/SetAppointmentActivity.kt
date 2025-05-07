package com.example.skillmatch.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.*
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.collections.get
import kotlin.collections.set
import java.time.LocalTime

class SetAppointmentActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var professionalNameText: TextView
    private lateinit var professionalRoleText: TextView
    private lateinit var ratingText: TextView
    private lateinit var selectedDateText: TextView
    private lateinit var monthYearText: TextView
    private lateinit var notesEditText: EditText
    private lateinit var setAppointmentButton: Button
    private lateinit var editDateButton: ImageButton
    private lateinit var editTimeButton: ImageButton
    private lateinit var selectedTimeText: TextView
    private lateinit var availableDaysText: TextView
    private lateinit var availableTimeText: TextView
    
    // New UI elements for service selection
    private lateinit var serviceSpinner: Spinner
    private lateinit var serviceDetailsText: TextView
    private lateinit var servicePriceText: TextView
    
    private lateinit var sessionManager: SessionManager
    private var professionalId: Long = 0
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTimeSlot: String? = null
    private var portfolio: Portfolio? = null
    
    // List to store services and the selected service
    private var services: List<Service> = emptyList()
    private var selectedService: Service? = null
    
    // Add these with other view declarations
    private lateinit var ratingBar: RatingBar
    
    companion object {
        private const val TAG = "SetAppointment"
        const val EXTRA_PROFESSIONAL_ID = "extra_professional_id"
        const val EXTRA_PROFESSIONAL_NAME = "extra_professional_name"
        const val EXTRA_PROFESSIONAL_ROLE = "extra_professional_role"
        const val EXTRA_PROFESSIONAL_RATING = "extra_professional_rating"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_appointment)
        
        // Initialize SessionManager
        sessionManager = SessionManager(this)
        
        // Get data from intent
        professionalId = intent.getLongExtra(EXTRA_PROFESSIONAL_ID, 0)
        val professionalName = intent.getStringExtra(EXTRA_PROFESSIONAL_NAME) ?: "Professional"
        val professionalRole = intent.getStringExtra(EXTRA_PROFESSIONAL_ROLE) ?: "Service Provider"
        val professionalRating = intent.getFloatExtra(EXTRA_PROFESSIONAL_RATING, 0f)
        
        // Initialize views
        initializeViews()
        
        // Set professional info
        professionalNameText.text = professionalName
        professionalRoleText.text = professionalRole
        ratingText.text = "$professionalRating"
        
        // Set current date
        updateDateDisplay()
        
        // Set up listeners
        setupListeners()
        
        // Fetch portfolio data (if available)
        fetchPortfolioData()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        professionalNameText = findViewById(R.id.professionalNameText)
        professionalRoleText = findViewById(R.id.professionalRoleText)
        ratingText = findViewById(R.id.ratingText)
        ratingBar = findViewById(R.id.ratingBar)
        selectedDateText = findViewById(R.id.selectedDateText)
        monthYearText = findViewById(R.id.monthYearText)
        notesEditText = findViewById(R.id.notesEditText)
        setAppointmentButton = findViewById(R.id.setAppointmentButton)
        editDateButton = findViewById(R.id.editDateButton)
        editTimeButton = findViewById(R.id.editTimeButton)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        
        // Initialize service selection views
        serviceSpinner = findViewById(R.id.serviceSpinner)
        serviceDetailsText = findViewById(R.id.serviceDetailsText)
        servicePriceText = findViewById(R.id.servicePriceText)
        
        // Find availability TextViews in the professional info card
        val availabilityContainer = findViewById<View>(R.id.availabilityContainer)
        if (availabilityContainer != null) {
            availableDaysText = availabilityContainer.findViewById(R.id.availableDaysText)
            availableTimeText = availabilityContainer.findViewById(R.id.availableTimeText)
        }
    }
    
    private fun setupListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            finish()
        }
        
        // Edit date button click listener
        editDateButton.setOnClickListener {
            showDatePicker()
        }
        
        // Edit time button click listener
        editTimeButton.setOnClickListener {
            showTimePicker()
        }
        
        // Set appointment button click listener
        setAppointmentButton.setOnClickListener {
            if (validateAppointmentData()) {
                bookAppointment()
            }
        }
        
        // Service spinner selection listener
        serviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (services.isNotEmpty() && position < services.size) {
                    selectedService = services[position]
                    updateServiceDetails()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedService = null
                serviceDetailsText.visibility = View.GONE
                servicePriceText.visibility = View.GONE
            }
        }
    }
    
    private fun fetchPortfolioData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getPortfolio(
                    "Bearer ${sessionManager.getAuthToken()}",
                    professionalId.toString()
                )
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        portfolio = response.body()
                        portfolio?.let {
                            // Update UI with portfolio data
                            updatePortfolioUI(it)
                            
                            // Fetch services for this portfolio
                            it.id?.let { portfolioId -> 
                                fetchServices(portfolioId)
                                // Also fetch comments to calculate overall rating
                                fetchCommentsAndUpdateRating(portfolioId)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@SetAppointmentActivity,
                            "Failed to load professional data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching portfolio data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SetAppointmentActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun fetchServices(portfolioId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Instead of calling a non-existent services endpoint,
                // we'll get the portfolio and extract services from it
                val token = sessionManager.getAuthToken() ?: ""
                val formattedToken = if (token.startsWith("Bearer ", ignoreCase = true)) {
                    token
                } else {
                    "Bearer $token"
                }
                
                // Get the portfolio which should include services
                val response = ApiClient.apiService.getPortfolio(
                    formattedToken,
                    portfolioId.toString()
                )
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val portfolio = response.body()!!
                        // Extract services from the portfolio
                        services = portfolio.servicesOffered ?: emptyList()
                        setupServiceSpinner()
                    } else {
                        Log.e(TAG, "Failed to load portfolio: ${response.code()} - ${response.errorBody()?.string()}")
                        setupEmptyServiceSpinner()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching portfolio data", e)
                withContext(Dispatchers.Main) {
                    setupEmptyServiceSpinner()
                }
            }
        }
    }
    
    private fun fetchCommentsAndUpdateRating(portfolioId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getAuthToken()
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
                            // Calculate average rating from comments
                            val averageRating = comments.map { it.rating }.average().toFloat()
                            ratingBar.rating = averageRating
                            ratingText.text = String.format("%.1f", averageRating)
                        } else {
                            // No comments, set rating to 0
                            ratingBar.rating = 0f
                            ratingText.text = "0.0"
                        }
                    } else {
                        // Set rating to 0 on error
                        ratingBar.rating = 0f
                        ratingText.text = "0.0"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments", e)
                withContext(Dispatchers.Main) {
                    // Set rating to 0 on error
                    ratingBar.rating = 0f
                    ratingText.text = "0.0"
                }
            }
        }
    }
    
    // Add a helper method to handle the empty services case
    private fun setupEmptyServiceSpinner() {
        val noServices = listOf("No services available")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, noServices)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        serviceSpinner.adapter = adapter
        serviceSpinner.isEnabled = false
        
        // Hide service details
        serviceDetailsText.visibility = View.GONE
        servicePriceText.visibility = View.GONE
    }
    
    private fun setupServiceSpinner() {
        if (services.isEmpty()) {
            // If no services, show a message
            val noServices = listOf("No services available")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, noServices)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            serviceSpinner.adapter = adapter
            serviceSpinner.isEnabled = false
        } else {
            // Create adapter with service names
            val serviceNames = services.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            serviceSpinner.adapter = adapter
            serviceSpinner.isEnabled = true
            
            // Select first service by default
            if (services.isNotEmpty()) {
                selectedService = services[0]
                updateServiceDetails()
            }
        }
    }
    
    private fun updateServiceDetails() {
        selectedService?.let { service ->
            // Show service details
            if (!service.description.isNullOrEmpty()) {
                serviceDetailsText.text = service.description
                serviceDetailsText.visibility = View.VISIBLE
            } else {
                serviceDetailsText.visibility = View.GONE
            }
            
            // Show service price
            if (!service.pricing.isNullOrEmpty()) {
                servicePriceText.text = "₱${service.pricing}"
                servicePriceText.visibility = View.VISIBLE
            } else {
                servicePriceText.visibility = View.GONE
            }
        }
    }
    
    private fun updatePortfolioUI(portfolio: Portfolio) {
        // Update availability information if available
        val availableDays = portfolio.daysAvailable?.joinToString(", ") ?: "Not specified"
        val availableHours = if (!portfolio.startTime.isNullOrEmpty() && !portfolio.endTime.isNullOrEmpty()) {
            // Format to 12-hour format with AM/PM
            val formattedStart = formatTo12HourTime(portfolio.startTime)
            val formattedEnd = formatTo12HourTime(portfolio.endTime)
            "$formattedStart - $formattedEnd"
        } else if (!portfolio.time.isNullOrEmpty()) {
            // Try to format the time string if it contains a range separator
            if (portfolio.time.contains("-")) {
                try {
                    val times = portfolio.time.split("-")
                    if (times.size == 2) {
                        val formattedStart = formatTo12HourTime(times[0].trim())
                        val formattedEnd = formatTo12HourTime(times[1].trim())
                        "$formattedStart - $formattedEnd"
                    } else {
                        portfolio.time
                    }
                } catch (e: Exception) {
                    portfolio.time
                }
            } else {
                portfolio.time
            }
        } else {
            "Not specified"
        }
        
        availableDaysText.text = availableDays
        availableTimeText.text = availableHours
    }

    // Add a helper method to format time to 12-hour format with AM/PM
    private fun formatTo12HourTime(timeString: String): String {
        return try {
            val time = if (timeString.count { it == ':' } > 1) {
                java.time.LocalTime.parse(timeString.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
            } else {
                java.time.LocalTime.parse(timeString.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            }
            time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
        } catch (e: Exception) {
            timeString // Return original if parsing fails
        }
    }
    
    private fun showTimePicker() {
        // Get current hour and minute from selectedDate
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        // Create TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minuteOfHour ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minuteOfHour)
                updateTimeDisplay()
            },
            hour,
            minute,
            false // 12-hour format
        )

        timePickerDialog.show()
    }

    private fun updateTimeDisplay() {
        // Use "h:mm a" pattern to ensure 12-hour format with AM/PM indicator
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        selectedTimeText.text = timeFormat.format(selectedDate.time)
    }
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
                
                // Validate selected date against professional's availability
                portfolio?.let { portfolio ->
                    val daysAvailable = portfolio.daysAvailable
                    
                    // If "Everyday" is in the list, any day is valid
                    if (daysAvailable.any { it.equals("Everyday", ignoreCase = true) }) {
                        // All days are available, no validation needed
                        return@let
                    }
                    
                    val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
                    val dayName = when (dayOfWeek) {
                        Calendar.MONDAY -> "monday"
                        Calendar.TUESDAY -> "tuesday"
                        Calendar.WEDNESDAY -> "wednesday"
                        Calendar.THURSDAY -> "thursday"
                        Calendar.FRIDAY -> "friday"
                        Calendar.SATURDAY -> "saturday"
                        Calendar.SUNDAY -> "sunday"
                        else -> ""
                    }
                    
                    if (daysAvailable.isNotEmpty() && !daysAvailable.map { it.lowercase() }.contains(dayName)) {
                        Toast.makeText(this, "Warning: Professional is not available on ${dayName.capitalize(Locale.ROOT)}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today to prevent booking in the past
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        selectedDateText.text = dateFormat.format(selectedDate.time)
        monthYearText.text = monthYearFormat.format(selectedDate.time)
    }
    
    private fun validateAppointmentData(): Boolean {
        // Check if time is selected
        if (selectedDate.get(Calendar.HOUR_OF_DAY) == 0 && selectedDate.get(Calendar.MINUTE) == 0) {
            Toast.makeText(this, "Please select a time for the appointment", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Check if service is selected (if services are available)
        if (services.isNotEmpty() && selectedService == null) {
            Toast.makeText(this, "Please select a service", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun bookAppointment() {
        // Get the current user ID from session
        val userId = sessionManager.getUserId()?.toLong() ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Format the selected date and time
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val appointmentDateTime = LocalDateTime.of(
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH) + 1,
            selectedDate.get(Calendar.DAY_OF_MONTH),
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE)
        )
        val formattedDateTime = appointmentDateTime.format(dateTimeFormatter)
        
        // Create the appointment request
        val appointmentRequest = AppointmentRequest(
            user = UserReference(userId),
            portfolio = PortfolioReference(portfolio?.id ?: professionalId),
            service = selectedService?.let { ServiceReference(it.id ?: 0L) },  // Add null safety with default value
            role = "CUSTOMER",
            appointmentTime = formattedDateTime,
            notes = notesEditText.text.toString()
        )
        
        // Show loading indicator
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Booking appointment...")
            setCancelable(false)
            show()
        }
        
        // Make API call to book appointment
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.bookAppointment(appointmentRequest)
                
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SetAppointmentActivity,
                            "Appointment booked successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@SetAppointmentActivity,
                            "Failed to book appointment: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error booking appointment", e)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SetAppointmentActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

