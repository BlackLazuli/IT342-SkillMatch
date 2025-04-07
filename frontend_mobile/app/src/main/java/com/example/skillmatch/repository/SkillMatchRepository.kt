package com.example.skillmatch.repository

import android.content.Context
import com.example.skillmatch.api.RetrofitClient
import com.example.skillmatch.models.Appointment
import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.User

class SkillMatchRepository(private val context: Context) {
    private val apiService = RetrofitClient.getApiService(context)

    // Authentication
    suspend fun login(email: String, password: String) =
        apiService.login(LoginRequest(email, password))

    suspend fun signup(name: String, email: String, password: String, userType: String) =
        apiService.signup(SignupRequest(name, email, password, userType))

    // User
    suspend fun getUserProfile(userId: String) = apiService.getUserProfile(userId)

    // Professionals
    suspend fun getAllProfessionals() = apiService.getAllProfessionals()

    suspend fun getProfessionalProfile(professionalId: String) =
        apiService.getProfessionalProfile(professionalId)

    // Appointments
    suspend fun getProfessionalAppointments(professionalId: String) =
        apiService.getProfessionalAppointments(professionalId)

    suspend fun getCustomerAppointments(customerId: String) =
        apiService.getCustomerAppointments(customerId)

    suspend fun createAppointment(appointment: Appointment) =
        apiService.createAppointment(appointment)
}