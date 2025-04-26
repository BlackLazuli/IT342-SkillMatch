package com.example.skillmatch.models

data class Portfolio(
    val id: Long? = null,
    val workExperience: String? = null,
    val servicesOffered: List<Service>? = null,
    val clientTestimonials: String? = null,
    val daysAvailable: List<String> = emptyList(),
    val time: String? = null
)