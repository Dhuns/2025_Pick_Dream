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
import com.example.pick_dream.model.Room
import androidx.navigation.fragment.findNavController
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

        LectureRoomRepository.wishlistsLiveData.observe(viewLifecycleOwner) { wishlists ->
            refreshFavorites()
        }

        LectureRoomRepository.roomsLiveData.observe(viewLifecycleOwner) { rooms ->
            refreshFavorites()
        }

        LectureRoomRepository.fetchRoomsFromFirebase()
    }

    private fun refreshFavorites() {
        val favoriteRooms = LectureRoomRepository.getFavorites().map { lectureRoom ->
            Room(
                capacity = lectureRoom.capacity,
                equipment = lectureRoom.equipment,
                location = lectureRoom.location,
                id = "${lectureRoom.buildingName} (${lectureRoom.buildingDetail})",
                name = lectureRoom.name,
                buildingName = lectureRoom.buildingName,
                buildingDetail = lectureRoom.buildingDetail,
                isFavorite = true
            )
        }

        if (favoriteRooms.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            adapter = FavoriteRoomsAdapter(
                favoriteRooms,
                { room ->
                    LectureRoomRepository.toggleFavorite(room.name)
                },
                { room ->
                    val bundle = Bundle().apply {
                        putString("roomName", room.name)
                        putString("buildingName", room.buildingName)
                        putString("buildingDetail", room.buildingDetail)
                    }
                    findNavController().navigate(R.id.lectureRoomDetailFragment, bundle)
                },
                { room ->
                    val bundle = Bundle().apply {
                        putString("building", room.id)
                        putString("roomName", room.name)
                    }
                    findNavController().navigate(R.id.manualReservationFragment, bundle)
                }
            )
            recyclerView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.visibility = View.VISIBLE
        if (navView?.selectedItemId != R.id.navigation_favorite) {
            navView?.selectedItemId = R.id.navigation_favorite
        }
        LectureRoomRepository.fetchWishlists()
    }
}
