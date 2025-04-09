package com.example.skillmatch.api

import com.example.skillmatch.models.Appointment
import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import com.example.skillmatch.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<SignupResponse>
    // User endpoints
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>

    // Professional endpoints
    @GET("professionals")
    suspend fun getAllProfessionals(): Response<List<Professional>>

    @GET("professionals/{professionalId}")
    suspend fun getProfessionalProfile(@Path("professionalId") professionalId: String): Response<Professional>

    // Appointment endpoints
    @GET("appointments/professional/{professionalId}")
    suspend fun getProfessionalAppointments(@Path("professionalId") professionalId: String): Response<List<Appointment>>

    @GET("appointments/customer/{customerId}")
    suspend fun getCustomerAppointments(@Path("customerId") customerId: String): Response<List<Appointment>>

    @POST("appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Response<Appointment>
}