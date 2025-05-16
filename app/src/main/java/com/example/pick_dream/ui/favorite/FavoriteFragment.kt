package com.example.pick_dream.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.Room
import com.example.pick_dream.ui.favorite.FavoriteRoomsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoriteRoomsAdapter
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rvFavoriteRooms)
        emptyView = view.findViewById(R.id.tvEmpty)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoriteRoomsAdapter(listOf()) { room ->
            // 하트 버튼 클릭 시 호출되는 콜백
            removeFromWishlist(room)
        }
        recyclerView.adapter = adapter
        loadFavoriteRooms()
    }

    private fun removeFromWishlist(room: Room) {
        val db = FirebaseFirestore.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("User").document(userID)
            .collection("wishlists")
            .document(room.id)
            .delete()
            .addOnSuccessListener {
                // 파이어베이스에서 삭제 성공
                adapter.removeRoom(room)
                
                // 모든 강의실이 삭제되었다면 빈 화면 메시지 표시
                if (adapter.itemCount == 0) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                
                Toast.makeText(context, "찜한 강의실에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFavoriteRooms() {
        val db = FirebaseFirestore.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. wishlists에서 roomId(강의실 번호) 리스트 가져오기
        db.collection("User").document(userID).collection("wishlists")
            .get()
            .addOnSuccessListener { result ->
                val roomNumbers = result.documents.map { it.id }
                if (roomNumbers.isEmpty()) {
                    adapter.updateRooms(listOf())
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    return@addOnSuccessListener
                }
                
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                
                val rooms = mutableListOf<Room>()
                var completed = 0
                // 2. rooms 컬렉션에서 각 강의실 정보 가져오기
                for (roomNumber in roomNumbers) {
                    db.collection("rooms").document(roomNumber)
                        .get()
                        .addOnSuccessListener { doc ->
                            doc.toObject(Room::class.java)?.let { room ->
                                val roomWithId = room.copy(id = doc.id)
                                rooms.add(roomWithId)
                            }
                            completed++
                            if (completed == roomNumbers.size) {
                                adapter.updateRooms(rooms)
                            }
                        }
                        .addOnFailureListener {
                            completed++
                            if (completed == roomNumbers.size) {
                                adapter.updateRooms(rooms)
                            }
                        }
                }
            }
            .addOnFailureListener {
                adapter.updateRooms(listOf())
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
    }
}
