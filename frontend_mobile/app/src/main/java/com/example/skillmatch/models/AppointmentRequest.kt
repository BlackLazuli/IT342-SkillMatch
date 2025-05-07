package com.example.skillmatch.models

import com.google.gson.annotations.SerializedName

data class AppointmentRequest(
    val id: Long? = null,
    val user: UserReference,
    val portfolio: PortfolioReference,
    val service: ServiceReference? = null,  // Add service reference
    val role: String,
    val appointmentTime: String, // Keep as string in ISO format
    val status: String? = null,
    val notes: String?,
    val createdAt: String? = null
)

data class UserReference(
    val id: Long
)

data class PortfolioReference(
    val id: Long
)

data class ServiceReference(  // New class for service reference
    val id: Long
)