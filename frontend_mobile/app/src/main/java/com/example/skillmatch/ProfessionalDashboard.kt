package com.example.skillmatch

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ProfessionalDashboard : AppCompatActivity() {

    // Declare UI elements
    private lateinit var logoImage: ImageView
    private lateinit var profileCircle: CardView
    private lateinit var featuredCard: CardView
    private lateinit var appointmentsTitle: TextView

    // Bottom navigation elements
    private lateinit var homeNav: LinearLayout
    private lateinit var briefcaseNav: LinearLayout
    private lateinit var settingsNav: LinearLayout
    private lateinit var profileNav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professionaldashboard)

        // Initialize UI elements
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Load appointments data
        loadAppointments()
    }

    private fun initializeViews() {
        // Initialize main UI elements
        logoImage = findViewById(R.id.logoImage)
        profileCircle = findViewById(R.id.profileCircle)
        featuredCard = findViewById(R.id.featuredCard)
        appointmentsTitle = findViewById(R.id.appointmentsTitle)

        // Initialize bottom navigation
        homeNav = findViewById(R.id.homeNav)
        briefcaseNav = findViewById(R.id.briefcaseNav)
        settingsNav = findViewById(R.id.settingsNav)
        profileNav = findViewById(R.id.profileNav)
    }

    private fun setupClickListeners() {
        // Set up click listener for profile circle
        profileCircle.setOnClickListener {
            // Navigate to profile page or show profile options
            // Example: startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Set up click listener for featured card
        featuredCard.setOnClickListener {
            // Could navigate to edit profile or show more details
            // Example: startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Set up bottom navigation click listeners
        homeNav.setOnClickListener {
            // Already on home page, could refresh
        }

        briefcaseNav.setOnClickListener {
            // Navigate to jobs/projects page
            // Example: startActivity(Intent(this, JobsActivity::class.java))
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

    // Method to load appointments from database or API
    private fun loadAppointments() {
        // This would fetch appointment data from your backend
        // For now, the layout has hardcoded appointments

        // Example of how you might update the UI with dynamic data:
        // val appointmentsList = findViewById<LinearLayout>(R.id.appointmentsList)
        // for (appointment in fetchedAppointments) {
        //     val appointmentView = createAppointmentView(appointment)
        //     appointmentsList.addView(appointmentView)
        // }
    }

    // You could add a method to create appointment views dynamically
    // private fun createAppointmentView(appointment: Appointment): View {
    //     // Inflate a layout for a single appointment and populate it
    //     // Return the populated view
    // }
}