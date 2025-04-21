package com.example.skillmatch.services

import com.example.skillmatch.api.ApiClient
import com.example.skillmatch.models.Professional
import com.example.skillmatch.models.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfessionalService {
    
    /**
     * Get all professionals (service providers)
     */
    suspend fun getAllProfessionals(): List<Professional> = withContext(Dispatchers.IO) {
        val response = ApiClient.apiService.getAllUsers()
        
        if (response.isSuccessful && response.body() != null) {
            val allUsers = response.body()!!
            val professionalUsers = allUsers.filter { it.role == "SERVICE_PROVIDER" }
            
            professionalUsers.map { user ->
                // Extract occupation from bio or occupation field
                val occupation = when {
                    !user.occupation.isNullOrBlank() -> user.occupation
                    !user.bio.isNullOrBlank() -> extractOccupationFromBio(user.bio)
                    else -> "Professional"
                }
                
                // Parse available days and hours
                val availableDays = user.availableDays?.split(",")?.map { it.trim() } ?: emptyList()
                val availableHours = user.availableHours ?: ""
                
                Professional(
                    id = user.id?.toLong() ?: 0L,
                    firstName = user.firstName ?: "",
                    lastName = user.lastName ?: "",
                    email = user.email ?: "",
                    occupation = occupation,
                    bio = user.bio,
                    phoneNumber = user.phoneNumber,
                    rating = user.rating,
                    profilePicture = user.profilePicture,
                    location = user.location,
                    availableDays = availableDays,
                    availableHours = availableHours
                )
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Search professionals by name, occupation, or location
     */
    suspend fun searchProfessionals(query: String): List<Professional> = withContext(Dispatchers.IO) {
        val allProfessionals = getAllProfessionals()
        
        if (query.isBlank()) {
            return@withContext allProfessionals
        }
        
        val lowercaseQuery = query.lowercase()
        
        allProfessionals.filter { professional ->
            professional.getFullName().lowercase().contains(lowercaseQuery) ||
            professional.occupation.lowercase().contains(lowercaseQuery) ||
            professional.bio?.lowercase()?.contains(lowercaseQuery) == true ||
            professional.location?.address?.lowercase()?.contains(lowercaseQuery) == true
        }
    }
    
    /**
     * Get professionals sorted by distance from a given location
     */
    suspend fun getProfessionalsByLocation(userLocation: Location): List<Professional> = withContext(Dispatchers.IO) {
        val allProfessionals = getAllProfessionals()
        
        allProfessionals.sortedBy { professional ->
            professional.location?.let { location ->
                calculateDistance(
                    userLocation.latitude,
                    userLocation.longitude,
                    location.latitude,
                    location.longitude
                )
            } ?: Double.MAX_VALUE // Put professionals without location at the end
        }
    }
    
    /**
     * Get a professional by ID
     */
    suspend fun getProfessionalById(id: Long): Professional? = withContext(Dispatchers.IO) {
        val response = ApiClient.apiService.getUserProfile(id.toString())
        
        if (response.isSuccessful && response.body() != null) {
            val user = response.body()!!
            
            if (user.role != "SERVICE_PROVIDER") {
                return@withContext null
            }
            
            // Extract occupation from bio or occupation field
            val occupation = when {
                !user.occupation.isNullOrBlank() -> user.occupation
                !user.bio.isNullOrBlank() -> extractOccupationFromBio(user.bio)
                else -> "Professional"
            }
            
            // Parse available days and hours
            val availableDays = user.availableDays?.split(",")?.map { it.trim() } ?: emptyList()
            val availableHours = user.availableHours ?: ""
            
            Professional(
                id = user.id?.toLong() ?: 0L,
                firstName = user.firstName ?: "",
                lastName = user.lastName ?: "",
                email = user.email ?: "",
                occupation = occupation,
                bio = user.bio,
                phoneNumber = user.phoneNumber,
                rating = user.rating,
                profilePicture = user.profilePicture,
                location = user.location,
                availableDays = availableDays,
                availableHours = availableHours
            )
        } else {
            null
        }
    }
    
    // Helper function to extract occupation from bio
    private fun extractOccupationFromBio(bio: String): String {
        // Simple extraction - look for common occupation indicators
        val occupationIndicators = listOf(
            "I am a ", "I'm a ", "I work as a ", "I'm working as a ",
            "I am an ", "I'm an ", "I work as an ", "I'm working as an "
        )
        
        for (indicator in occupationIndicators) {
            val index = bio.indexOf(indicator, ignoreCase = true)
            if (index >= 0) {
                val start = index + indicator.length
                val end = bio.indexOf(".", start).takeIf { it > 0 } 
                    ?: bio.indexOf(",", start).takeIf { it > 0 }
                    ?: bio.indexOf("\n", start).takeIf { it > 0 }
                    ?: minOf(start + 20, bio.length)
                
                if (end > start) {
                    return bio.substring(start, end).trim()
                }
            }
        }
        
        return "Professional" // Default if no occupation found
    }
    
    // Calculate distance between two points using Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c // Distance in km
    }
}