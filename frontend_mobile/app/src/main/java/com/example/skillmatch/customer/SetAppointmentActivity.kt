package com.example.skillmatch.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.AppointmentRequest
import com.example.skillmatch.models.Portfolio
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
    
    private lateinit var sessionManager: SessionManager
    private var professionalId: Long = 0
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTimeSlot: String? = null
    private var portfolio: Portfolio? = null
    
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
        val professionalRating = intent.getFloatExtra(EXTRA_PROFESSIONAL_RATING, 0.0f)
        
        if (professionalId == 0L) {
            Toast.makeText(this, "Error: Professional not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Set initial professional info from intent
        professionalNameText.text = professionalName
        professionalRoleText.text = professionalRole
        ratingText.text = String.format("%.1f", professionalRating)
        
        // Set current date and time
        updateDateDisplay()
        updateTimeDisplay()
        
        // Set up listeners
        setupListeners()
        
        // Fetch professional portfolio data
        fetchProfessionalPortfolio()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        professionalNameText = findViewById(R.id.professionalNameText)
        professionalRoleText = findViewById(R.id.professionalRoleText)
        ratingText = findViewById(R.id.ratingText)
        selectedDateText = findViewById(R.id.selectedDateText)
        monthYearText = findViewById(R.id.monthYearText)
        notesEditText = findViewById(R.id.notesEditText)
        setAppointmentButton = findViewById(R.id.setAppointmentButton)
        editDateButton = findViewById(R.id.editDateButton)
        editTimeButton = findViewById(R.id.editTimeButton)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        
        // Find availability TextViews in the professional info card
        val availabilityContainer = findViewById<View>(R.id.availabilityContainer)
        availableDaysText = availabilityContainer.findViewById(R.id.availableDaysText)
        availableTimeText = availabilityContainer.findViewById(R.id.availableTimeText)
    }
    
    private fun fetchProfessionalPortfolio() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "Token is missing or empty")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SetAppointmentActivity, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                val response = ApiClient.apiService.getPortfolio("Bearer $token", professionalId.toString())
                
                if (response.isSuccessful && response.body() != null) {
                    portfolio = response.body()
                    
                    withContext(Dispatchers.Main) {
                        updatePortfolioUI()
                    }
                } else {
                    Log.e(TAG, "Failed to fetch portfolio: ${response.code()} - ${response.message()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SetAppointmentActivity, "Could not load professional's portfolio. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching portfolio", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SetAppointmentActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updatePortfolioUI() {
        portfolio?.let { portfolio ->
            // Update available days
            val daysAvailable = portfolio.daysAvailable
            if (daysAvailable.isNotEmpty()) {
                val formattedDays = daysAvailable.joinToString(", ") { it.capitalize(Locale.ROOT) }
                availableDaysText.text = formattedDays
            }
            
            // Update available time
            if (!portfolio.time.isNullOrEmpty()) {
                availableTimeText.text = portfolio.time
            }
        }
    }
    
    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }
        
        // Edit date button
        editDateButton.setOnClickListener {
            showDatePicker()
        }
        
        // Edit time button
        editTimeButton.setOnClickListener {
            showTimePicker()
        }
        
        // Set appointment button
        setAppointmentButton.setOnClickListener {
            bookAppointment()
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
    
    /**
     * Formats the selected date and time into ISO format for the appointment
     * @return String in format "yyyy-MM-dd'T'HH:mm:ss"
     */
    private fun formatDateTimeForAppointment(): String {
        // Create a formatter for ISO date-time format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        
        // Convert Calendar to LocalDateTime
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)
        
        // Create LocalDateTime and format it
        val localDateTime = LocalDateTime.of(year, month, day, hour, minute)
        return localDateTime.format(formatter)
    }
    
    private fun bookAppointment() {
        if (portfolio == null) {
            Toast.makeText(this, "Cannot book appointment: Professional's portfolio not found", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Attempted to book appointment but portfolio is null")
            return
        }
        
        val userId = sessionManager.getUserId()?.toLong() ?: 0L
        if (userId == 0L) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Format the date and time for the appointment
        val dateTime = formatDateTimeForAppointment()
        
        // Get portfolio ID and ensure it's a non-nullable Long
        val portfolioId = portfolio?.id ?: 0L
        
        // Create the appointment request
        val appointmentRequest = AppointmentRequest(
            userId = userId,
            role = "CUSTOMER", // Assuming the role is CUSTOMER when booking
            portfolioId = portfolioId, // Use the extracted non-nullable Long
            appointmentTime = dateTime,
            notes = notesEditText.text.toString().trim()
        )
        
        // Show loading state
        setAppointmentButton.isEnabled = false
        setAppointmentButton.text = "Booking..."
        
        // Make the API call
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SetAppointmentActivity, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
                        setAppointmentButton.isEnabled = true
                        setAppointmentButton.text = "Set Appointment"
                    }
                    return@launch
                }
                
                Log.d(TAG, "Sending appointment request: $appointmentRequest")
                val response = ApiClient.apiService.bookAppointment("Bearer $token", appointmentRequest)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SetAppointmentActivity, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to previous screen
                    } else {
                        val errorCode = response.code()
                        val errorMessage = when (errorCode) {
                            404 -> "Professional's portfolio not found. Please try again later."
                            400 -> "Invalid appointment details. Please check and try again."
                            else -> "Failed to book appointment: Error $errorCode"
                        }
                        Log.e(TAG, "Appointment booking failed: $errorCode - ${response.message()}")
                        Toast.makeText(this@SetAppointmentActivity, errorMessage, Toast.LENGTH_LONG).show()
                        setAppointmentButton.isEnabled = true
                        setAppointmentButton.text = "Set Appointment"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error booking appointment", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SetAppointmentActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    setAppointmentButton.isEnabled = true
                    setAppointmentButton.text = "Set Appointment"
                }
            }
        }
    }
}

