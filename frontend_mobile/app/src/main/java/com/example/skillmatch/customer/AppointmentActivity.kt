package com.example.skillmatch.customer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.skillmatch.R
import com.example.skillmatch.adapters.AppointmentAdapter

import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.AppointmentResponse
import com.example.skillmatch.models.CommentRequest
import com.example.skillmatch.professional.ProfessionalProfileActivity
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AppointmentActivity : AppCompatActivity(), AppointmentAdapter.AppointmentClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        // Initialize SessionManager and get userId
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()?.toLong() ?: 0

        // Set up RecyclerView
        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        appointmentAdapter = AppointmentAdapter(emptyList(), this)
        recyclerView.adapter = appointmentAdapter

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadAppointments()
        }

        // Load appointments
        loadAppointments()

        // Set up back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    
        // Set up bottom navigation
        findViewById<View>(R.id.homeButton).setOnClickListener {
            // Navigate to home/dashboard based on user role
            val userRole = sessionManager.getUserRole()
            if (userRole == "PROFESSIONAL" || userRole == "SERVICE PROVIDER") {
                val intent = Intent(this, ProfessionalProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                val intent = Intent(this, CustomerDashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            finish()
        }
        
        findViewById<View>(R.id.calendarButton).setOnClickListener {
            // Already on appointments screen, do nothing or refresh
            loadAppointments()
        }
        
        findViewById<View>(R.id.settingsNavButton).setOnClickListener {
            // Navigate to settings
            val intent = Intent(this, CustomerSettingsActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<View>(R.id.profileButton).setOnClickListener {
            // Navigate to profile based on user role
            val userRole = sessionManager.getUserRole()
            if (userRole == "PROFESSIONAL" || userRole == "SERVICE PROVIDER") {
                val intent = Intent(this, ProfessionalProfileActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, CustomerProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadAppointments() {
        // Show loading state
        findViewById<View>(R.id.emptyStateLayout).visibility = View.GONE
        
        // Get authentication token and role
        val token = sessionManager.getToken()
        val userRole = sessionManager.getUserRole()
        
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            showEmptyState("Authentication error. Please log in again.")
            return
        }
        
        // Call the appropriate API based on user role
        val call = if (userRole == "PROFESSIONAL" || userRole == "SERVICE PROVIDER") {
            // Use the all endpoint for professionals which includes both customer and provider appointments
            ApiClient.apiService.getAllAppointmentsForProfessional("Bearer $token", userId)
        } else {
            // Use the user endpoint for customers
            ApiClient.apiService.getAllAppointmentsForUser("Bearer $token", userId, userRole.toString())
        }
        
        call.enqueue(object : Callback<List<AppointmentResponse>> {
            override fun onResponse(call: Call<List<AppointmentResponse>>, response: Response<List<AppointmentResponse>>) {
                if (response.isSuccessful) {
                    val appointments = response.body() ?: emptyList()
                    appointmentAdapter.updateAppointments(appointments)
                    
                    // Show empty state if no appointments
                    if (appointments.isEmpty()) {
                        showEmptyState("No Appointments Yet")
                    } else {
                        findViewById<View>(R.id.emptyStateLayout).visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    // Handle error response
                    val errorMessage = when (response.code()) {
                        401 -> "Authentication failed. Please log in again."
                        403 -> "You don't have permission to view these appointments."
                        404 -> "No appointments found."
                        else -> "Failed to load appointments: ${response.code()}"
                    }
                    Toast.makeText(this@AppointmentActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    
                    // Show empty state with error
                    showEmptyState(errorMessage)
                }
            }

            override fun onFailure(call: Call<List<AppointmentResponse>>, t: Throwable) {
                // Handle network failure
                Toast.makeText(this@AppointmentActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                
                // Show empty state with error
                showEmptyState("Failed to load appointments")
                
                // Log the error for debugging
                Log.e("AppointmentActivity", "Network error loading appointments", t)
            }
        })
    }

    // Add this helper method to show empty state with custom message
    private fun showEmptyState(message: String) {
        val emptyStateLayout = findViewById<View>(R.id.emptyStateLayout)
        emptyStateLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        
        // Find the TextView in your empty state layout that shows the main message
        // Use the correct ID from your layout file
        val titleTextView = emptyStateLayout.findViewById<TextView>(R.id.empty)
        if (titleTextView != null) {
            titleTextView.text = message
        }
    }

    override fun onAppointmentClick(appointment: AppointmentResponse) {
        // Open appointment detail dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_appointment_detail, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set appointment details in the dialog
        dialogView.findViewById<TextView>(R.id.appointmentIdText).text = "Appointment #${appointment.id}"
        dialogView.findViewById<TextView>(R.id.appointmentStatusText).text = "Status: ${appointment.status}"
        dialogView.findViewById<TextView>(R.id.appointmentTimeText).text = "Time: ${formatDateTime(appointment.appointmentTime)}"
        dialogView.findViewById<TextView>(R.id.appointmentNotesText).text = "Notes: ${appointment.notes ?: "No notes"}"

        // Set up action buttons based on appointment status
        val rescheduleButton = dialogView.findViewById<Button>(R.id.rescheduleButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val rateButton = dialogView.findViewById<Button>(R.id.rateButton)
        
        // Only show reschedule/cancel buttons if appointment is not completed or canceled
        if (appointment.status == "COMPLETED" || appointment.status == "CANCELED") {
            rescheduleButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            
            // Show rate button for completed appointments that haven't been rated
            if (appointment.status == "COMPLETED" && appointment.rated != true) {
                rateButton.visibility = View.VISIBLE
                rateButton.setOnClickListener {
                    dialog.dismiss()
                    showRatingDialog(appointment)
                }
            } else {
                rateButton.visibility = View.GONE
            }
        } else {
            // Set up reschedule button
            rescheduleButton.setOnClickListener {
                dialog.dismiss()
                showRescheduleDateTimePicker(appointment)
            }
            
            // Set up cancel button
            cancelButton.setOnClickListener {
                dialog.dismiss()
                showCancelConfirmation(appointment)
            }
            
            // Hide rate button for non-completed appointments
            rateButton.visibility = View.GONE
        }

        // Close button
        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    
    override fun onRateAppointmentClick(appointment: AppointmentResponse) {
        showRatingDialog(appointment)
    }
    
    private fun showRatingDialog(appointment: AppointmentResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rate_professional, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            
        // Set professional name
        val professionalName = "${appointment.providerFirstName ?: ""} ${appointment.providerLastName ?: ""}"
        dialogView.findViewById<TextView>(R.id.professionalNameText).text = professionalName
        
        // Get rating and comment views
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)
        
        // Set up buttons
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.submitButton).setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = commentEditText.text.toString().trim()
            
            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Submit rating and comment
            submitRatingAndComment(appointment, rating, comment)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun submitRatingAndComment(appointment: AppointmentResponse, rating: Int, comment: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getAuthToken()
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AppointmentActivity, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                // Create comment request
                val commentRequest = CommentRequest(
                    message = comment,
                    rating = rating
                )
                
                // Use the portfolioId directly from the appointment
                val portfolioId = appointment.portfolioId
                
                if (portfolioId != null && portfolioId > 0) {
                    // Submit comment to the professional's portfolio using the portfolio ID from appointment
                    val response = ApiClient.apiService.addComment(
                        "Bearer $token",
                        userId.toString(),
                        portfolioId.toString(),
                        commentRequest
                    )
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AppointmentActivity, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                            
                            // Mark appointment as rated
                            markAppointmentAsRated(appointment.id)
                        } else {
                            Toast.makeText(this@AppointmentActivity, "Failed to submit rating: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.e("AppointmentActivity", "Error response: ${response.errorBody()?.string()}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AppointmentActivity, "Invalid portfolio ID for this provider", Toast.LENGTH_SHORT).show()
                        Log.e("AppointmentActivity", "Invalid portfolio ID: $portfolioId")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppointmentActivity", "Error submitting rating", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AppointmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun markAppointmentAsRated(appointmentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.getAuthToken()
                if (token.isNullOrEmpty()) {
                    return@launch
                }
                
                val response = ApiClient.apiService.markAppointmentAsRated(
                    "Bearer $token",
                    appointmentId
                )
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Refresh appointments list
                        loadAppointments()
                    }
                }
            } catch (e: Exception) {
                Log.e("AppointmentActivity", "Error marking appointment as rated", e)
            }
        }
    }

    private fun showRescheduleDateTimePicker(appointment: AppointmentResponse) {
        val calendar = Calendar.getInstance()
        
        // Show date picker
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                // After date is selected, show time picker
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        // Format the new date time
                        val newDateTime = LocalDateTime.of(
                            year, month + 1, dayOfMonth, hourOfDay, minute
                        )
                        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        val formattedDateTime = newDateTime.format(formatter)
                        
                        // Call API to reschedule
                        rescheduleAppointment(appointment.id, formattedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun rescheduleAppointment(appointmentId: Long, newTime: String) {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        ApiClient.apiService.rescheduleAppointment("Bearer $token", appointmentId, newTime).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AppointmentActivity, "Appointment rescheduled successfully", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Refresh the list
                } else {
                    Toast.makeText(this@AppointmentActivity, "Failed to reschedule appointment", Toast.LENGTH_SHORT).show()
                }
            }
    
            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                Toast.makeText(this@AppointmentActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCancelConfirmation(appointment: AppointmentResponse) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { _, _ ->
                // Call the cancelAppointment method
                cancelAppointment(appointment.id)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelAppointment(appointmentId: Long) {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        ApiClient.apiService.cancelAppointment("Bearer $token", appointmentId).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AppointmentActivity, "Appointment canceled successfully", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Refresh the list
                } else {
                    Toast.makeText(this@AppointmentActivity, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                Toast.makeText(this@AppointmentActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatDateTime(dateTimeString: String): String {
        try {
            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
            val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
            return dateTime.format(outputFormatter)
        } catch (e: Exception) {
            return dateTimeString
        }
    }
}