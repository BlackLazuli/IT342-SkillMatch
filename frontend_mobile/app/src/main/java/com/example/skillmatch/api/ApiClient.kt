package com.example.skillmatch.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Change this to your EC2 backend URL
    private const val BASE_URL = "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("ApiClient", "Request URL: ${request.url}")
            Log.d("ApiClient", "Request Method: ${request.method}")
            
            // Add content type header to all requests
            val newRequest = request.newBuilder()
                .header("Content-Type", "application/json")
                .build()

            val response = chain.proceed(newRequest)
            Log.d("ApiClient", "Response Code: ${response.code}")

            response
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Create a single Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Use the retrofit instance to create the API service
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}


