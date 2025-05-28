package com.example.pick_dream.model

data class Reservation(
    val userID: String = "",
    val roomID: String = "",
    val eventName: String = "",
    val eventDescription: String = "",
    val eventTarget: String = "",
    val eventParticipants: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "대기"
)