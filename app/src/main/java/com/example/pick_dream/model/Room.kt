package com.example.pick_dream.model

data class Room(
    val capacity: Int = 0,
    val equiment: List<String> = emptyList(),
    val location: String = "",
    val id: String = "" // 강의실 번호(문서ID)
)
