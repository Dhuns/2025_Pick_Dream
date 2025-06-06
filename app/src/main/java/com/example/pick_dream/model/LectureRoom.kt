package com.example.pick_dream.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LectureRoom(
    val id: String = "",
    val name: String = "",
    val buildingName: String = "",
    val buildingDetail: String = "",
    val location: String = "",
    val capacity: Int = 0,
    val equipment: List<String> = emptyList(),
    var isFavorite: Boolean = false,
    var isAvailable: Boolean = true,
    val imageUrl: String? = null
) : Parcelable