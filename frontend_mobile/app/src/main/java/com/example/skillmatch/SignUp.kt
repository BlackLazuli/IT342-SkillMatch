package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val firstNameInput: TextInputEditText = findViewById(R.id.firstNameInput)
        val middleNameInput: TextInputEditText = findViewById(R.id.middleNameInput)
        val lastNameInput: TextInputEditText = findViewById(R.id.lastNameInput)
        val dobInput: TextInputEditText = findViewById(R.id.dobInput)
        val emailInput: TextInputEditText = findViewById(R.id.emailInput)
        val contactsInput: TextInputEditText = findViewById(R.id.contactsInput)
        val usernameInput: TextInputEditText = findViewById(R.id.usernameInput)
        val passwordInput: TextInputEditText = findViewById(R.id.passwordInput)

        val submitButton: Button = findViewById(R.id.submitbtn)
        val cancelButton: Button = findViewById(R.id.cancelbtn)
        val googleButton: ImageButton = findViewById(R.id.googlebtn)

        submitButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val middleName = middleNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val dob = dobInput.text.toString()
            val email = emailInput.text.toString()
            val contacts = contactsInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            // Validate the input
            if (firstName.isNotEmpty() && middleName.isNotEmpty() && lastName.isNotEmpty() &&
                dob.isNotEmpty() && email.isNotEmpty() && contacts.isNotEmpty() &&
                username.isNotEmpty() && password.isNotEmpty()) {

                // Proceed with the sign-up logic (e.g., save the data to a database or send it to a backend)
                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                finish()
                 // Redirect to login screen after sign up
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            // Handle cancel action (e.g., go back to login screen)
            // Close the current activity
            startActivity(Intent(this, Login::class.java))
        }

        googleButton.setOnClickListener {
            Toast.makeText(this, "Google Login Clicked", Toast.LENGTH_SHORT).show()
            // Implement Google Sign-In functionality
        }
    }
}
