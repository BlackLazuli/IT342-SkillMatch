package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ChooseRole : AppCompatActivity() {

    private lateinit var customerRoleCardPopup: CardView
    private lateinit var professionalRoleCardPopup: CardView
    private lateinit var instructionText: TextView
    private lateinit var selectedRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooserole)

        // Initialize views
        customerRoleCardPopup = findViewById(R.id.customerRoleCardPopup)
        professionalRoleCardPopup = findViewById(R.id.professionalRoleCardPopup)
        instructionText = findViewById(R.id.instructionText)

        // Set click listeners for role selection
        customerRoleCardPopup.setOnClickListener {
            selectedRole = "customer"
            Toast.makeText(this, "Customer role selected", Toast.LENGTH_SHORT).show()
            navigateToSignUp()
        }

        professionalRoleCardPopup.setOnClickListener {
            selectedRole = "professional"
            Toast.makeText(this, "Professional role selected", Toast.LENGTH_SHORT).show()
            navigateToSignUp()
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUp::class.java)
        intent.putExtra("SELECTED_ROLE", selectedRole)
        startActivity(intent)
        finish() // Close this activity so user can't go back to it
    }

}