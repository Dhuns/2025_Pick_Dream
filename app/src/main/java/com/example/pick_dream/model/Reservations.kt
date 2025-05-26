package com.example.pick_dream.model

import com.google.firebase.Timestamp

data class Reservation(
    val userID: String = "",
    val roomID: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
)
