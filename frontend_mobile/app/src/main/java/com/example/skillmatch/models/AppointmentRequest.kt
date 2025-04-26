package com.example.skillmatch.models

data class AppointmentRequest(
    val id: Long? = null,
    val userId: Long,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val role: String,
    val portfolioId: Long,
    val appointmentTime: String, // Keep as string in ISO format
    val status: String? = null,
    val notes: String?,
    val createdAt: String? = null

)