package com.example.skillmatch.api

import com.example.skillmatch.models.LoginRequest
import com.example.skillmatch.models.LoginResponse
import com.example.skillmatch.models.SignupRequest
import com.example.skillmatch.models.SignupResponse
import com.example.skillmatch.models.User
import com.example.skillmatch.models.Location
import com.example.skillmatch.models.Portfolio

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>
    
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Response<User>
    
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

    // Make sure your ApiService has this method defined correctly
    @POST("portfolios/{userId}")
    suspend fun createOrUpdatePortfolio(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body portfolio: Portfolio
    ): Response<Portfolio>
    
    @PUT("portfolios/{userId}")
    suspend fun updatePortfolio(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body portfolio: Portfolio
    ): Response<Portfolio>
}