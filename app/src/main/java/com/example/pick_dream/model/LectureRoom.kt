package com.example.pick_dream.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
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
    val chairType: String = "",
    val isProjectorAvailable: Boolean = false,
    val isBlackboardAvailable: Boolean = false,
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isRentalAvailable: Boolean = false,
    val imageUrl: String? = null
) : Parcelable