package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pick_dream.R

// 강의실 상세 데이터 모델
data class LectureRoomDetail(
    val id: String,
    val name: String,
    val desc: String,
    val imageRes: Int,
    val infoList: List<String>
)

class LectureRoomDetailViewModel : ViewModel() {
    private val _roomDetail = MutableLiveData<LectureRoomDetail>()
    val roomDetail: LiveData<LectureRoomDetail> = _roomDetail

    private val _isFavorite = MutableLiveData<Boolean>(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    // 예시: 강의실 상세 불러오기 (실제 앱에서는 DB/서버 연동)
    fun loadRoomDetail(roomId: String) {
        // 임시 데이터. 실제로는 roomId로 DB/서버에서 조회
        _roomDetail.value = LectureRoomDetail(
            id = roomId,
            name = "덕문관 (5강의동)",
            desc = "엘리베이터 없음",
            imageRes = R.drawable.sample_room,
            infoList = listOf(
                "강의실 : 5105 강의실",
                "기자재 목록 : 빔프로젝터, 마이크, 콘센트, 스크린",
                "의자 : 일체형 의자",
                "빔 프로젝터 대여 여부 : 사용가능",
                "전자 칠판 대여 여부 : 사용불가 (없음)",
                "장소 대여 여부 : ",
                "앱에서 바로 예약 가능"
            )
        )
        // 예시: 찜 여부도 DB/서버에서 받아올 수 있음
        _isFavorite.value = false
    }

    fun toggleFavorite() {
        _isFavorite.value = !(_isFavorite.value ?: false)
        // 실제 앱에서는 DB/서버에 찜 상태 저장/삭제 로직 추가
    }
}