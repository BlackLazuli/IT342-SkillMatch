package com.example.skillmatch.professional

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.skillmatch.Login
import com.example.skillmatch.R
import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.User
import com.example.skillmatch.utils.SessionManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfessionalSettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var backButton: ImageButton
    private lateinit var profileCard: CardView
    private lateinit var logoutCard: CardView
    private lateinit var homeButton: ImageButton
    private lateinit var messagesButton: ImageButton
    private lateinit var settingsNavButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professional_settings)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize views
        initializeViews()

        // Set click listeners
        setupClickListeners()

        // Load user profile data
        loadUserProfile()
    }

    private fun initializeViews() {
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        profileImage = findViewById(R.id.profileImage)
        backButton = findViewById(R.id.backButton)
        profileCard = findViewById(R.id.profileCard)
        logoutCard = findViewById(R.id.logoutCard)
        homeButton = findViewById(R.id.homeButton)
        messagesButton = findViewById(R.id.messagesButton)
        settingsNavButton = findViewById(R.id.settingsNavButton)
        profileButton = findViewById(R.id.profileButton)
    }

    private fun setupClickListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            finish()
        }

        // Profile card click listener
        profileCard.setOnClickListener {
            val intent = Intent(this, ProfessionalProfileActivity::class.java)
            startActivity(intent)
        }

        // Logout card click listener
        logoutCard.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Bottom navigation buttons
        homeButton.setOnClickListener {
            val intent = Intent(this, AppointmentProfessionalActivity::class.java)
            startActivity(intent)
            finish()
        }

        messagesButton.setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            startActivity(intent)
        }

        settingsNavButton.setOnClickListener {
            // Already on settings screen
            Toast.makeText(this, "Already on Settings screen", Toast.LENGTH_SHORT).show()
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfessionalProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Clear session and navigate to login screen
                sessionManager.clearSession()
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadUserProfile() {
        val userId = sessionManager.getUserId()

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getUserProfile(userId)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()

                    withContext(Dispatchers.Main) {
                        populateUserData(user!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("SettingsActivity", "Failed to load user data: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(applicationContext, "Failed to load user data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsActivity", "Exception loading user data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        // Set user name (combine first and last name)
        val fullName = "${user.firstName} ${user.lastName}"
        nameText.text = fullName

        // Set user email
        emailText.text = user.email

        // Load profile image if available
        user.profilePicture?.let { profilePic ->
            val backendBaseUrl = "http://3.107.23.86:8080"
            if (profilePic.startsWith("http")) {
                Glide.with(this)
                    .load(profilePic)
                    .placeholder(R.drawable.user)
                    .into(profileImage)
            } else if (profilePic.startsWith("/uploads")) {
                Glide.with(this)
                    .load(backendBaseUrl + profilePic)
                    .placeholder(R.drawable.user)
                    .into(profileImage)
            } else {
                try {
                    val imageBytes = Base64.decode(profilePic, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    profileImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("SettingsActivity", "Error loading profile image", e)
                }
            }
        }
    }
}