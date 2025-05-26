package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LectureRoomDetailViewModel : ViewModel() {
    private val _roomDetail = MutableLiveData<LectureRoom>()
    val roomDetail: LiveData<LectureRoom> = _roomDetail

    fun loadRoomDetail(roomId: String) {
        // Firebase에서 해당 강의실 정보를 가져오는 로직
        // 현재는 Repository를 통해 전체 목록에서 찾는 방식 사용
        LectureRoomRepository.roomsLiveData.value?.find { it.name == roomId }?.let {
            _roomDetail.value = it
        }
    }
} 