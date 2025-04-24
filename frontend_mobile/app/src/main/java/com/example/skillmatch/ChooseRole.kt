package com.example.skillmatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ChooseRole : AppCompatActivity() {

    private lateinit var customerRoleCard: CardView
    private lateinit var professionalRoleCard: CardView
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooserole)

        // Initialize views - make sure these IDs match exactly what's in your layout
        customerRoleCard = findViewById(R.id.customerRoleCardPopup)
        professionalRoleCard = findViewById(R.id.professionalRoleCardPopup)

        // Set click listeners with explicit View parameter
        customerRoleCard.setOnClickListener(View.OnClickListener {
            selectedRole = "CUSTOMER"
            proceedToSignUp()
        })

        professionalRoleCard.setOnClickListener(View.OnClickListener {
            selectedRole = "PROFESSIONAL"
            proceedToSignUp()
        })
    }

    private fun proceedToSignUp() {
        // Create intent to pass to SignUp activity
        val intent = Intent(this, SignUp::class.java)
        intent.putExtra("SELECTED_ROLE", selectedRole)
        startActivity(intent)
        finish() // Close this activity
    }
}