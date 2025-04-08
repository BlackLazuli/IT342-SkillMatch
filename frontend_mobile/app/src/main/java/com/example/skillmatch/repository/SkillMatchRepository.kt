package com.example.skillmatch.repository

import android.content.Context
import com.example.skillmatch.api.RetrofitClient
import com.example.skillmatch.models.Appointment
import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import retrofit2.Response

class SkillMatchRepository(private val context: Context) {
    private val apiService = RetrofitClient.apiService
    
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val loginRequest = LoginRequest(email, password)
        return apiService.login(loginRequest)
    }
    
    suspend fun signup(firstName: String, lastName: String, email: String, 
                      password: String, phoneNumber: String, role: String): Response<SignupResponse> {
        val signupRequest = SignupRequest(firstName, lastName, email, password, phoneNumber, role)
        return apiService.signup(signupRequest)
    }

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