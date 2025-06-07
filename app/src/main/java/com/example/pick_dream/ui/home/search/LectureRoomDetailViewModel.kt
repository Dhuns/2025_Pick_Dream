package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pick_dream.model.LectureRoom
import com.google.firebase.firestore.FirebaseFirestore

class LectureRoomDetailViewModel : ViewModel() {
    private val _roomDetail = MutableLiveData<LectureRoom>()
    val roomDetail: LiveData<LectureRoom> = _roomDetail

    fun loadRoomDetail(roomName: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rooms")
            .whereEqualTo("name", roomName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()
                    val room = document.toObject(LectureRoom::class.java).copy(id = document.id)
                    _roomDetail.value = room
                }
            }
    }
} 