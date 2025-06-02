package com.example.pick_dream.model

data class Room(
    val capacity: Int = 0,
    val equipment: List<String> = emptyList(),
    val location: String = "",
    val id: String = "" ,
    val name: String = "",
    val buildingName: String = "",
    val buildingDetail: String = "",
    var isFavorite: Boolean = false
)
