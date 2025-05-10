package com.example.skillmatch.customer

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

class CustomerSettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var backButton: ImageButton
    private lateinit var profileCard: CardView
    private lateinit var logoutCard: CardView
    
    // Bottom navigation
    private lateinit var homeButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var settingsNavButton: ImageButton
    private lateinit var profileButton: ImageButton
    
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_settings)

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

        
        // Initialize bottom navigation
        homeButton = findViewById(R.id.homeButton)
        calendarButton = findViewById(R.id.calendarButton)
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
            val intent = Intent(this, EditCustomerProfile::class.java)
            startActivity(intent)
        }

        // Logout card click listener
        logoutCard.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Bottom navigation buttons
        homeButton.setOnClickListener {
            val intent = Intent(this, CustomerDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        calendarButton.setOnClickListener {
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }

        settingsNavButton.setOnClickListener {
            // Already on settings screen
            Toast.makeText(this, "Already on Settings screen", Toast.LENGTH_SHORT).show()
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, CustomerProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
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
                    currentUser = response.body()

                    withContext(Dispatchers.Main) {
                        populateUserData(currentUser!!)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("CustomerSettingsActivity", "Failed to load user data: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(applicationContext, "Failed to load user data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomerSettingsActivity", "Exception loading user data", e)
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
                    Log.e("CustomerSettingsActivity", "Error loading profile image", e)
                }
            }
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("About SkillMatch")
        builder.setMessage("SkillMatch v1.0\n\nA platform connecting customers with skilled professionals.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun performLogout() {
        // Clear session data
        sessionManager.clearSession()
        
        // Navigate to login screen (replace with your login activity)
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Reload user profile when returning to this screen (in case it was updated)
        loadUserProfile()
    }
}