package com.example.pick_dream.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.data.model.Room
import com.example.pick_dream.ui.adapter.FavoriteRoomsAdapter

class FavoriteRoomsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoriteRoomsAdapter

    private val sampleRooms = listOf(
        Room(R.drawable.sample_room, "덕문관 (5강의동)", "5022 강의실", "빔프로젝터, 마이크, 콘센트, 스크린"),
        Room(R.drawable.sample_room, "덕문관 (5강의동)", "4103 강의실", "빔프로젝터, 마이크, 스크린")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_favorite_rooms.xml 파일을 inflate 합니다.
        return inflater.inflate(R.layout.fragment_favorite_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rvFavoriteRooms)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoriteRoomsAdapter(sampleRooms)
        recyclerView.adapter = adapter
    }
}
