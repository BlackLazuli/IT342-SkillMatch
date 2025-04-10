package com.example.skillmatch.models

import com.google.gson.annotations.SerializedName
import retrofit2.Response

// Authentication models
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val email: String,
    val userId: String,
    val token: String,
    val role: String,
    val firstName: String,
    val lastName: String
)

// Update your SignupRequest model to match backend expectations
data class SignupRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val role: String
)

data class SignupResponse(
    val email: String,
    val userId: String,
    val token: String,
    val role: String, // Changed from userType
    val firstName: String,
    val lastName: String
)
// User models
data class User(
    val id: String,
    val name: String,
    val email: String,
    val userType: String,
    val profilePicture: String? = null
)

// Professional model
data class Professional(
    val id: String,
    val name: String,
    val profession: String,
    val rating: Float,
    val workingDays: String,
    val workingHours: String,
    val profilePicture: String? = null
)

// Appointment model
data class Appointment(
    val id: String? = null,
    val customerId: String,
    val customerName: String,
    val professionalId: String,
    val professionalName: String,
    val date: String,
    val time: String,
    val status: String = "pending"
)