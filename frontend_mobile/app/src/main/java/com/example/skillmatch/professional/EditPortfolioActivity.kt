package com.example.skillmatch.professional

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.adapter.ServiceAdapter
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.dialogs.ServiceDialogFragment
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Service
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class EditPortfolioActivity : AppCompatActivity() {

    private val services = mutableListOf<Service>()
    private val availableDays = mutableListOf<String>()
    private lateinit var sessionManager: SessionManager
    private var portfolioId: Long? = null
    
    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var addServiceButton: Button
    private lateinit var workExperienceInput: EditText
    private lateinit var startTimeInput: TextView
    private lateinit var endTimeInput: TextView
    
    // Day checkboxes
    private lateinit var checkboxMonday: CheckBox
    private lateinit var checkboxTuesday: CheckBox
    private lateinit var checkboxWednesday: CheckBox
    private lateinit var checkboxThursday: CheckBox
    private lateinit var checkboxFriday: CheckBox
    private lateinit var checkboxSaturday: CheckBox
    private lateinit var checkboxSunday: CheckBox
    
    // Time formatter
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var selectedStartTime: LocalTime = LocalTime.of(9, 0) // Default to 9:00 AM
    private var selectedEndTime: LocalTime = LocalTime.of(17, 0) // Default to 5:00 PM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_portfolio)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Initialize views
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        addServiceButton = findViewById(R.id.addServiceButton)
        workExperienceInput = findViewById(R.id.workExperienceInput)
        startTimeInput = findViewById(R.id.startTimeInput)
        endTimeInput = findViewById(R.id.endTimeInput)
        
        // Initialize day checkboxes
        checkboxMonday = findViewById(R.id.checkboxMonday)
        checkboxTuesday = findViewById(R.id.checkboxTuesday)
        checkboxWednesday = findViewById(R.id.checkboxWednesday)
        checkboxThursday = findViewById(R.id.checkboxThursday)
        checkboxFriday = findViewById(R.id.checkboxFriday)
        checkboxSaturday = findViewById(R.id.checkboxSaturday)
        checkboxSunday = findViewById(R.id.checkboxSunday)

        setupUI()
        setupListeners()
        fetchPortfolioData()
    }

    private fun setupUI() {
        // Setup RecyclerView
        val serviceAdapter = ServiceAdapter(
            services,
            onEditClick = { service -> showServiceDialog(service) },
            onDeleteClick = { service -> deleteService(service) }
        )
        servicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EditPortfolioActivity)
            adapter = serviceAdapter
        }
        
        // Set initial time displays
        updateTimeDisplays()
    }

    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            // Replace onBackPressed() with finish()
            finish()
        }
        
        // Save button
        saveButton.setOnClickListener {
            savePortfolio()
        }
        
        // Add service button
        addServiceButton.setOnClickListener {
            showServiceDialog(null)
        }
        
        // Time picker dialogs for availability times
        startTimeInput.setOnClickListener {
            showStartTimePickerDialog()
        }
        
        endTimeInput.setOnClickListener {
            showEndTimePickerDialog()
        }
    }
    
    private fun showStartTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedStartTime = LocalTime.of(hourOfDay, minute)
                updateTimeDisplays()
            },
            selectedStartTime.hour,
            selectedStartTime.minute,
            true // 24-hour format
        )
        timePickerDialog.show()
    }
    
    private fun showEndTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedEndTime = LocalTime.of(hourOfDay, minute)
                updateTimeDisplays()
            },
            selectedEndTime.hour,
            selectedEndTime.minute,
            true // 24-hour format
        )
        timePickerDialog.show()
    }
    
    private fun updateTimeDisplays() {
        // Format times in 12-hour format with AM/PM
        startTimeInput.text = selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        endTimeInput.text = selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }

    private fun fetchPortfolioData() {
        val userId = sessionManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Add authentication token
                    val token = "Bearer ${sessionManager.getToken()}"
                    val response = ApiClient.apiService.getPortfolio(token, userId)
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val portfolio = response.body()!!
                            portfolioId = portfolio.id
                            
                            // Set work experience
                            workExperienceInput.setText(portfolio.workExperience ?: "")
                            
                            // Clear and add services
                            services.clear()
                            portfolio.servicesOffered?.let { serviceList ->
                                services.addAll(serviceList)
                                servicesRecyclerView.adapter?.notifyDataSetChanged()
                            }
                            
                            // Set availability days and time from portfolio
                            portfolio.daysAvailable.let { days ->
                                setAvailableDays(days)
                            }
                            
                            // Parse start and end times from portfolio
                            try {
                                // Check for startTime and endTime first (new format)
                                if (portfolio.startTime != null) {
                                    selectedStartTime = LocalTime.parse(portfolio.startTime.trim(), timeFormatter)
                                }
                                
                                if (portfolio.endTime != null) {
                                    selectedEndTime = LocalTime.parse(portfolio.endTime.trim(), timeFormatter)
                                }
                                // For backward compatibility, check time field
                                else if (portfolio.time != null) {
                                    // Check if time contains a range separator
                                    if (portfolio.time.contains("-")) {
                                        val times = portfolio.time.split("-")
                                        if (times.size == 2) {
                                            selectedStartTime = LocalTime.parse(times[0].trim(), timeFormatter)
                                            selectedEndTime = LocalTime.parse(times[1].trim(), timeFormatter)
                                        }
                                    } else {
                                        // For backward compatibility with old format
                                        selectedStartTime = LocalTime.parse(portfolio.time.trim(), timeFormatter)
                                    }
                                }
                                updateTimeDisplays()
                            } catch (e: Exception) {
                                Log.e("EditPortfolio", "Error parsing time", e)
                                // Keep default times if parsing fails
                            }
                        } else {
                            // No portfolio found, create a new one
                            Log.d("EditPortfolio", "No portfolio found, will create a new one on save")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditPortfolioActivity,
                            "Error loading portfolio: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("EditPortfolio", "Error loading portfolio", e)
                    }
                }
            }
        }
    }
    
    private fun setAvailableDays(days: List<String>) {
        // Reset all checkboxes
        checkboxMonday.isChecked = false
        checkboxTuesday.isChecked = false
        checkboxWednesday.isChecked = false
        checkboxThursday.isChecked = false
        checkboxFriday.isChecked = false
        checkboxSaturday.isChecked = false
        checkboxSunday.isChecked = false
        
        // Check the days that are available
        for (day in days) {
            when (day) {
                "Monday" -> checkboxMonday.isChecked = true
                "Tuesday" -> checkboxTuesday.isChecked = true
                "Wednesday" -> checkboxWednesday.isChecked = true
                "Thursday" -> checkboxThursday.isChecked = true
                "Friday" -> checkboxFriday.isChecked = true
                "Saturday" -> checkboxSaturday.isChecked = true
                "Sunday" -> checkboxSunday.isChecked = true
            }
        }
    }
    
    private fun getSelectedDays(): List<String> {
        val selectedDays = mutableListOf<String>()
        
        if (checkboxMonday.isChecked) selectedDays.add("Monday")
        if (checkboxTuesday.isChecked) selectedDays.add("Tuesday")
        if (checkboxWednesday.isChecked) selectedDays.add("Wednesday")
        if (checkboxThursday.isChecked) selectedDays.add("Thursday")
        if (checkboxFriday.isChecked) selectedDays.add("Friday")
        if (checkboxSaturday.isChecked) selectedDays.add("Saturday")
        if (checkboxSunday.isChecked) selectedDays.add("Sunday")
        
        return selectedDays
    }

    private fun showServiceDialog(service: Service?) {
        val dialog = ServiceDialogFragment.newInstance(service)
        dialog.setServiceDialogListener(object : ServiceDialogFragment.ServiceDialogListener {
            override fun onServiceSaved(updatedService: Service) {
                if (service == null) {
                    // Add new service
                    services.add(updatedService)
                } else {
                    // Update existing service
                    val index = services.indexOfFirst { it.id == updatedService.id }
                    if (index != -1) {
                        services[index] = updatedService
                    }
                }
                // Notify adapter of changes
                servicesRecyclerView.adapter?.notifyDataSetChanged()
            }
        })
        dialog.show(supportFragmentManager, "ServiceDialog")
    }

    private fun deleteService(service: Service) {
        services.removeIf { it.id == service.id }
        servicesRecyclerView.adapter?.notifyDataSetChanged()
    }

    // When saving the portfolio, ensure the time is included
    private fun savePortfolio() {
        // Get work experience
        val workExperience = workExperienceInput.text.toString()
        
        // Get selected days
        val selectedDays = getSelectedDays()
        
        // Format start and end times
        val startTimeStr = selectedStartTime.format(timeFormatter)
        val endTimeStr = selectedEndTime.format(timeFormatter)
        
        // For backward compatibility, also set the time field
        val availabilityTime = "$startTimeStr - $endTimeStr"
        
        // Create portfolio object
        val portfolio = Portfolio(
            id = portfolioId,
            workExperience = workExperience,
            servicesOffered = services,
            daysAvailable = selectedDays,
            startTime = startTimeStr,
            endTime = endTimeStr,
            time = availabilityTime  // For backward compatibility
        )
        
        // Save the portfolio
        val userId = sessionManager.getUserId()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Add authentication token
                    val token = "Bearer ${sessionManager.getToken()}"
                    val response = if (portfolioId == null) {
                        // Create new portfolio
                        ApiClient.apiService.createOrUpdatePortfolio(token, userId, portfolio)
                    } else {
                        // Update existing portfolio
                        ApiClient.apiService.updatePortfolio(token, userId, portfolio)
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@EditPortfolioActivity,
                                "Portfolio saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@EditPortfolioActivity,
                                "Error saving portfolio",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditPortfolioActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}