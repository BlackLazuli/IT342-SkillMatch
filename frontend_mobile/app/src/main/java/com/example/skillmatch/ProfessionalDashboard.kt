package com.example.skillmatch

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.models.Appointment
import com.example.skillmatch.models.User
import com.example.skillmatch.repository.SkillMatchRepository
import kotlinx.coroutines.launch

class ProfessionalDashboard : AppCompatActivity() {

    // Declare UI elements
    private lateinit var logoImage: ImageView
    private lateinit var profileCircle: CardView
    private lateinit var featuredCard: CardView
    private lateinit var appointmentsTitle: TextView
    private lateinit var professionalNameTextView: TextView
    private lateinit var professionalRoleTextView: TextView

    // Bottom navigation elements
    private lateinit var homeNav: LinearLayout
    private lateinit var briefcaseNav: LinearLayout
    private lateinit var settingsNav: LinearLayout
    private lateinit var profileNav: LinearLayout

    // Repository
    private val repository = SkillMatchRepository(this)

    // User data
    private var userId: String? = null
    private var userProfile: User? = null
    private var appointments: List<Appointment> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professionaldashboard)

        // Get user ID from intent or shared preferences
        userId = intent.getStringExtra("USER_ID") ?: getUserIdFromPreferences()

        // Initialize UI elements
        initializeViews()

        // Set up click listeners
        setupClickListeners()

        // Load user data and appointments
        loadUserData()
        loadAppointments()
    }

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("SkillMatchPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    private fun initializeViews() {
        // Initialize main UI elements
        logoImage = findViewById(R.id.logoImage)
        profileCircle = findViewById(R.id.profileCircle)
        featuredCard = findViewById(R.id.featuredCard)
        appointmentsTitle = findViewById(R.id.appointmentsTitle)

        // Find professional name and role TextViews within the featuredCard
        val featuredCardLayout = featuredCard.getChildAt(0) as LinearLayout
        val profileInfoLayout = featuredCardLayout.getChildAt(1) as LinearLayout
        professionalNameTextView = profileInfoLayout.getChildAt(0) as TextView
        professionalRoleTextView = profileInfoLayout.getChildAt(1) as TextView

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
            loadUserData()
            loadAppointments()
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

    private fun loadUserData() {
        userId?.let { id ->
            lifecycleScope.launch {
                try {
                    val response = repository.getUserProfile(id)
                    if (response.isSuccessful) {
                        userProfile = response.body()
                        updateUI()
                    } else {
                        Toast.makeText(this@ProfessionalDashboard,
                            "Failed to load profile: ${response.message()}",
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfessionalDashboard,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI() {
        userProfile?.let { user ->
            // Update profile circle text with first letter of name
            val profileCircleLayout = profileCircle.getChildAt(0) as? TextView
            profileCircleLayout?.text = user.name.firstOrNull()?.toString() ?: "U"

            // Update featured card with user info
            professionalNameTextView.text = user.name
            professionalRoleTextView.text = "Professional" // You might want to get this from the user profile
        }
    }

    private fun loadAppointments() {
        userId?.let { id ->
            lifecycleScope.launch {
                try {
                    val response = repository.getProfessionalAppointments(id)
                    if (response.isSuccessful) {
                        appointments = response.body() ?: emptyList()
                        updateAppointmentsUI()
                    } else {
                        Toast.makeText(this@ProfessionalDashboard,
                            "Failed to load appointments: ${response.message()}",
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProfessionalDashboard,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateAppointmentsUI() {
        // This would dynamically create appointment cards based on the data
        // For now, we're using the hardcoded appointments in the layout

        // Example of how you might implement this:
        // val appointmentsList = findViewById<LinearLayout>(R.id.appointmentsList)
        // appointmentsList.removeAllViews()
        //
        // for (appointment in appointments) {
        //     val appointmentView = createAppointmentView(appointment)
        //     appointmentsList.addView(appointmentView)
        // }
    }

    // Helper method to create appointment views dynamically
    // private fun createAppointmentView(appointment: Appointment): View {
    //     // Inflate a layout for a single appointment and populate it
    //     val inflater = layoutInflater
    //     val appointmentView = inflater.inflate(R.layout.item_appointment, null)
    //
    //     // Find views within the appointment layout
    //     val customerInitial = appointmentView.findViewById<TextView>(R.id.customerInitial)
    //     val customerName = appointmentView.findViewById<TextView>(R.id.customerName)
    //     val appointmentDay = appointmentView.findViewById<TextView>(R.id.appointmentDay)
    //     val appointmentTime = appointmentView.findViewById<TextView>(R.id.appointmentTime)
    //
    //     // Set data to views
    //     customerInitial.text = appointment.customerName.firstOrNull()?.toString() ?: "C"
    //     customerName.text = appointment.customerName
    //     appointmentDay.text = appointment.date
    //     appointmentTime.text = appointment.time
    //
    //     return appointmentView
    // }
}