package com.example.pick_dream.ui.home

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentHomeBinding
import com.example.pick_dream.ui.home.reservation.ReservationFragment
import com.example.pick_dream.ui.home.notice.NoticeFragment
import android.content.Context
import android.util.Log
import com.example.pick_dream.ui.home.llm.LlmFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pick_dream.model.Reservation
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var timeUpdateRunnable: Runnable
    private var remainingSeconds = 4860

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNotice.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_noticeFragment)
        }
        initViews()
        setupClickListeners()
        loadMyReservation()
    }
    // 현재 예약 정보 불러오기
    private fun loadMyReservation() {
        val db = FirebaseFirestore.getInstance()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("User").document(currentUid)
            .get()
            .addOnSuccessListener { userDoc ->
                val studentId = userDoc.getString("studentId")
                if (studentId != null) {
                    db.collection("Reservations")
                        .whereEqualTo("userID", studentId)
                        .whereGreaterThan("endTime", Timestamp.now())
                        .orderBy("endTime")
                        .limit(1)
                        .get()
                        .addOnSuccessListener { result ->
                            if (result.isEmpty) {
                                binding.tvReservationRoom.text = "예약 내역이 없습니다."
                                binding.tvReservationTime.text = ""
                                binding.tvRemainingTime.text = ""
                                return@addOnSuccessListener
                            }

                            val doc = result.first()
                            val roomID = doc.getString("roomID") ?: ""
                            val userID = doc.getString("userID") ?: ""
                            val startTime = doc.getTimestamp("startTime")
                            val endTime = doc.getTimestamp("endTime")

                            val reservation = Reservation(
                                roomID = roomID,
                                userID = userID,
                                startTime = startTime,
                                endTime = endTime
                            )

                            val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)
                            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            val start = reservation.startTime?.toDate()
                            val end = reservation.endTime?.toDate()

                            val nowMillis = System.currentTimeMillis()
                            val startMillis = start?.time ?: 0L
                            val endMillis = end?.time ?: 0L

                            val startStr = start?.let { timeFormat.format(it) } ?: "-"
                            val endStr = end?.let { timeFormat.format(it) } ?: "-"

                            if (nowMillis < startMillis) {
                                // 다음 예약만 보여주기
                                binding.tvReservationRoom.text = "예약 장소 : $roomID"
                                binding.tvReservationTime.text = "대여 시간 : $startStr - $endStr"
                                binding.tvRemainingTime.text = "아직 예약 시간이 아닙니다"
                                return@addOnSuccessListener
                            }

                            // 예약 시간이면 남은 시간 카운트다운 표시
                            binding.tvReservationRoom.text = "예약 장소 : $roomID"
                            binding.tvReservationTime.text = "대여 시간 : $startStr - $endStr"

                            // 남은 시간 타이머
                            end?.let { endDate ->
                                val handler = Handler()
                                val runnable = object : Runnable {
                                    override fun run() {
                                        val now = System.currentTimeMillis()
                                        val remainingMillis = endDate.time - now
                                        if (remainingMillis > 0) {
                                            val minutes = (remainingMillis / 1000 / 60) % 60
                                            val hours = (remainingMillis / 1000 / 60 / 60)
                                            val seconds = (remainingMillis / 1000) % 60
                                            binding.tvRemainingTime.text = if (hours > 0) {
                                                "남은 시간: ${hours}시간 ${minutes}분"
                                            } else {
                                                "남은 시간: ${minutes}분 ${seconds}초"
                                            }
                                            handler.postDelayed(this, 1000)
                                        } else {
                                            binding.tvRemainingTime.text = "대여 시간 종료"
                                        }
                                    }
                                }
                                handler.post(runnable)
                            }
                        }
                        .addOnFailureListener {
                            binding.tvReservationRoom.text = "예약 정보를 불러오지 못했습니다."
                            binding.tvReservationTime.text = ""
                            binding.tvRemainingTime.text = ""
                        }
                } else {
                    binding.tvReservationRoom.text = "학번 정보가 없습니다."
                    binding.tvReservationTime.text = ""
                    binding.tvRemainingTime.text = ""
                }
            }
            .addOnFailureListener {
                binding.tvReservationRoom.text = "사용자 정보를 불러오지 못했습니다."
                binding.tvReservationTime.text = ""
                binding.tvRemainingTime.text = ""
            }
    }



    private fun initViews() {
        // 필요 시 여기에서 초기 설정 가능
    }

    private fun setupClickListeners() {
        listOf(binding.btnLlm, binding.btnSearch, binding.btnInquiry, binding.btnMap).forEach { button ->
            button.setOnClickListener {
                onButtonClick(it)
            }
        }
        // 공지사항 전체보기 버튼 클릭 시 NoticeFragment로 이동 (Navigation Component 사용)
//        binding.btnNotice.setOnClickListener {
//            findNavController().navigate(R.id.noticeFragment)
//        }
    }

    private fun onButtonClick(view: View) {
        val context = requireContext()
        val originalColor = ContextCompat.getColor(context, R.color.button_normal)
        val clickedColor = ContextCompat.getColor(context, R.color.button_clicked)

        view.setBackgroundColor(clickedColor)

        Handler().postDelayed({
            view.setBackgroundColor(originalColor)
        }, 200)

        when (view.id) {
            R.id.btn_llm -> {
                // LLM Fragment로 이동
                findNavController().navigate(R.id.action_homeFragment_to_llmFragment)
            }
            R.id.btn_search -> {
                // 검색/대여 강의실 조회 페이지로 이동
                findNavController().navigate(R.id.action_homeFragment_to_lectureRoomListFragment)
            }
            R.id.btn_inquiry -> {
                findNavController().navigate(R.id.action_homeFragment_to_reservationFragment)
            }
            R.id.btn_map -> {
                findNavController().navigate(R.id.action_homeFragment_to_mapsFragment)
                // 지도 기능 실행
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeUpdateRunnable)
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.visibility = View.VISIBLE
        if (navView?.selectedItemId != R.id.navigation_home) {
            navView?.selectedItemId = R.id.navigation_home
        }
    }
}
