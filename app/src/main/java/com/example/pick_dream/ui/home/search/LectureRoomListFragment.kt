package com.example.pick_dream.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.ui.home.search.LectureRoom
import com.example.pick_dream.ui.home.search.LectureRoomAdapter
import com.example.pick_dream.ui.home.search.LectureRoomListFragmentDirections
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context

class LectureRoomListFragment : Fragment() {
    private lateinit var adapter: LectureRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lecture_room_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LectureRoomRepository.fetchRoomsFromFirebase()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvLectureRooms)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnSearch = view.findViewById<View>(R.id.btnSearch)
        val btnFilterBeam = view.findViewById<View>(R.id.btnFilterBeam)
        val btnFilterMic = view.findViewById<View>(R.id.btnFilterMic)
        val btnFilterOutlet = view.findViewById<View>(R.id.btnFilterOutlet)
        val btnFilterScreen = view.findViewById<View>(R.id.btnFilterScreen)

        // 예시 데이터 (Repository에서 가져옴)
        val groupedRooms = LectureRoomRepository.roomsLiveData.value
            ?.groupBy { Pair(it.buildingName, it.buildingDetail) }
            ?.map { (buildingPair, rooms) -> buildingPair to rooms }
            ?: emptyList()
        var currentSectionedList = buildSectionedList(groupedRooms)

        adapter = LectureRoomAdapter(currentSectionedList, { room ->
            val action = LectureRoomListFragmentDirections.actionLectureRoomListFragmentToLectureRoomDetailFragment(
                room.name,
                "${room.buildingName} (${room.buildingDetail})",
                room.buildingName,
                room.buildingDetail
            )
            findNavController().navigate(action)
        }, { refreshList() })
        recyclerView.adapter = adapter

        // 검색 동작 구현
        etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = etSearch.text.toString().trim()
                filterAndShow(query, groupedRooms)
                // 키보드 내리기
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
            val query = etSearch.text.toString().trim()
            filterAndShow(query, groupedRooms)
            // 키보드 내리기
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        }

        btnFilterBeam.setOnClickListener {
            etSearch.setText("빔프로젝터")
            filterAndShow("빔프로젝터", groupedRooms)
        }
        btnFilterMic.setOnClickListener {
            etSearch.setText("마이크")
            filterAndShow("마이크", groupedRooms)
        }
        btnFilterOutlet.setOnClickListener {
            etSearch.setText("콘센트")
            filterAndShow("콘센트", groupedRooms)
        }
        btnFilterScreen.setOnClickListener {
            etSearch.setText("스크린")
            filterAndShow("스크린", groupedRooms)
        }

        // 하단 네비게이션 바 숨기기
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    // 검색어에 따라 섹션별로 필터링된 리스트를 만들어 어댑터에 반영
    private fun filterAndShow(query: String, groupedRooms: List<Pair<Pair<String, String>, List<LectureRoom>>>) {
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
        adapter = LectureRoomAdapter(sectionedList, { room ->
            val action = LectureRoomListFragmentDirections.actionLectureRoomListFragmentToLectureRoomDetailFragment(
                room.name,
                "${room.buildingName} (${room.buildingDetail})",
                room.buildingName,
                room.buildingDetail
            )
            findNavController().navigate(action)
        }, { refreshList() })
        view?.findViewById<RecyclerView>(R.id.rvLectureRooms)?.adapter = adapter
    }

    // 섹션 리스트 생성 함수
    private fun buildSectionedList(groupedRooms: List<Pair<Pair<String, String>, List<LectureRoom>>>): MutableList<SectionedItem> {
        val sectionedList = mutableListOf<SectionedItem>()
        for ((buildingPair, rooms) in groupedRooms) {
            val (buildingName, buildingDetail) = buildingPair
            sectionedList.add(SectionedItem.Header(buildingName, buildingDetail))
            sectionedList.addAll(rooms.map { SectionedItem.Room(it) })
        }
        return sectionedList
    }

    fun refreshList() {
        val groupedRooms = LectureRoomRepository.roomsLiveData.value
            ?.groupBy { Pair(it.buildingName, it.buildingDetail) }
            ?.map { (buildingPair, rooms) -> buildingPair to rooms }
            ?: emptyList()
        val sectionedList = buildSectionedList(groupedRooms)
        adapter = LectureRoomAdapter(sectionedList, { room ->
            val action = LectureRoomListFragmentDirections.actionLectureRoomListFragmentToLectureRoomDetailFragment(
                room.name,
                "${room.buildingName} (${room.buildingDetail})",
                room.buildingName,
                room.buildingDetail
            )
            findNavController().navigate(action)
        }, { refreshList() })
        view?.findViewById<RecyclerView>(R.id.rvLectureRooms)?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트가 사라질 때 다시 보이게
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}