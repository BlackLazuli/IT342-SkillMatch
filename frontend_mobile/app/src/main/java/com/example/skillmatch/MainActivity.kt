package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
            val intent = Intent(this, ChooseRole::class.java)
            startActivity(intent)
        }
    }
    
    private fun navigateBasedOnUserType() {
        val userType = sessionManager.getUserType()
        val userId = sessionManager.getUserId()
        
        if (userType == "CUSTOMER") {
            val intent = Intent(this, CustomerDashboard::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        } else if (userType == "PROFESSIONAL") {
            val intent = Intent(this, ProfessionalDashboard::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }
    }
    
    // Inside your MainActivity or another appropriate place
    private fun testBackendConnection() {
        lifecycleScope.launch {
            try {
                val response = repository.getAllProfessionals()
                if (response.isSuccessful) {
                    Log.d("API", "Connection successful: ${response.body()}")
                    Toast.makeText(this@MainActivity, "Connected to backend!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Connection failed: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Failed to connect: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "Connection error", e)
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}