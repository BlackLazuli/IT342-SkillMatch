package com.example.skillmatch.models

data class Professional(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val occupation: String,
    val rating: Float = 0f,
    val availableDays: List<String> = emptyList(),
    val availableHours: String = ""
)