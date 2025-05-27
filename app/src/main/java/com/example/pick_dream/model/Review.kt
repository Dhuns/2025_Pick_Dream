package com.example.pick_dream.model

import com.google.firebase.Timestamp

data class Review(
    val comment: String = "",
    val createdAt: Timestamp? = null,  // ✅ 수정된 부분
    val rating: Int = 0,
    val roomID: String = "",
    val userID: String = "",
    val eventName: String = "발표리허설" // 기본값 설정
)

