package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.skillmatch.repository.SkillMatchRepository
import com.example.skillmatch.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var repository: SkillMatchRepository
    private lateinit var sessionManager: SessionManager
    
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize repository and session manager
        repository = SkillMatchRepository(this)
        sessionManager = SessionManager(this)
        
        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginbtn)
        signupText = findViewById(R.id.signUpText)
        
        // Set up click listeners
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        
        signupText.setOnClickListener {
            val intent = Intent(this, ChooseRole::class.java)
            startActivity(intent)
        }
    }
    
    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Add debug logging
                android.util.Log.d("Login", "Attempting login with email: $email")
                
                val response = repository.login(email, password)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    
                    android.util.Log.d("Login", "Login response: $loginResponse")
                    
                    if (loginResponse != null) {
                        // Save user data to session
                        sessionManager.saveAuthToken(loginResponse.token)
                        sessionManager.saveUserId(loginResponse.userId)
                        sessionManager.saveUserType(loginResponse.role)
                        
                        android.util.Log.d("Login", "User role: ${loginResponse.role}")
                        
                        // Navigate based on user type
                        if (loginResponse.role == "CUSTOMER") {
                            val intent = Intent(this@Login, CustomerDashboard::class.java)
                            intent.putExtra("USER_ID", loginResponse.userId)
                            startActivity(intent)
                            finish()
                        } else if (loginResponse.role == "SERVICE_PROVIDER") {
                            val intent = Intent(this@Login, ProfessionalDashboard::class.java)
                            intent.putExtra("USER_ID", loginResponse.userId)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        android.util.Log.e("Login", "Login response body is null")
                        Toast.makeText(this@Login, "Login failed: Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.util.Log.e("Login", "Login failed: ${response.code()} - ${response.message()}")
                    
                    // Display a more user-friendly error message
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid email or password"
                        else -> "Login failed: ${response.message()}"
                    }
                    
                    //
                    
                    Toast.makeText(this@Login, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Login error", e)
                Toast.makeText(this@Login, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
