package com.example.skillmatch

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    private lateinit var themeSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load saved theme preference
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val rememberMe = findViewById<CheckBox>(R.id.rememberMe)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val signUp = findViewById<TextView>(R.id.signUp)
        themeSwitch = findViewById(R.id.themeSwitch)

        // Set switch state based on saved preference
        themeSwitch.isChecked = isDarkMode

        // Handle theme switch toggle
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("dark_mode", true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("dark_mode", false)
            }
            editor.apply()

            // Restart activity to apply theme change
            recreate()
        }

        // Sign-In Button Click Listener
        signInButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Implement authentication logic
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()
            }
        }

        // Forgot Password Click Listener
        forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
        }

        // Sign-Up Click Listener
        signUp.setOnClickListener {
            Toast.makeText(this, "Sign-Up Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
