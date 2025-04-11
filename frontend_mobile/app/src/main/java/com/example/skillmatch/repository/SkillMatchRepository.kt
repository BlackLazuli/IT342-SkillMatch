package com.example.skillmatch.repository

import android.content.Context
import com.example.skillmatch.api.RetrofitClient

import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import retrofit2.Response

class SkillMatchRepository(private val context: Context) {
    private val apiService = RetrofitClient.apiService
    
    // Make sure your login method looks like this
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val authRequest = LoginRequest(email, password)
        return apiService.login(authRequest)
    }
    
    suspend fun signup(firstName: String, lastName: String, email: String, 
                      password: String, phoneNumber: String, role: String): Response<SignupResponse> {
        val signupRequest = SignupRequest(firstName, lastName, email, password, phoneNumber, role)
        return apiService.signup(signupRequest)
    }

    // User
    suspend fun getUserProfile(userId: String) = apiService.getUserProfile(userId)

    // Professionals

}