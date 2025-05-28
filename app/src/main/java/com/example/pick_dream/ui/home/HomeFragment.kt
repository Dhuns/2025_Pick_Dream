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
import com.example.pick_dream.model.Reservation
import com.example.pick_dream.ui.home.llm.LlmFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // handler와 timeUpdateRunnable을 선언과 동시에 초기화
    private val handler = Handler()
    private var endMillis: Long = 0L

    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val remainingMillis = endMillis - now
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
                binding.tvReservationRoom.text = "예약 내역이 없습니다."
                binding.tvReservationTime.text = ""
                binding.tvRemainingTime.text = ""
            }
        }
    }

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

    // 현재 예약 정보 불러오기
    private fun loadMyReservation() {
        val db = FirebaseFirestore.getInstance()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d("예약", "현재 UID: $currentUid")

        db.collection("User").document(currentUid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (_binding == null) {
                    Log.e("예약", "바인딩이 null이라 UI를 업데이트할 수 없음 (User 쿼리)")
                    return@addOnSuccessListener
                }
                val studentId = userDoc.getString("studentId")
                Log.d("예약", "가져온 studentId: $studentId")
                if (studentId != null) {
                    val now = Date()
                    val pattern = "yyyy년 M월 d일 a h시 m분 s초"
                    val formatter = SimpleDateFormat(pattern, Locale.KOREA)
                    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val nowString = formatter.format(now)
                    Log.d("예약", "nowString: $nowString")

                    db.collection("Reservations")
                        .whereEqualTo("userID", studentId)
                        .whereGreaterThan("endTime", nowString)
                        .orderBy("endTime")
                        .limit(1)
                        .get()
                        .addOnSuccessListener { result ->
                            if (_binding == null) {
                                return@addOnSuccessListener
                            }
                            Log.d("예약", "예약 쿼리 결과 개수: ${result.size()}")
                            if (!isAdded || isDetached) return@addOnSuccessListener

                            if (result.isEmpty) {
                                Log.d("예약", "예약 결과 없음")
                                _binding?.tvReservationRoom?.text = ""
                                _binding?.tvReservationTime?.text = ""
                                _binding?.tvRemainingTime?.text = "예약 내역이 없습니다."
                                handler.removeCallbacks(timeUpdateRunnable)
                                return@addOnSuccessListener
                            }

                            val doc = result.first()
                            val roomID = doc.getString("roomID") ?: ""
                            val userID = doc.getString("userID") ?: ""
                            val startTimeRaw = doc.getString("startTime") ?: ""
                            val endTimeRaw = doc.getString("endTime") ?: ""

                            Log.d("예약", "roomID: $roomID, userID: $userID")
                            Log.d("예약", "startTime: $startTimeRaw, endTime: $endTimeRaw")

                            // " UTC+9" 문자열 제거
                            val cleanedStartTime = startTimeRaw.replace(" UTC+9", "")
                            val cleanedEndTime = endTimeRaw.replace(" UTC+9", "")

                            val reservation = Reservation(
                                roomID = roomID,
                                userID = userID,
                                startTime = cleanedStartTime,
                                endTime = cleanedEndTime
                            )

                            val startDate: Date? = try { formatter.parse(reservation.startTime) } catch (e: Exception) { null }
                            val endDate: Date? = try { formatter.parse(reservation.endTime) } catch (e: Exception) { null }

                            Log.d("예약", "startDate: $startDate, endDate: $endDate")

                            val nowMillis = System.currentTimeMillis()
                            val startMillis = startDate?.time ?: 0L
                            endMillis = endDate?.time ?: 0L

                            val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)
                            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            val startStr = startDate?.let { timeFormat.format(it) } ?: "-"
                            val endStr = endDate?.let { timeFormat.format(it) } ?: "-"

                            Log.d("예약", "startStr: $startStr, endStr: $endStr")

                            when {
                                nowMillis < startMillis -> {
                                    Log.d("예약", "아직 예약 시간이 아님")
                                    _binding?.tvReservationRoom?.text = "예약 장소 : $roomID"
                                    _binding?.tvReservationTime?.text = "대여 시간 : $startStr - $endStr"
                                    _binding?.tvRemainingTime?.text = "아직 예약 시간이 아닙니다"
                                    handler.removeCallbacks(timeUpdateRunnable)
                                }
                                nowMillis in startMillis until endMillis -> {
                                    Log.d("예약", "예약 시간 진행 중")
                                    _binding?.tvReservationRoom?.text = "예약 장소 : $roomID"
                                    _binding?.tvReservationTime?.text = "대여 시간 : $startStr - $endStr"
                                    handler.removeCallbacks(timeUpdateRunnable)
                                    handler.post(timeUpdateRunnable)
                                }
                                else -> {
                                    Log.d("예약", "예약 시간 종료")
                                    _binding?.tvReservationRoom?.text = ""
                                    _binding?.tvReservationTime?.text = ""
                                    _binding?.tvRemainingTime?.text = "예약 내역이 없습니다"
                                    handler.removeCallbacks(timeUpdateRunnable)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            if (_binding == null) {
                                return@addOnFailureListener
                            }
                            Log.e("예약", "예약 쿼리 실패: ${e.message}")
                            _binding?.tvReservationRoom?.text = "예약 정보를 불러오지 못했습니다."
                            _binding?.tvReservationTime?.text = ""
                            _binding?.tvRemainingTime?.text = ""
                            handler.removeCallbacks(timeUpdateRunnable)
                        }
                } else {
                    if (_binding == null) {
                        return@addOnSuccessListener
                    }
                    Log.d("예약", "studentId 없음")
                    _binding?.tvReservationRoom?.text = "학번 정보가 없습니다."
                    _binding?.tvReservationTime?.text = ""
                    _binding?.tvRemainingTime?.text = ""
                    handler.removeCallbacks(timeUpdateRunnable)
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) {
                    return@addOnFailureListener
                }
                Log.e("예약", "User 정보 쿼리 실패: ${e.message}")
                _binding?.tvReservationRoom?.text = "사용자 정보를 불러오지 못했습니다."
                _binding?.tvReservationTime?.text = ""
                _binding?.tvRemainingTime?.text = ""
                handler.removeCallbacks(timeUpdateRunnable)
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
