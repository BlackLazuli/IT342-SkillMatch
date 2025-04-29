package com.example.skillmatch.models

data class AppointmentResponse(
    val id: Long,
    val userId: Long,
    val userFirstName: String?,
    val userLastName: String?,
    val providerId: Long,
    val providerFirstName: String?,
    val providerLastName: String?,
    val portfolioId: Long,
    val role: String,
    val appointmentTime: String,
    val status: String,
    val notes: String?,
    val createdAt: String,
    val rated: Boolean? = false
)

