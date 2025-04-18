package com.example.skillmatch.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SkillMatchPrefs", Context.MODE_PRIVATE)
    
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        const val USER_FIRST_NAME = "user_first_name"
        const val USER_LAST_NAME = "user_last_name"
        const val KEY_TOKEN = "token"
    }
    
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
        Log.d("SessionManager", "Token saved: $token")
    }
    
    fun getAuthToken(): String? {
        val token = prefs.getString(USER_TOKEN, null)
        Log.d("SessionManager", "Retrieved token: $token")
        return token
    }
    
    // Add these individual methods
    fun saveUserId(userId: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
        Log.d("SessionManager", "User ID saved: $userId")
    }
    
    fun saveUserType(role: String) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role)
        editor.apply()
    }
    
    fun saveUserDetails(userId: String, email: String, role: String, firstName: String, lastName: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, role)
        editor.putString(USER_FIRST_NAME, firstName)
        editor.putString(USER_LAST_NAME, lastName)
        editor.apply()
        Log.d("SessionManager", "User details saved - ID: $userId, Role: $role")
    }
    
    fun getUserId(): String? {
        val userId = prefs.getString(USER_ID, null)
        Log.d("SessionManager", "Retrieved user ID: $userId")
        return userId
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }
    
    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }
    
    fun getUserFirstName(): String? {
        return prefs.getString(USER_FIRST_NAME, null)
    }
    
    fun getUserLastName(): String? {
        return prefs.getString(USER_LAST_NAME, null)
    }
    
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
    
    // Add this method to your SessionManager class
    // Fix this method to use prefs instead of sharedPreferences
    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null) ?: prefs.getString(USER_TOKEN, null)
        Log.d("SessionManager", "Retrieved token from getToken(): $token")
        return token
    }
}