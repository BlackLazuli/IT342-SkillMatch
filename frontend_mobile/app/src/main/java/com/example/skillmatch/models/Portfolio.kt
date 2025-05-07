package com.example.skillmatch.models

data class Portfolio(
    val id: Long? = null,
    val user: User? = null,
    val workExperience: String? = null,
    val servicesOffered: List<Service>? = null,
    val clientTestimonials: String? = null,
    val daysAvailable: List<String> = emptyList(),
    val startTime: String? = null,
    val endTime: String? = null,
    val time: String? = null,  // Keep for backward compatibility
    val comments: List<Any>? = null,
    val appointments: List<Any>? = null
)



