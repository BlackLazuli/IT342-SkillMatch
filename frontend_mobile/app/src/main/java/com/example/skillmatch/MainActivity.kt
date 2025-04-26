package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.customer.EditCustomerProfile
import com.example.skillmatch.repository.SkillMatchRepository
import com.example.skillmatch.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: SkillMatchRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize repository and session manager
        repository = SkillMatchRepository(this)
        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.getAuthToken() != null) {
            navigateBasedOnUserType()
            return
        }

        val getStartedButton: Button = findViewById(R.id.GetStarted)
        getStartedButton.setOnClickListener {
            // Navigate to role selection screen
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    // Add the missing navigateBasedOnUserType method
    // Updated navigateBasedOnUserType method without CustomerDashboard reference
    private fun navigateBasedOnUserType() {
        try {
            val userRole = sessionManager.getUserRole()
            // Since CustomerDashboard is removed, redirect all users to Login for now
            Toast.makeText(this, "Welcome back! Please log in again.", Toast.LENGTH_SHORT).show()
            sessionManager.clearSession() // Clear session to ensure fresh login
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Close MainActivity so user can't go back
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation error", e)
            Toast.makeText(this, "Error navigating: ${e.message}", Toast.LENGTH_SHORT).show()
            // Reset session and stay on main activity if there's an error
            sessionManager.clearSession()
        }
    }


}