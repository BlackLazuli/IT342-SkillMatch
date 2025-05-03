package com.example.skillmatch.professional

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.adapters.AppointmentAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.AppointmentResponse
import com.example.skillmatch.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AppointmentProfessionalActivity : AppCompatActivity(), AppointmentAdapter.AppointmentClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var sessionManager: SessionManager
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_professional)

        // Initialize SessionManager and get userId
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()?.toLong() ?: 0

        // Set up RecyclerView
        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        appointmentAdapter = AppointmentAdapter(emptyList(), this)
        recyclerView.adapter = appointmentAdapter

        // Load appointments
        loadAppointments()

        // Set up back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }
    
        // Set up bottom navigation
        findViewById<View>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, ProfessionalProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        findViewById<View>(R.id.calendarButton).setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<View>(R.id.settingsNavButton).setOnClickListener {
            // Navigate to settings
            Toast.makeText(this, "Settings feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<View>(R.id.profileButton).setOnClickListener {
            val intent = Intent(this, ProfessionalProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadAppointments() {
        // Show loading state
        findViewById<View>(R.id.emptyStateLayout).visibility = View.GONE
        
        // Get authentication token
        val token = sessionManager.getToken()
        
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            showEmptyState("Authentication error. Please log in again.")
            return
        }
        
        // Call the API for professionals
        val call = ApiClient.apiService.getAllAppointmentsForProfessional("Bearer $token", userId)
        
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
                    Toast.makeText(this@AppointmentProfessionalActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    
                    // Show empty state with error
                    showEmptyState(errorMessage)
                }
            }

            override fun onFailure(call: Call<List<AppointmentResponse>>, t: Throwable) {
                // Handle network failure
                Toast.makeText(this@AppointmentProfessionalActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                
                // Show empty state with error
                showEmptyState("Failed to load appointments")
                
                // Log the error for debugging
                Log.e("AppointmentProfessionalActivity", "Network error loading appointments", t)
            }
        })
    }

    // Helper method to show empty state with custom message
    private fun showEmptyState(message: String) {
        val emptyStateLayout = findViewById<View>(R.id.emptyStateLayout)
        emptyStateLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        
        // Find the TextView in your empty state layout that shows the main message
        val titleTextView = emptyStateLayout.findViewById<TextView>(R.id.empty)
        if (titleTextView != null) {
            titleTextView.text = message
        }
    }

    override fun onAppointmentClick(appointment: AppointmentResponse) {
        // Open appointment detail dialog for professionals
        val dialogView = layoutInflater.inflate(R.layout.dialog_appointment_detail_professional, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Set appointment details in the dialog
        dialogView.findViewById<TextView>(R.id.appointmentIdText).text = "Appointment #${appointment.id}"
        dialogView.findViewById<TextView>(R.id.appointmentStatusText).text = "Status: ${appointment.status}"
        dialogView.findViewById<TextView>(R.id.appointmentTimeText).text = "Time: ${formatDateTime(appointment.appointmentTime)}"
        dialogView.findViewById<TextView>(R.id.clientNameText).text = "Client: ${appointment.userFirstName} ${appointment.userLastName}"
        dialogView.findViewById<TextView>(R.id.appointmentNotesText).text = "Notes: ${appointment.notes ?: "No notes"}"

        // Set up action buttons based on appointment status
        val rescheduleButton = dialogView.findViewById<android.widget.Button>(R.id.rescheduleButton)
        val cancelButton = dialogView.findViewById<android.widget.Button>(R.id.cancelButton)
        val completeButton = dialogView.findViewById<android.widget.Button>(R.id.completeButton)
        
        // Only show action buttons if appointment is not completed or canceled
        if (appointment.status == "COMPLETED" || appointment.status == "CANCELED") {
            rescheduleButton.visibility = View.GONE
            cancelButton.visibility = View.GONE
            completeButton.visibility = View.GONE
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
            
            // Set up complete button
            completeButton.setOnClickListener {
                dialog.dismiss()
                showCompleteConfirmation(appointment)
            }
        }

        // Close button
        dialogView.findViewById<android.widget.Button>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
                    Toast.makeText(this@AppointmentProfessionalActivity, "Appointment rescheduled successfully", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Refresh the list
                } else {
                    Toast.makeText(this@AppointmentProfessionalActivity, "Failed to reschedule appointment", Toast.LENGTH_SHORT).show()
                }
            }
    
            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                Toast.makeText(this@AppointmentProfessionalActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@AppointmentProfessionalActivity, "Appointment canceled successfully", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Refresh the list
                } else {
                    Toast.makeText(this@AppointmentProfessionalActivity, "Failed to cancel appointment", Toast.LENGTH_SHORT).show()
                }
            }
    
            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                Toast.makeText(this@AppointmentProfessionalActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun showCompleteConfirmation(appointment: AppointmentResponse) {
        AlertDialog.Builder(this)
            .setTitle("Complete Appointment")
            .setMessage("Mark this appointment as completed?")
            .setPositiveButton("Yes") { _, _ ->
                // Call the completeAppointment method
                completeAppointment(appointment.id)
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun completeAppointment(appointmentId: Long) {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        ApiClient.apiService.completeAppointment("Bearer $token", appointmentId).enqueue(object : Callback<AppointmentResponse> {
            override fun onResponse(call: Call<AppointmentResponse>, response: Response<AppointmentResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AppointmentProfessionalActivity, "Appointment marked as completed", Toast.LENGTH_SHORT).show()
                    loadAppointments() // Refresh the list
                } else {
                    Toast.makeText(this@AppointmentProfessionalActivity, "Failed to complete appointment", Toast.LENGTH_SHORT).show()
                }
            }
    
            override fun onFailure(call: Call<AppointmentResponse>, t: Throwable) {
                Toast.makeText(this@AppointmentProfessionalActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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

    // Add this method to implement the interface
    override fun onRateAppointmentClick(appointment: AppointmentResponse) {
        // Professionals don't rate appointments, so this can be empty
        // This method is only used by customers to rate professionals
        // But we need to implement it to satisfy the interface
    }
}