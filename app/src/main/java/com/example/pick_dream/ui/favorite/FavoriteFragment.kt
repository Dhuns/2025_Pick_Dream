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
import com.example.pick_dream.ui.home.search.LectureRoom
import com.example.pick_dream.ui.home.search.SectionedItem
import com.example.pick_dream.ui.home.search.LectureRoomRepository
import com.example.pick_dream.ui.home.search.LectureRoomAdapter
import com.example.pick_dream.model.Room
import androidx.navigation.fragment.findNavController

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
        fun refreshFavorites() {
            val favoriteLectureRooms = LectureRoomRepository.getFavorites()
            val favoriteRooms = favoriteLectureRooms.map {
                Room(
                    capacity = 0,
                    equiment = it.roomInfo.split(", "),
                    location = it.name,
                    id = "${it.buildingName} (${it.buildingDetail})"
                )
            }
            if (favoriteRooms.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
            adapter = FavoriteRoomsAdapter(favoriteRooms, { room ->
                LectureRoomRepository.toggleFavorite(room.location)
                refreshFavorites()
            }, { room ->
                val buildingName = room.id.substringBefore(" (")
                val buildingDetail = room.id.substringAfter("(").substringBefore(")")
                val bundle = Bundle().apply {
                    putString("roomName", room.location)
                    putString("building", room.id)
                    putString("buildingName", buildingName)
                    putString("buildingDetail", buildingDetail)
                }
                findNavController().navigate(R.id.lectureRoomDetailFragment, bundle)
            })
            recyclerView.adapter = adapter
        }
        refreshFavorites()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}
