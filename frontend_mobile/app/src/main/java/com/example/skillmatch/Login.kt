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
    
    private lateinit var usernameInput: TextInputEditText
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
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginbtn)
        signupText = findViewById(R.id.signUpText)
        
        // Set up click listeners
        loginButton.setOnClickListener {
            val email = usernameInput.text.toString()
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
                val response = repository.login(email, password)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    
                    if (loginResponse != null) {
                        // Save user data to session
                        sessionManager.saveAuthToken(loginResponse.token)
                        sessionManager.saveUserId(loginResponse.userId)
                        sessionManager.saveUserType(loginResponse.userType)
                        
                        // Navigate based on user type
                        if (loginResponse.userType == "CUSTOMER") {
                            val intent = Intent(this@Login, CustomerDashboard::class.java)
                            intent.putExtra("USER_ID", loginResponse.userId)
                            startActivity(intent)
                            finish()
                        } else if (loginResponse.userType == "PROFESSIONAL") {
                            val intent = Intent(this@Login, ProfessionalDashboard::class.java)
                            intent.putExtra("USER_ID", loginResponse.userId)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Login, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
