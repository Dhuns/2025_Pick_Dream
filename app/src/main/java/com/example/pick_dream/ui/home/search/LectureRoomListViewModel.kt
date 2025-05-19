package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pick_dream.ui.home.search.LectureRoom

// 강의실 데이터 모델 (LectureRoom) 재사용
// data class LectureRoom(val name: String, val building: String)

class LectureRoomListViewModel : ViewModel() {
    // 전체 강의실 리스트 (실제 앱에서는 DB/서버 연동)
    private val allRooms = listOf(
        LectureRoom("5022 강의실", "예지관", "4강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "덕문관", "5강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "광교관", "6강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "집현관", "7강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "호연관", "9강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "명학관", "8강의동", "빔프로젝터, 마이크, 콘센트, 스크린"),
        LectureRoom("5022 강의실", "성실관", "3강의동", "빔프로젝터, 마이크, 콘센트, 스크린")
    )

    private val _rooms = MutableLiveData<List<LectureRoom>>(allRooms)
    val rooms: LiveData<List<LectureRoom>> = _rooms

    private val _recentSearches = MutableLiveData<List<String>>(emptyList())
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    // 검색어 입력 시 필터링
    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _rooms.value = allRooms
        } else {
            _rooms.value = allRooms.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.buildingName.contains(query, ignoreCase = true) ||
                it.buildingDetail.contains(query, ignoreCase = true) ||
                it.roomInfo.contains(query, ignoreCase = true)
            }
        }
    }

    // 최근 검색어 추가
    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value?.toMutableList() ?: mutableListOf()
        current.remove(query) // 중복 제거
        current.add(0, query)
        if (current.size > 10) current.removeLast() // 최대 10개
        _recentSearches.value = current
    }

    // 최근 검색어 삭제
    fun removeRecentSearch(query: String) {
        val current = _recentSearches.value?.toMutableList() ?: mutableListOf()
        current.remove(query)
        _recentSearches.value = current
    }

    // 필터 예시 (실제 구현 시 필터 조건 추가)
    fun filterByFacility(facility: String) {
        _rooms.value = allRooms.filter {
            it.name.contains(facility, ignoreCase = true) ||
            it.buildingName.contains(facility, ignoreCase = true) ||
            it.buildingDetail.contains(facility, ignoreCase = true) ||
            it.roomInfo.contains(facility, ignoreCase = true)
        }
    }

    // 전체 리스트로 초기화
    fun resetFilter() {
        _rooms.value = allRooms
    }
}