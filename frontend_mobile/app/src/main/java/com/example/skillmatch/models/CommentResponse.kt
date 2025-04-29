package com.example.skillmatch.models

import java.time.LocalDateTime

data class CommentResponse(
    val id: Long,
    val message: String,
    val timestamp: String,
    val rating: Int,
    val authorName: String,
    val profilePicture: String?
)