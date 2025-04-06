package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.repository.SkillMatchRepository
import com.example.skillmatch.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {

    private lateinit var repository: SkillMatchRepository
    private lateinit var sessionManager: SessionManager
    
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var submitBtn: Button
    private lateinit var cancelBtn: Button
    
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        // Get selected role from intent
        selectedRole = intent.getStringExtra("SELECTED_ROLE")
        
        // Initialize repository and session manager
        repository = SkillMatchRepository(this)
        sessionManager = SessionManager(this)
        
        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        emailInput = findViewById(R.id.emailInput)
        submitBtn = findViewById(R.id.submitbtn)
        cancelBtn = findViewById(R.id.cancelbtn)
        
        // Set up click listeners
        submitBtn.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val email = emailInput.text.toString()
            
            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty() && selectedRole != null) {
                registerUser(username, email, password, selectedRole!!)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        cancelBtn.setOnClickListener {
            // Go back to login screen
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun registerUser(name: String, email: String, password: String, userType: String) {
        lifecycleScope.launch {
            try {
                val response = repository.signup(name, email, password, userType)
                
                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    
                    if (signupResponse != null) {
                        // Save user data to session
                        sessionManager.saveAuthToken(signupResponse.token)
                        sessionManager.saveUserId(signupResponse.userId)
                        sessionManager.saveUserType(signupResponse.userType)
                        
                        // Navigate based on user type
                        if (signupResponse.userType == "CUSTOMER") {
                            val intent = Intent(this@SignUp, CustomerDashboard::class.java)
                            intent.putExtra("USER_ID", signupResponse.userId)
                            startActivity(intent)
                            finish()
                        } else if (signupResponse.userType == "PROFESSIONAL") {
                            val intent = Intent(this@SignUp, ProfessionalDashboard::class.java)
                            intent.putExtra("USER_ID", signupResponse.userId)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this@SignUp, "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUp, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
