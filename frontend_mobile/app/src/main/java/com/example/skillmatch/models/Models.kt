package com.example.skillmatch.models

import com.google.gson.annotations.SerializedName

// Authentication models
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val userType: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val userType: String
)

data class SignupResponse(
    val token: String,
    val userId: String,
    val userType: String
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