package com.example.skillmatch.professional

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var availabilityTimeInput: EditText
    
    // Day checkboxes
    private lateinit var checkboxMonday: CheckBox
    private lateinit var checkboxTuesday: CheckBox
    private lateinit var checkboxWednesday: CheckBox
    private lateinit var checkboxThursday: CheckBox
    private lateinit var checkboxFriday: CheckBox
    private lateinit var checkboxSaturday: CheckBox
    private lateinit var checkboxSunday: CheckBox

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
        availabilityTimeInput = findViewById(R.id.availabilityTimeInput)
        
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
    }

    private fun setupListeners() {
        // Back button
        backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Save button
        saveButton.setOnClickListener {
            savePortfolio()
        }
        
        // Add service button
        addServiceButton.setOnClickListener {
            showServiceDialog(null)
        }
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
                            
                            // Set availability days and time
                            val firstService = portfolio.servicesOffered?.firstOrNull()
                            firstService?.daysOfTheWeek?.let { days ->
                                setAvailableDays(days)
                            }
                            
                            firstService?.time?.let { time ->
                                availabilityTimeInput.setText(time)
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

    private fun savePortfolio() {
        // Get selected days
        val selectedDays = getSelectedDays()
        
        // Make a copy of the services list to avoid concurrent modification
        val servicesCopy = ArrayList(services)
        
        // Update services with selected days and time
        val availabilityTime = availabilityTimeInput.text.toString()
        val updatedServices = servicesCopy.map { service ->
            Service(
                id = service.id, // Only set to null if it's a new service
                name = service.name,
                description = service.description,
                pricing = service.pricing,
                time = availabilityTime,
                daysOfTheWeek = selectedDays
            )
        }
        val portfolioToSave = Portfolio(
            id = portfolioId,
            workExperience = workExperienceInput.text.toString(),
            servicesOffered = updatedServices,
            clientTestimonials = null
        )
        
        // Add more detailed logging
        Log.d("EditPortfolio", "Portfolio to save: ${portfolioToSave}")
        
        val userId = sessionManager.getUserId()?.toString()
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Add detailed logging
                    Log.d("EditPortfolio", "Saving portfolio for user ID: $userId")
                    Log.d("EditPortfolio", "Portfolio data: ${portfolioToSave}")
                    
                    // Add authentication token
                    val token = "Bearer ${sessionManager.getToken()}"
                    Log.d("EditPortfolio", "portfolioId to update: $portfolioId") // This should print 1, not 2
                    val response = if (portfolioId != null) {
                        Log.d("EditPortfolio", "Updating existing portfolio with PUT")
                        ApiClient.apiService.updatePortfolio(token, userId, portfolioToSave)
                    } else {
                        Log.d("EditPortfolio", "Creating new portfolio with POST")
                        ApiClient.apiService.createOrUpdatePortfolio(token, userId, portfolioToSave)
                    }
                    
                    // Log the raw request body for debugging
                    Log.d("EditPortfolio", "Request body: ${portfolioToSave}")
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditPortfolioActivity, "Portfolio saved successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@EditPortfolioActivity, PortfolioActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("EditPortfolio", "Error code: ${response.code()}, Error body: $errorBody")
                            Toast.makeText(this@EditPortfolioActivity, "Error saving portfolio: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("EditPortfolio", "Exception: ${e.message}", e)
                        Toast.makeText(this@EditPortfolioActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
        }
    }
}