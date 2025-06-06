package com.example.pick_dream.ui.home.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pick_dream.model.LectureRoom
import com.google.firebase.firestore.FieldValue

sealed class ListItem {
    data class HeaderItem(val buildingName: String) : ListItem()
    data class RoomItem(val lectureRoom: LectureRoom) : ListItem()
}

object LectureRoomRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val allRooms = MutableLiveData<List<LectureRoom>>()
    private val favoriteRoomIds = MutableLiveData<List<String>>()

    // 최종적으로 UI에 보여줄 LiveData. allRooms나 favoriteRoomIds가 변경되면 자동으로 업데이트됨
    val lectureRoomsWithFavorites = MediatorLiveData<List<ListItem>>()

    init {
        // 두 LiveData를 관찰하고, 변경이 있을 때마다 데이터를 조합하는 로직
        lectureRoomsWithFavorites.addSource(allRooms) { rooms ->
            combineRoomsAndFavorites(rooms, favoriteRoomIds.value)
        }
        lectureRoomsWithFavorites.addSource(favoriteRoomIds) { ids ->
            combineRoomsAndFavorites(allRooms.value, ids)
        }
    }

    private fun combineRoomsAndFavorites(rooms: List<LectureRoom>?, ids: List<String>?) {
        if (rooms == null || ids == null) {
            return
        }

        val updatedRooms = rooms.map { room ->
            room.copy(isFavorite = ids.contains(room.id))
        }

        // buildingDetail에서 숫자(강의동 번호)를 추출하여 건물별로 먼저 정렬하고, 그 다음 강의실 번호로 정렬
        val sortedRooms = updatedRooms.sortedWith(
            compareBy<LectureRoom> { room ->
                // "5강의동" -> 5
                room.buildingDetail.filter { it.isDigit() }.toIntOrNull() ?: 999
            }.thenBy {
                // "5104" -> 5104
                it.name.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
            }
        )

        val groupedList = mutableListOf<ListItem>()
        var lastBuildingName = ""
        sortedRooms.forEach { room ->
            if (room.buildingName != lastBuildingName) {
                // 헤더 텍스트 포맷: "덕문관 (5강의동)"
                val headerText = "${room.buildingName} (${room.buildingDetail})"
                groupedList.add(ListItem.HeaderItem(headerText))
                lastBuildingName = room.buildingName
            }
            groupedList.add(ListItem.RoomItem(room))
        }
        
        lectureRoomsWithFavorites.postValue(groupedList)
    }

    fun fetchRooms() {
        if (allRooms.value != null && allRooms.value!!.isNotEmpty()) {
            Log.d("LectureRoomRepo", "Rooms already fetched. Skipping.")
            return
        }
        
        Log.d("LectureRoomRepo", "Fetching rooms from Firestore.")
        db.collection("rooms").get()
            .addOnSuccessListener { result ->
                val roomList = result.mapNotNull { doc ->
                    doc.toObject<LectureRoom>().copy(id = doc.id)
                }
                allRooms.postValue(roomList)
                Log.d("LectureRoomRepo", "Successfully fetched ${roomList.size} rooms.")
            }
            .addOnFailureListener { e ->
                Log.e("LectureRoomRepo", "Error fetching rooms", e)
                allRooms.postValue(emptyList()) // 실패 시 빈 리스트 전달
            }
    }

    fun fetchFavoriteIds() {
        val uid = auth.currentUser?.uid ?: run {
            favoriteRoomIds.postValue(emptyList()) // 로그인하지 않은 사용자는 빈 찜 목록
            return
        }
        // 실시간 업데이트를 위해 addSnapshotListener 사용
        db.collection("User").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("LectureRoomRepo", "Listen failed.", e)
                    favoriteRoomIds.postValue(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // 이제 찜 목록 필드 이름은 'favoriteRooms' 입니다.
                    val ids = snapshot.get("favoriteRooms") as? List<String> ?: emptyList()
                    favoriteRoomIds.postValue(ids)
                    Log.d("LectureRoomRepo", "Favorite IDs updated: $ids")
                } else {
                    Log.d("LectureRoomRepo", "Current data: null")
                    favoriteRoomIds.postValue(emptyList())
                }
            }
    }

    fun toggleFavorite(roomId: String) {
        val uid = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("User").document(uid)

        // isFavorite 값을 확인하여 서버에 추가 또는 삭제 요청
        val isCurrentlyFavorite = favoriteRoomIds.value?.contains(roomId) == true

        if (isCurrentlyFavorite) {
            // 찜 목록에서 제거
            userDocRef.update("favoriteRooms", FieldValue.arrayRemove(roomId))
                .addOnSuccessListener { Log.d("LectureRoomRepo", "Room $roomId removed from favorites.") }
                .addOnFailureListener { e -> Log.e("LectureRoomRepo", "Error removing favorite", e) }
        } else {
            // 찜 목록에 추가
            userDocRef.update("favoriteRooms", FieldValue.arrayUnion(roomId))
                .addOnSuccessListener { Log.d("LectureRoomRepo", "Room $roomId added to favorites.") }
                .addOnFailureListener { e -> Log.e("LectureRoomRepo", "Error adding favorite", e) }
        }
    }
}
