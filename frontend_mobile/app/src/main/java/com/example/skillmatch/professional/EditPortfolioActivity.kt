package com.example.skillmatch.professional

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillmatch.R
import com.example.skillmatch.adapter.ServiceAdapter
import com.example.skillmatch.dialogs.ServiceDialogFragment
import com.example.skillmatch.models.Service
import java.text.SimpleDateFormat
import java.util.*

class EditPortfolioActivity : AppCompatActivity() {

    private val services = mutableListOf<Service>()
    private var selectedDates = mutableSetOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private lateinit var servicesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var addServiceButton: Button
    private lateinit var workExperienceInput: EditText
    private lateinit var availabilityTimeInput: EditText
    private lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_portfolio)

        // Initialize views
        servicesRecyclerView = findViewById(R.id.servicesRecyclerView)
        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        addServiceButton = findViewById(R.id.addServiceButton)
        workExperienceInput = findViewById(R.id.workExperienceInput)
        availabilityTimeInput = findViewById(R.id.availabilityTimeInput)
        calendarView = findViewById(R.id.calendarView)

        setupUI()
        setupListeners()
        loadPortfolioData()
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

        // Setup Calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val dateString = dateFormat.format(calendar.time)
            
            if (selectedDates.contains(dateString)) {
                selectedDates.remove(dateString)
                // Unmark date visually if needed
            } else {
                selectedDates.add(dateString)
                // Mark date visually if needed
            }
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

    private fun loadPortfolioData() {
        // This would typically fetch data from your API
        // For now, we'll use dummy data
        workExperienceInput.setText("5 years of experience in web development")
        
        // Add some sample services
        services.add(Service(1, "Web Development", "Full-stack web development services", 50.0))
        services.add(Service(2, "Mobile App Development", "Native Android and iOS apps", 60.0))
        servicesRecyclerView.adapter?.notifyDataSetChanged()
        
        // Set some sample availability dates
        // Implementation depends on how you want to visualize selected dates
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
        val workExperience = workExperienceInput.text.toString()
        val availabilityTime = availabilityTimeInput.text.toString()
        
        // Here you would typically send this data to your API
        // For now, we'll just show a success message
        
        Toast.makeText(this, "Portfolio saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}