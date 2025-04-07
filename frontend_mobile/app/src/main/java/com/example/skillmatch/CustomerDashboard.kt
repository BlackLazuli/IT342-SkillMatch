package com.example.skillmatch

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class CustomerDashboard : AppCompatActivity() {

    // Declare UI elements
    private lateinit var profileIcon: CardView
    private lateinit var logoImage: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var locationText: TextView

    // Bottom navigation elements
    private lateinit var homeNav: LinearLayout
    private lateinit var calendarNav: LinearLayout
    private lateinit var settingsNav: LinearLayout
    private lateinit var profileNav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customerdashboard)

        // Initialize UI elements
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        // Initialize main UI elements
        profileIcon = findViewById(R.id.profileCircle)
        logoImage = findViewById(R.id.logoImage)
        searchEditText = findViewById(R.id.searchEditText)
        locationText = findViewById(R.id.locationText)

        // Initialize bottom navigation
        homeNav = findViewById(R.id.homeNav)
        calendarNav = findViewById(R.id.calendarNav)
        settingsNav = findViewById(R.id.settingsNav)
        profileNav = findViewById(R.id.profileNav)
    }

    private fun setupClickListeners() {
        // Set up click listener for profile icon
        profileIcon.setOnClickListener {
            // Navigate to profile page or show profile options
            // Example: startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set up search functionality
        searchEditText.setOnClickListener {
            // Implement search functionality
            // Could expand search bar or show suggestions
        }

        // Set up bottom navigation click listeners
        homeNav.setOnClickListener {
            // Already on home page, could refresh
        }

        calendarNav.setOnClickListener {
            // Navigate to calendar/appointments page
            // Example: startActivity(Intent(this, CalendarActivity::class.java))
        }

        settingsNav.setOnClickListener {
            // Navigate to settings page
            // Example: startActivity(Intent(this, SettingsActivity::class.java))
        }

        profileNav.setOnClickListener {
            // Navigate to profile page
            // Example: startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // You can add more methods as needed for specific functionality
    // For example, a method to populate the professionals list from a database
    private fun loadProfessionals() {
        // This would fetch data from your backend and populate the list
        // For now, the layout has hardcoded professionals
    }
}