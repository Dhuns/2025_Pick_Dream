package com.example.pick_dream.ui.home.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth

object LectureRoomRepository {
    private val _roomsLiveData = MutableLiveData<List<LectureRoom>>()
    val roomsLiveData: LiveData<List<LectureRoom>> get() = _roomsLiveData

    private val _wishlistsLiveData = MutableLiveData<List<String>>()
    val wishlistsLiveData: LiveData<List<String>> get() = _wishlistsLiveData

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun fetchRoomsFromFirebase() {
        db.collection("rooms")
            .get()
            .addOnSuccessListener { result ->
                val roomList = result.mapNotNull { it.toObject<LectureRoom>() }
                _roomsLiveData.value = roomList
                fetchWishlists()
            }
            .addOnFailureListener { exception ->
            }
    }

    fun fetchWishlists() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("User")
            .document(userId)
            .collection("wishlists")
            .get()
            .addOnSuccessListener { result ->
                val wishlistIds = result.documents.map { it.id }
                _wishlistsLiveData.value = wishlistIds
                updateFavoriteStatus(wishlistIds)
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun updateFavoriteStatus(wishlistIds: List<String>) {
        _roomsLiveData.value = _roomsLiveData.value?.map { room ->
            val roomNumber = room.name.replace(Regex("[^0-9]"), "")
            room.copy(isFavorite = wishlistIds.contains(roomNumber))
        }
    }

    fun toggleFavorite(name: String) {
        val userId = auth.currentUser?.uid ?: return
        val userWishlistRef = db.collection("User").document(userId).collection("wishlists")

        val roomNumber = name.replace(Regex("[^0-9]"), "")
        
        if (_wishlistsLiveData.value?.contains(roomNumber) == true) {
            userWishlistRef.document(roomNumber).delete()
                .addOnSuccessListener {
                    fetchWishlists()
                }
        } else {
            userWishlistRef.document(roomNumber)
                .set(mapOf("timestamp" to com.google.firebase.Timestamp.now()))
                .addOnSuccessListener {
                    fetchWishlists()
                }
        }
    }

    fun getFavorites(): List<LectureRoom> {
        val wishlistIds = _wishlistsLiveData.value ?: return emptyList()
        return _roomsLiveData.value?.filter { room ->
            val roomNumber = room.name.replace(Regex("[^0-9]"), "")
            wishlistIds.contains(roomNumber)
        } ?: emptyList()
    }
}
