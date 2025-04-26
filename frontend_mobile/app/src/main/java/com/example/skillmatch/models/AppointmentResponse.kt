package com.example.skillmatch.models

data class AppointmentResponse(
    val id: Long,
    val userId: Long,
    val userFirstName: String,
    val userLastName: String,
    val role: String,
    val appointmentTime: String,
    val status: String,
    val notes: String?,
    val createdAt: String,
    val portfolioId: Long,
    val professionalFirstName: String? = null,
    val professionalLastName: String? = null,
    val providerFirstName: String? = null,
    val providerLastName: String? = null,
    val providerId: Long? = null
)