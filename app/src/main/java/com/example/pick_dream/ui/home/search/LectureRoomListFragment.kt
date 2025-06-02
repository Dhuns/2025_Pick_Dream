package com.example.pick_dream.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context

class LectureRoomListFragment : Fragment() {
    private lateinit var adapter: LectureRoomAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lecture_room_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LectureRoomRepository.fetchRoomsFromFirebase()

        recyclerView = view.findViewById<RecyclerView>(R.id.rvLectureRooms)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnSearch = view.findViewById<View>(R.id.btnSearch)
        val btnFilterBeam = view.findViewById<View>(R.id.btnFilterBeam)
        val btnFilterMic = view.findViewById<View>(R.id.btnFilterMic)
        val btnFilterOutlet = view.findViewById<View>(R.id.btnFilterOutlet)
        val btnFilterScreen = view.findViewById<View>(R.id.btnFilterScreen)

        setupAdapter()

        etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                currentQuery = etSearch.text.toString().trim()
                filterAndShow(currentQuery)
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }

        val btnBack = view.findViewById<View>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        btnSearch.setOnClickListener {
            currentQuery = etSearch.text.toString().trim()
            filterAndShow(currentQuery)
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        }

        btnFilterBeam.setOnClickListener {
            etSearch.setText("빔프로젝터")
            currentQuery = "빔프로젝터"
            filterAndShow(currentQuery)
        }
        btnFilterMic.setOnClickListener {
            etSearch.setText("마이크")
            currentQuery = "마이크"
            filterAndShow(currentQuery)
        }
        btnFilterOutlet.setOnClickListener {
            etSearch.setText("콘센트")
            currentQuery = "콘센트"
            filterAndShow(currentQuery)
        }
        btnFilterScreen.setOnClickListener {
            etSearch.setText("스크린")
            currentQuery = "스크린"
            filterAndShow(currentQuery)
        }

        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    private fun setupAdapter() {
        val groupedRooms = LectureRoomRepository.roomsLiveData.value
            ?.groupBy { Pair(it.buildingName, it.buildingDetail) }
            ?.map { (buildingPair, rooms) -> buildingPair to rooms }
            ?: emptyList()
        val sectionedList = buildSectionedList(groupedRooms)
        
        adapter = LectureRoomAdapter(sectionedList, 
            onItemClick = { room ->
                val action = LectureRoomListFragmentDirections
                    .actionLectureRoomListFragmentToLectureRoomDetailFragment(
                        room.name,
                        "${room.buildingName} (${room.buildingDetail})",
                        room.buildingName,
                        room.buildingDetail
                    )
                findNavController().navigate(action)
            },
            onFavoriteChanged = {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val position = layoutManager.findFirstVisibleItemPosition()
                val offset = recyclerView.getChildAt(0)?.top ?: 0

                filterAndShow(currentQuery)

                layoutManager.scrollToPositionWithOffset(position, offset)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun filterAndShow(query: String) {
        val filtered = LectureRoomRepository.roomsLiveData.value
            ?.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.buildingName.contains(query, ignoreCase = true) ||
                it.buildingDetail.contains(query, ignoreCase = true) ||
                it.equipment.any { equip -> equip.contains(query, ignoreCase = true) }
            }
            ?.groupBy { Pair(it.buildingName, it.buildingDetail) }
            ?.map { (buildingPair, rooms) -> buildingPair to rooms }
            ?: emptyList()
            
        val sectionedList = buildSectionedList(filtered)
        adapter.updateList(sectionedList)
    }

    private fun buildSectionedList(groupedRooms: List<Pair<Pair<String, String>, List<LectureRoom>>>): MutableList<SectionedItem> {
        val sectionedList = mutableListOf<SectionedItem>()
        for ((buildingPair, rooms) in groupedRooms) {
            val (buildingName, buildingDetail) = buildingPair
            sectionedList.add(SectionedItem.Header(buildingName, buildingDetail))
            sectionedList.addAll(rooms.map { SectionedItem.Room(it) })
        }
        return sectionedList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}