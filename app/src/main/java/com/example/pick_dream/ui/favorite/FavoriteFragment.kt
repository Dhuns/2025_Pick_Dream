package com.example.pick_dream.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rvFavoriteRooms)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoriteRoomsAdapter(listOf())
        recyclerView.adapter = adapter
        loadFavoriteRooms()
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
                    return@addOnSuccessListener
                }
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
            }
    }
}
