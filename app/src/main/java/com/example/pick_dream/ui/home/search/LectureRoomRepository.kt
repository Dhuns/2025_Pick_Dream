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
    private val buildingInfoMap = MutableLiveData<Map<String, BuildingInfo>>()

    data class BuildingInfo(
        val displayName: String = "",
        val sortOrder: Int = 999
    )

    // 최종적으로 UI에 보여줄 LiveData. allRooms나 favoriteRoomIds가 변경되면 자동으로 업데이트됨
    val lectureRoomsWithFavorites = MediatorLiveData<List<ListItem>>()

    init {
        // 여러 LiveData를 관찰하고, 변경이 있을 때마다 데이터를 조합하는 로직
        lectureRoomsWithFavorites.addSource(allRooms) { rooms ->
            combineData(rooms, favoriteRoomIds.value, buildingInfoMap.value)
        }
        lectureRoomsWithFavorites.addSource(favoriteRoomIds) { ids ->
            combineData(allRooms.value, ids, buildingInfoMap.value)
        }
        lectureRoomsWithFavorites.addSource(buildingInfoMap) { buildingInfo ->
            combineData(allRooms.value, favoriteRoomIds.value, buildingInfo)
        }
    }

    private fun combineData(
        rooms: List<LectureRoom>?,
        ids: List<String>?,
        buildingInfo: Map<String, BuildingInfo>?
    ) {
        if (rooms == null || ids == null || buildingInfo == null) {
            return
        }

        val defaultSortOrder = buildingInfo.size + 1

        val updatedRooms = rooms.map { room ->
            room.copy(isFavorite = ids.contains(room.id))
        }
        
        val sortedRooms = updatedRooms.sortedWith(
            compareBy(
                { buildingInfo[it.buildingName.trim()]?.sortOrder ?: defaultSortOrder }, 
                { it.name.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 }
            )
        )
        
        val groupedList = mutableListOf<ListItem>()
        sortedRooms.groupBy { it.buildingName }.forEach { (buildingName, roomsInBuilding) ->
            val headerText = buildingInfo[buildingName.trim()]?.displayName ?: buildingName
            groupedList.add(ListItem.HeaderItem(headerText))
            roomsInBuilding.forEach { room ->
                groupedList.add(ListItem.RoomItem(room))
            }
        }
        
        lectureRoomsWithFavorites.postValue(groupedList)
    }

    fun fetchRooms() {
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

    fun fetchBuildingInfo() {
        db.collection("buildings").get()
            .addOnSuccessListener { result ->
                val infoMap = result.documents.associate { doc ->
                    val info = doc.toObject<BuildingInfo>() ?: BuildingInfo()
                    doc.id to info
                }
                buildingInfoMap.postValue(infoMap)
                Log.d("LectureRoomRepo", "Successfully fetched ${infoMap.size} building info.")
            }
            .addOnFailureListener { e ->
                Log.e("LectureRoomRepo", "Error fetching building info", e)
                buildingInfoMap.postValue(emptyMap())
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
