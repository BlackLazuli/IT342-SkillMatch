package com.example.skillmatch.models

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
// Add or update these models in your Models.kt file
data class SignupRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val role: String
)

data class SignupResponse(
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val token: String
)
// Update the User data class to include profilePicture (renamed from profileImage)
data class User(
    val id: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val role: String?, // "CUSTOMER" or "SERVICE_PROVIDER"
    val bio: String?,
    val phoneNumber: String?,
    val rating: Double?,
    val occupation: String?, // For service providers
    val availableDays: String?, // For service providers
    val availableHours: String?, // For service providers
    val location: Location?,
    val portfolio: Portfolio?,
    val profilePicture: String?, // Renamed from profileImage to match backend
    val userId: String? = null // Made nullable with default value
)



data class Location(
    val id: Long? = null,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
