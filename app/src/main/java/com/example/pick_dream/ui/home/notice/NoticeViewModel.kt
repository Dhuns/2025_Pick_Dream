package com.example.pickdream.ui.home.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Notice(
    val id: Int,
    val iconEmoji: String, // "📢" 또는 "🎉"
    val title: String,
    val date: String,
    val content: String
)

class NoticeViewModel : ViewModel() {

    private val _notices = MutableLiveData<List<Notice>>()
    val notices: LiveData<List<Notice>> = _notices

    init {
        _notices.value = listOf(
            Notice(
                id = 1,
                iconEmoji = "📢",
                title = "[공지사항] 덕문관 5001 강의실 대여 불가 안내",
                date = "2025.04.07",
                content = """
                    덕문관 5001호 강의실은 아래와 같은 사유로 인해 일시적으로 대여가 제한됩니다.
                    이용에 참고해 주시기 바랍니다.

                    ❌ 제한 사유
                    - 시설 점검 및 유지보수 작업
                    - 전기 설비 및 냉난방 시스템 정기 점검 예정

                    🕒 대여 제한 기간
                    2025년 4월 10일(목) ~ 2025년 4월 13일(일)

                    📌 대체 강의실 안내
                    - 덕문관 5003호 (최대 20명, 전자칠판)
                    - 예지관 4340호 (최대 25명, 빔프로젝터)

                    ⚠️ 유의사항
                    예약 시, 덕문관 5001호는 검색 또는 예약 목록에 표시되지 않습니다.
                """.trimIndent()
            ),
            Notice(
                id = 2,
                iconEmoji = "🎉",
                title = "[이벤트] 강의실 사용 후기 이벤트 안내",
                date = "2025.04.07",
                content = """
                    강의실 사용 후기를 남기면 추첨을 통해 다양한 상품을 드립니다!

                    - 참여 방법: 강의실 이용 후 앱에서 후기 작성
                    - 이벤트 기간: 2025.04.07 ~ 2025.04.30
                    - 경품: 스타벅스 기프티콘 등

                    많은 참여 바랍니다!
                """.trimIndent()
            ),
            Notice(
                id = 3,
                iconEmoji = "📢",
                title = "[공지사항] 덕문관 5001 강의실 대여 불가 안내",
                date = "2025.04.07",
                content = "상세 내용이 동일합니다."
            ),
            Notice(
                id = 4,
                iconEmoji = "🎉",
                title = "[이벤트] 강의실 사용 후기 이벤트 안내",
                date = "2025.04.07",
                content = "상세 내용이 동일합니다."
            )
        )
    }

    // (선택) 검색 기능 예시
    fun search(query: String): List<Notice> {
        return _notices.value?.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        } ?: emptyList()
    }

    // (선택) id로 Notice 찾기
    fun getNoticeById(id: Int): Notice? {
        return _notices.value?.find { it.id == id }
    }
}