package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object LectureRoomRepository {
    private val _rooms = mutableListOf<LectureRoom>(
        LectureRoom("5022 강의실", "덕문관", "5강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5023 강의실", "덕문관", "5강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5024 강의실", "덕문관", "5강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("6022 강의실", "광교관", "6강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("6023 강의실", "광교관", "6강의동", "빔프로젝터, 마이크, 콘센트, 스크린")
    )
    private val _roomsLiveData = MutableLiveData<List<LectureRoom>>(_rooms)
    val roomsLiveData: LiveData<List<LectureRoom>> get() = _roomsLiveData

    fun getRoomByName(name: String): LectureRoom? = _rooms.find { it.name == name }

    fun toggleFavorite(name: String) {
        getRoomByName(name)?.let {
            it.isFavorite = !it.isFavorite
            _roomsLiveData.value = _rooms.toList() // 변경 알림
        }
    }

    fun isFavorite(name: String): Boolean = getRoomByName(name)?.isFavorite == true
    fun getFavorites(): List<LectureRoom> = _rooms.filter { it.isFavorite }
}