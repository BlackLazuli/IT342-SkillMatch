package com.example.skillmatch.api

import com.example.skillmatch.models.Appointment
import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import com.example.skillmatch.models.User
import com.example.skillmatch.models.Comment
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Rating
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
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
    @GET("users/getUserById/{id}")
    suspend fun getUserById(@Path("id") id: String): User
    
    @GET("users/getAllUsers")
    suspend fun getAllUsers(): List<User>
    
    // Appointment endpoints
    @GET("appointments/user/{userId}")
    suspend fun getAppointmentsByUser(
        @Path("userId") userId: String, 
        @Query("role") role: String
    ): List<Appointment>
    
    @GET("appointments/{id}")
    suspend fun getAppointmentById(@Path("id") id: String): Appointment
    
    @POST("appointments/")
    suspend fun bookAppointment(@Body appointment: Appointment): Response<Appointment>
    
    @PUT("appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("id") id: String, 
        @Query("newTime") newTime: String
    ): Response<Appointment>
    
    @PUT("appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: String): Response<Appointment>
    
    // Comment endpoints
    @GET("comments/portfolio/{portfolioId}")
    suspend fun getCommentsByPortfolio(@Path("portfolioId") portfolioId: String): List<Comment>
    
    @POST("comments/{userId}/{portfolioId}")
    suspend fun addComment(
        @Path("userId") userId: String,
        @Path("portfolioId") portfolioId: String,
        @Body comment: Comment
    ): Response<Comment>
    
    // Location endpoints
    @GET("locations/{userId}")
    suspend fun getLocationByUserId(@Path("userId") userId: String): Location
    
    @POST("locations/{userId}")
    suspend fun saveOrUpdateLocation(
        @Path("userId") userId: String,
        @Body location: Location
    ): Response<Location>
    
    // Portfolio endpoints
    @GET("portfolios/{userId}")
    suspend fun getPortfolioByUserId(@Path("userId") userId: String): Portfolio
    
    @POST("portfolios/{userId}")
    suspend fun createOrUpdatePortfolio(
        @Path("userId") userId: String,
        @Body portfolio: Portfolio
    ): Response<Portfolio>
    
    // Rating endpoints
    @GET("ratings/user/{userId}")
    suspend fun getRatingsForUser(@Path("userId") userId: String): List<Rating>
    
    @POST("ratings/")
    suspend fun addRating(@Body rating: Rating): Response<Rating>
}