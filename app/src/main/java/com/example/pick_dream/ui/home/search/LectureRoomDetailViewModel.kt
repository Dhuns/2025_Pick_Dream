package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LectureRoomDetailViewModel : ViewModel() {
    private val _roomDetail = MutableLiveData<LectureRoom>()
    val roomDetail: LiveData<LectureRoom> = _roomDetail

    fun loadRoomDetail(roomId: String) {
        LectureRoomRepository.roomsLiveData.value?.find { it.name == roomId }?.let {
            _roomDetail.value = it
        }
    }
} 