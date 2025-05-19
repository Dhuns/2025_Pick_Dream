package com.example.pick_dream.ui.home.search.manualReservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pick_dream.R
import android.widget.Button
import androidx.navigation.fragment.findNavController

class ManualReservationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvBuilding = view.findViewById<TextView>(R.id.tvBuilding)
        val tvRoomName = view.findViewById<TextView>(R.id.tvRoomName)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val btnBack = view.findViewById<View>(R.id.btnBack)

        // 예시: arguments로 강의실 정보 전달받아 표시
        val building = arguments?.getString("building") ?: ""
        val roomName = arguments?.getString("roomName") ?: ""
        tvBuilding.text = building
        tvRoomName.text = roomName

        btnNext.setOnClickListener {
            // 다음 단계로 이동 (구현 필요)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
} 