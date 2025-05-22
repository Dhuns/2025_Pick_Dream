package com.example.pick_dream.model

data class Room(
    val capacity: Int = 0,
    val equipment: List<String> = emptyList(),
    val location: String = "",
    val id: String = "" ,// 강의실 번호(문서ID)
    val name: String = "",
    val buildingName: String = "",
    val buildingDetail: String = "",
    var isFavorite: Boolean = false
)
