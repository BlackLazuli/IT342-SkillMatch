package com.example.skillmatch.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SkillMatchPrefs", Context.MODE_PRIVATE)
    
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_ROLE = "user_role" // Changed from USER_TYPE
    }
    
    // Save auth token
    fun saveAuthToken(token: String?) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }
    
    // Get auth token
    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    
    // Save user ID
    fun saveUserId(userId: String?) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }
    
    // Get user ID
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }
    
    // Save user role (renamed from saveUserType)
    fun saveUserType(role: String?) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role ?: "CUSTOMER") // Default to CUSTOMER if null
        editor.apply()
    }
    
    // Get user role (renamed from getUserType)
    fun getUserType(): String? {
        return prefs.getString(USER_ROLE, null)
    }
    
    // Clear session
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}