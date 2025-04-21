package com.example.skillmatch.models

data class Professional(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val occupation: String,
    val bio: String? = null,
    val phoneNumber: String? = null,
    val rating: Double? = null,
    val profilePicture: String? = null,
    val location: Location? = null,
    val availableDays: List<String> = emptyList(),
    val availableHours: String = ""
) {
    fun getFullName(): String = "$firstName $lastName"
    
    fun getInitial(): String = firstName.firstOrNull()?.toString() ?: "?"
}