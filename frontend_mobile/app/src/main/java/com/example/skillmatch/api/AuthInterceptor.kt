package com.example.skillmatch.api

import android.content.Context
import com.example.skillmatch.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        
        // Add auth token if available
        sessionManager.getAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}