package com.example.pick_dream.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.R
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.Observer

class LectureRoomDetailFragment : Fragment() {
    private val viewModel: LectureRoomDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lecture_room_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomName = arguments?.getString("roomName") ?: ""
        val buildingDetail = arguments?.getString("building")

        val ivLectureRoom = view.findViewById<ImageView>(R.id.ivLectureRoom)
        val tvRoomName = view.findViewById<TextView>(R.id.tvRoomName)
        val tvRoomDesc = view.findViewById<TextView>(R.id.tvRoomDesc)
        val btnFavorite = view.findViewById<ImageButton>(R.id.btnFavorite)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnReserve = view.findViewById<MaterialButton>(R.id.btnReserve)
        val infoBox = view.findViewById<ViewGroup>(R.id.infoBox)
        val infoTextViews = listOf<TextView>(
            infoBox.getChildAt(0) as TextView,
            infoBox.getChildAt(1) as TextView,
            infoBox.getChildAt(2) as TextView,
            infoBox.getChildAt(3) as TextView,
            infoBox.getChildAt(4) as TextView,
            infoBox.getChildAt(5) as TextView,
            infoBox.getChildAt(6) as TextView
        )

        // LiveData 관찰로 강의실 정보 전체를 갱신
        LectureRoomRepository.roomsLiveData.observe(viewLifecycleOwner) { rooms ->
            val updatedRoom = rooms.find { it.name == roomName }
            if (updatedRoom != null) {
                tvRoomName.text = "${updatedRoom.buildingName} (${updatedRoom.buildingDetail})"
                tvRoomDesc.text = updatedRoom.roomInfo
                // 필요하다면 ivLectureRoom.setImageResource 등도 추가
                btnFavorite.setImageResource(
                    if (updatedRoom.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
                )
                // infoTextViews 등 추가 정보도 updatedRoom에서 갱신
            }
        }
        btnFavorite.setOnClickListener {
            LectureRoomRepository.toggleFavorite(roomName)
            // UI 갱신은 LiveData 관찰로만 처리
        }

        // 뒤로가기
        btnBack.setOnClickListener {
            findNavController().navigate(R.id.lectureRoomListFragment)
        }

        // 대여하기 버튼 클릭
        btnReserve.setOnClickListener {
            Toast.makeText(requireContext(), "예약 화면으로 이동!", Toast.LENGTH_SHORT).show()
            // 실제 예약 화면 이동 로직 추가
        }

        // 예시: arguments로 roomId를 받아 ViewModel에 전달
        val roomId = arguments?.getString("roomId")
        if (roomId != null) {
            viewModel.loadRoomDetail(roomId)
        }
    }
}