package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pick_dream.model.LectureRoom

class LectureRoomDetailViewModel : ViewModel() {
    private val _roomDetail = MutableLiveData<LectureRoom>()
    val roomDetail: LiveData<LectureRoom> = _roomDetail

    fun loadRoomDetail(roomName: String) {
        val roomItem = LectureRoomRepository.lectureRoomsWithFavorites.value?.find { item ->
            item is ListItem.RoomItem && item.lectureRoom.name == roomName
        }
        
        if (roomItem is ListItem.RoomItem) {
            _roomDetail.value = roomItem.lectureRoom
        }
    }
} 