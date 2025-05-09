package com.example.pick_dream.ui.home.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.pick_dream.R

class ReservationDetailBottomSheet : BottomSheetDialogFragment() {
    private lateinit var tvRoom: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvPeople: TextView
    private lateinit var tvFacilities: TextView
    private lateinit var tvReserver: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        tvRoom = view.findViewById(R.id.tv_room)
        tvDate = view.findViewById(R.id.tv_date)
        tvTime = view.findViewById(R.id.tv_time)
        tvPeople = view.findViewById(R.id.tv_people)
        tvFacilities = view.findViewById(R.id.tv_facilities)
        tvReserver = view.findViewById(R.id.tv_reserver)

        // 전달받은 데이터 표시
        arguments?.let { bundle ->
            tvRoom.text = "강의실 : ${bundle.getString(KEY_ROOM)}"
            tvDate.text = "예약일 : ${bundle.getString(KEY_DATE)}"
            tvTime.text = "시간 : ${bundle.getString(KEY_TIME)}"
            tvPeople.text = "인원 : 총 ${bundle.getString(KEY_PEOPLE)}명"
            tvFacilities.text = "주요 시설 : ${bundle.getString(KEY_FACILITIES)}"
            tvReserver.text = "예약자 : ${bundle.getString(KEY_RESERVER)}"
        }
    }

    companion object {
        private const val KEY_ROOM = "room"
        private const val KEY_DATE = "date"
        private const val KEY_TIME = "time"
        private const val KEY_PEOPLE = "people"
        private const val KEY_FACILITIES = "facilities"
        private const val KEY_RESERVER = "reserver"

        fun newInstance(
            room: String,
            date: String,
            time: String,
            people: String,
            facilities: String,
            reserver: String
        ): ReservationDetailBottomSheet {
            return ReservationDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(KEY_ROOM, room)
                    putString(KEY_DATE, date)
                    putString(KEY_TIME, time)
                    putString(KEY_PEOPLE, people)
                    putString(KEY_FACILITIES, facilities)
                    putString(KEY_RESERVER, reserver)
                }
            }
        }
    }
} 