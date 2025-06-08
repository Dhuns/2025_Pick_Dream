package com.example.pick_dream.model


data class Reservation(
    var documentId: String = "",
    val userID: String = "",
    val roomID: String = "",
    val eventName: String = "",
    val eventDescription: String = "",
    val eventTarget: String = "",
    val eventParticipants: Int = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String = "대기"
)