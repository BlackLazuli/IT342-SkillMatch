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
import retrofit2.Response
import android.util.Log

class SignUp : AppCompatActivity() {

    private lateinit var repository: SkillMatchRepository
    private lateinit var sessionManager: SessionManager
    
    // UI elements

    private lateinit var passwordInput: TextInputEditText
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var contactInput: TextInputEditText
    private lateinit var submitBtn: Button
    private lateinit var cancelBtn: Button
    
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        // Get selected role from intent
        selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "CUSTOMER"
        
        // Initialize repository and session manager
        repository = SkillMatchRepository(this)
        sessionManager = SessionManager(this)
        
        // Initialize UI elements - make sure IDs match your layout

        passwordInput = findViewById(R.id.passwordInput)
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        emailInput = findViewById(R.id.emailInput)
        contactInput = findViewById(R.id.contactsInput)
        submitBtn = findViewById(R.id.submitbtn)
        cancelBtn = findViewById(R.id.cancelbtn)
        
        // Set up click listeners
        submitBtn.setOnClickListener {
            // In the screenshot, username appears to be used for first name
            // and the form has separate first name and last name fields
            val password = passwordInput.text.toString()
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val email = emailInput.text.toString()
            val contact = contactInput.text.toString()
            
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && 
                password.isNotEmpty() && email.isNotEmpty()) {
                registerUser(firstName, lastName, email, password, contact, selectedRole!!)
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        cancelBtn.setOnClickListener {
            // Go back to login screen
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    // In your registerUser method
    private fun registerUser(firstName: String, lastName: String, email: String, 
                          password: String, phoneNumber: String, userType: String) {
        lifecycleScope.launch {
            try {
                // Convert PROFESSIONAL to SERVICE_PROVIDER to match backend
                val role = if (userType == "PROFESSIONAL") "SERVICE_PROVIDER" else userType
                
                // Add debug logging
                android.util.Log.d("SignUp", "Attempting to register: $firstName, $lastName, $email, [password], $phoneNumber, $role")
                
                val response = repository.signup(firstName, lastName, email, password, phoneNumber, role)
                
                if (response.isSuccessful) {
                    val signupResponse = response.body()
                    
                    if (signupResponse != null) {
                        // Save user data to session
                        sessionManager.saveAuthToken(signupResponse.token)
                        sessionManager.saveUserId(signupResponse.userId)
                        sessionManager.saveUserType(signupResponse.role) // Change to role
                        
                        Log.d("SignUp", "Registration successful: ${signupResponse.userId}")
                        Toast.makeText(this@SignUp, "Registration successful!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate based on role
                        if (signupResponse.role == "CUSTOMER") {
                            val intent = Intent(this@SignUp, CustomerDashboard::class.java)
                            intent.putExtra("USER_ID", signupResponse.userId)
                            startActivity(intent)
                            finish()
                        } else if (signupResponse.role == "PROFESSIONAL" || 
                                  signupResponse.role == "SERVICE_PROVIDER") {
                            val intent = Intent(this@SignUp, ProfessionalDashboard::class.java)
                            intent.putExtra("USER_ID", signupResponse.userId)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Log.e("SignUp", "Registration response body is null")
                        Toast.makeText(this@SignUp, "Registration failed: Empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.e("SignUp", "Registration failed: $errorCode - $errorBody")
                    
                    val errorMessage = when (errorCode) {
                        403 -> "Registration failed: Access forbidden. Check your server permissions."
                        409 -> "Email already exists. Please use a different email."
                        400 -> "Invalid registration data. Please check your inputs."
                        500 -> "Server error. Please try again later."
                        else -> "Registration failed: $errorCode - $errorBody"
                    }
                    
                    Toast.makeText(this@SignUp, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("SignUp", "Registration exception", e)
                Toast.makeText(this@SignUp, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
