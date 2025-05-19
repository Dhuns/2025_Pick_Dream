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
import android.util.Log

class LectureRoomDetailFragment : Fragment() {
    private val viewModel: LectureRoomDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lecture_room_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE

        val roomName = arguments?.getString("roomName") ?: ""
        val buildingName = arguments?.getString("buildingName") ?: ""
        val buildingDetail = arguments?.getString("buildingDetail") ?: ""
        Log.d("DEBUG", "roomName: '$roomName', buildingName: '$buildingName', buildingDetail: '$buildingDetail'")

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
            rooms.forEach {
                Log.d("DEBUG", "LectureRoom: name='${it.name}', buildingName='${it.buildingName}', buildingDetail='${it.buildingDetail}'")
            }
            val updatedRoom = rooms.find {
                it.name.trim() == roomName.trim() &&
                it.buildingName.trim() == buildingName.trim() &&
                it.buildingDetail.trim() == buildingDetail.trim()
            } ?: rooms.find { it.name.trim() == roomName.trim() }
            if (updatedRoom != null) {
                Log.d("DEBUG", "updatedRoom: name='${updatedRoom.name}', buildingName='${updatedRoom.buildingName}', buildingDetail='${updatedRoom.buildingDetail}'")
                tvRoomName.text = "${updatedRoom.buildingName} (${updatedRoom.buildingDetail})"
                tvRoomDesc.text = updatedRoom.roomInfo
                btnFavorite.setImageResource(
                    if (updatedRoom.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
                )
                // infoBox 세팅
                infoTextViews[0].text = "강의실 : ${updatedRoom.name}"
                infoTextViews[1].text = "기자재 목록 : ${updatedRoom.roomInfo}"
                infoTextViews[2].text = "의자 : 정보 없음"
                infoTextViews[3].text = "빔 프로젝터 대여 여부 : 정보 없음"
                infoTextViews[4].text = "전자 칠판 대여 여부 : 정보 없음"
                infoTextViews[5].text = "장소 대여 여부 : 정보 없음"
                infoTextViews[6].text = "앱에서 바로 예약 가능"
            } else {
                Log.d("DEBUG", "updatedRoom is null!")
            }
        }
        btnFavorite.setOnClickListener {
            LectureRoomRepository.toggleFavorite(roomName)
            // UI 갱신은 LiveData 관찰로만 처리
        }

        // 뒤로가기
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 대여하기 버튼 클릭
        btnReserve.setOnClickListener {
            // updatedRoom이 null이 아닐 때만 이동
            LectureRoomRepository.roomsLiveData.value?.let { rooms ->
                val matchedRoom = rooms.find {
                    it.name.trim() == roomName.trim() &&
                    it.buildingName.trim() == buildingName.trim() &&
                    it.buildingDetail.trim() == buildingDetail.trim()
                } ?: rooms.find { it.name.trim() == roomName.trim() }
                if (matchedRoom != null) {
                    val bundle = Bundle().apply {
                        putString("building", "${matchedRoom.buildingName} (${matchedRoom.buildingDetail})")
                        putString("roomName", matchedRoom.name)
                    }
                    findNavController().navigate(R.id.manualReservationFragment, bundle)
                }
            }
        }

        // 예시: arguments로 roomId를 받아 ViewModel에 전달
        val roomId = arguments?.getString("roomId")
        if (roomId != null) {
            viewModel.loadRoomDetail(roomId)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }
}