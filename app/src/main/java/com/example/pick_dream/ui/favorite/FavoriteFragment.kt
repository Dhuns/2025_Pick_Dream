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

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LectureRoomAdapter
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
        val favoriteRooms = LectureRoomRepository.getFavorites()
        if (favoriteRooms.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        adapter = LectureRoomAdapter(
            favoriteRooms.map { SectionedItem.Room(it) },
            onItemClick = { /* 상세 이동 등 필요시 구현 */ },
            onFavoriteChanged = { refreshFavorites() }
        )
        recyclerView.adapter = adapter
    }
    refreshFavorites()
}
