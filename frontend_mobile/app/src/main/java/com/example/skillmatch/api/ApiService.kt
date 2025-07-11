package com.example.skillmatch.api

import com.example.skillmatch.models.AppointmentRequest
import com.example.skillmatch.models.AppointmentResponse
import com.example.skillmatch.models.CommentRequest
import com.example.skillmatch.models.CommentResponse
import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import com.example.skillmatch.models.User
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.Portfolio
import com.example.skillmatch.models.Service

import retrofit2.Response
import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import android.graphics.BitmapFactory
import android.util.Base64

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>

    // Add this endpoint to get all users (for finding professionals)
    @GET("users/getAllUsers")
    suspend fun getAllUsers(): Response<List<User>>

    // Updated to match the backend controller endpoint
    @PUT("users/updateUser/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<User>

    // Update the location endpoint to use PUT instead of POST
    @PUT("locations/update/{userId}")
    suspend fun updateLocation(
        @Path("userId") userId: String,
        @Body location: Location
    ): Response<Location>

    // Portfolio endpoints - updated to match backend controller paths
    @GET("portfolios/{userId}")
    suspend fun getPortfolio(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<Portfolio>
    
    @GET("portfolios/getAllPortfolios")
    suspend fun getAllPortfolios(): Response<List<Portfolio>>
    // Make sure your ApiService has this method defined correctly
    @POST("portfolios/{userId}")
    suspend fun createOrUpdatePortfolio(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body portfolio: Portfolio
    ): Response<Portfolio>

    // Update this method to use userId in the path, not portfolioId
    @PUT("portfolios/{userId}")
    suspend fun updatePortfolio(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body portfolio: Portfolio
    ): Response<Portfolio>

    // Add these methods for profile picture operations
    @Multipart
    @PUT("users/{id}/uploadProfilePicture")
    suspend fun uploadProfilePicture(
        @Path("id") userId: String,
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<User>

    // Simple method to get user with profile picture
    @GET("users/{userId}")
    suspend fun getUserWithProfilePicture(@Path("userId") userId: String): Response<User>

    @POST("appointments/")
    suspend fun bookAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

    // New appointment management endpoints
    // Update to match the backend controller endpoints
    @GET("appointments/user/{userId}")
    fun getAllAppointmentsForUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("role") role: String
    ): Call<List<AppointmentResponse>>

    // For professionals, use the all endpoint which includes provider details
    @GET("appointments/all/{userId}")
    fun getAllAppointmentsForProfessional(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Call<List<AppointmentResponse>>


    @PUT("appointments/{id}/reschedule")
    fun rescheduleAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: Long,
        @Query("newTime") newTime: String
    ): Call<AppointmentResponse>

    @PUT("appointments/{id}/cancel")
    fun cancelAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: Long
    ): Call<AppointmentResponse>
    
    @PUT("appointments/{id}/complete")
    fun completeAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: Long
    ): Call<AppointmentResponse>

    // Add comment to a professional's portfolio - Fixed endpoint URL
    @POST("comments/{userId}/{portfolioId}")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Path("portfolioId") portfolioId: String,
        @Body commentRequest: CommentRequest
    ): Response<CommentResponse>
    
    // Get comments for a portfolio - Fixed endpoint URL
    @GET("comments/portfolio/{portfolioId}") 
    suspend fun getCommentsByPortfolio( 
        @Header("Authorization") token: String, 
        @Path("portfolioId") portfolioId: String 
    ): Response<List<CommentResponse>>
    
    // Mark appointment as rated
    @PUT("appointments/{appointmentId}/markRated")
    suspend fun markAppointmentAsRated(
        @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: Long
    ): Response<AppointmentResponse>
    
    // Add the missing endpoint for getting services by portfolio
    @GET("services/portfolio/{portfolioId}")
    suspend fun getServicesByPortfolio(
        @Header("Authorization") token: String,
        @Path("portfolioId") portfolioId: String
    ): Response<List<Service>>

    @GET("portfolios/portfolio/{portfolioId}")
    suspend fun getPortfolioById(
        @Header("Authorization") token: String,
        @Path("portfolioId") portfolioId: Long
    ): Response<Portfolio>

}