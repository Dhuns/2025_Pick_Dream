package com.example.pick_dream.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.ui.home.search.LectureRoomRepository
import com.example.pick_dream.model.LectureRoom
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.ui.home.search.ListItem
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        adapter = FavoriteRoomsAdapter(emptyList(),
            onFavoriteClick = { room ->
                LectureRoomRepository.toggleFavorite(room.id)
            },
            onDetailClick = { room ->
                val action = FavoriteFragmentDirections.actionNavigationFavoriteToLectureRoomDetailFragment(
                    roomName = room.name,
                    buildingName = room.buildingName,
                    buildingDetail = room.buildingDetail,
                    building = "${room.buildingName} (${room.buildingDetail})"
                )
                findNavController().navigate(action)
            },
            onReserveClick = { room ->
                val action = FavoriteFragmentDirections.actionNavigationFavoriteToManualReservationFragment(
                    building = "${room.buildingName} (${room.buildingDetail})",
                    roomName = room.name
                )
                findNavController().navigate(action)
            }
        )
        recyclerView.adapter = adapter

        observeFavoriteRooms()
    }
    
    override fun onResume() {
        super.onResume()
        // Repository에서 실시간으로 데이터를 관찰하므로 onResume에서 수동으로 fetch할 필요가 없습니다.
        // LectureRoomRepository.fetchRooms()
        // LectureRoomRepository.fetchFavoriteIds()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.visibility = View.VISIBLE
        if (navView?.selectedItemId != R.id.navigation_favorite) {
            navView?.selectedItemId = R.id.navigation_favorite
        }
    }

    private fun observeFavoriteRooms() {
        LectureRoomRepository.lectureRoomsWithFavorites.observe(viewLifecycleOwner) { allItems ->
            val favoriteRooms = allItems.mapNotNull { item ->
                if (item is ListItem.RoomItem && item.lectureRoom.isFavorite) {
                    item.lectureRoom
                } else {
                    null
                }
            }
            adapter.updateRooms(favoriteRooms)

            if (favoriteRooms.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
}
