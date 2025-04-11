package com.example.skillmatch.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SkillMatchPrefs", Context.MODE_PRIVATE)
    
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        const val USER_FIRST_NAME = "user_first_name"
        const val USER_LAST_NAME = "user_last_name"
    }
    
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }
    
    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    
    // Add these individual methods
    fun saveUserId(userId: String) {
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
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
    }
    
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
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
}