package com.example.pick_dream.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val userID: String = "",
    val roomID: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    val purpose: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
)

