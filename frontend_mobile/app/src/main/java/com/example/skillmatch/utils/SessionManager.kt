package com.example.skillmatch.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SkillMatchPrefs", Context.MODE_PRIVATE)
    
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_TYPE = "user_type"
    }
    
    // Save auth token
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }
    
    // Get auth token
    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    
    // Save user ID
    fun saveUserId(userId: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }
    
    // Get user ID
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }
    
    // Save user type
    fun saveUserType(userType: String) {
        val editor = prefs.edit()
        editor.putString(USER_TYPE, userType)
        editor.apply()
    }
    
    // Get user type
    fun getUserType(): String? {
        return prefs.getString(USER_TYPE, null)
    }
    
    // Clear session
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}