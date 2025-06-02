package com.example.pickdream.ui.home.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Notice(
    val id: Int,
    val iconEmoji: String,
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
                title = "[공지] 공지사항 예시",
                date = "2025.04.07",
                content = "이곳에 공지사항 내용이 표시됩니다."
            )
        )
    }

    fun search(query: String): List<Notice> {
        return _notices.value?.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        } ?: emptyList()
    }

    fun getNoticeById(id: Int): Notice? {
        return _notices.value?.find { it.id == id }
    }
}