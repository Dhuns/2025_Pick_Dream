package com.example.pick_dream.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentHomeBinding
import com.example.pick_dream.model.Reservation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.content.Context

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

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

        // 데이터 로딩 전, 예약 정보 관련 뷰들을 미리 숨김
        binding.layoutReservationDetails.visibility = View.INVISIBLE
        binding.layoutNoReservation.visibility = View.INVISIBLE
        binding.flReservationStatusVisual.visibility = View.INVISIBLE

        initViews()
        setupClickListeners()
        // loadMyReservation() // onResume에서 호출되므로 중복 호출 방지
    }

    private fun initViews() {
    }

    private fun setupClickListeners() {
        listOf(binding.btnLlm, binding.btnSearch, binding.btnInquiry, binding.btnMap).forEach { button ->
            button.setOnClickListener {
                onButtonClick(it)
            }
        }
        binding.btnNotice.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_noticeFragment)
        }
        binding.cardReservationInfo.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_reservationFragment)
        }
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
                findNavController().navigate(R.id.action_homeFragment_to_llmFragment)
            }
            R.id.btn_search -> {
                findNavController().navigate(R.id.action_homeFragment_to_lectureRoomListFragment)
            }
            R.id.btn_inquiry -> {
                findNavController().navigate(R.id.action_homeFragment_to_reservationFragment)
            }
            R.id.btn_map -> {
                findNavController().navigate(R.id.action_homeFragment_to_mapsFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.visibility = View.VISIBLE
        if (navView?.selectedItemId != R.id.navigation_home) {
            navView?.selectedItemId = R.id.navigation_home
        }
        loadMyReservation()
    }

    private fun loadMyReservation() {
        handler.removeCallbacksAndMessages(null) // 이전 타이머 콜백 제거
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("User").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
            if (_binding == null || !isAdded) { return@addOnSuccessListener }

            val studentId = userDoc.getString("studentId") ?: userDoc.getString("userID")
            if (studentId.isNullOrBlank()) {
                updateReservationCard(null)
                return@addOnSuccessListener
            }

            db.collection("Reservations")
                .whereEqualTo("userID", studentId)
                .get()
                .addOnSuccessListener { reservationSnapshot ->
                    if (_binding == null || !isAdded) { return@addOnSuccessListener }

                    val now = Calendar.getInstance()
                    val reservations = reservationSnapshot.documents.mapNotNull { doc ->
                        doc.toObject<com.example.pick_dream.model.Reservation>()
                    }

                    Log.d("HomeFragment", "Fetched ${reservations.size} reservations for user $studentId")
                    reservations.forEach {
                        val startCal = it.startTime?.let { it1 -> parseKoreanDateToCalendar(it1) }
                        Log.d("HomeFragment", "Reservation for ${it.roomID}: startTime='${it.startTime}', parsed=${startCal != null}")
                    }

                    val activeReservation = reservations.firstOrNull {
                        val startCal = it.startTime?.let { it1 -> parseKoreanDateToCalendar(it1) }
                        val endCal = it.endTime?.let { it1 -> parseKoreanDateToCalendar(it1) }
                        startCal != null && endCal != null && !now.before(startCal) && now.before(endCal)
                    }

                    val upcomingReservation = if (activeReservation == null) {
                        reservations.filter {
                            val startCal = it.startTime?.let { it1 -> parseKoreanDateToCalendar(it1) }
                            startCal != null && now.before(startCal)
                        }.minByOrNull { parseKoreanDateToCalendar(it.startTime!!)!!.timeInMillis }
                    } else {
                        null
                    }

                    val reservationToShow = activeReservation ?: upcomingReservation
                    updateReservationCard(reservationToShow)
                }
        }
    }

    private fun updateReservationCard(reservation: com.example.pick_dream.model.Reservation?) {
        if (_binding == null || !isAdded) return

        if (reservation == null) {
            binding.layoutNoReservation.visibility = View.VISIBLE
            binding.layoutReservationDetails.visibility = View.GONE
            binding.flReservationStatusVisual.visibility = View.GONE
            return
        }

        binding.layoutNoReservation.visibility = View.GONE
        binding.layoutReservationDetails.visibility = View.VISIBLE
        binding.flReservationStatusVisual.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        val roomIdOnly = reservation.roomID.replace(Regex("[^0-9]"), "")
        db.collection("rooms").document(roomIdOnly).get().addOnSuccessListener { roomDoc ->
            if (_binding == null || !isAdded) { return@addOnSuccessListener }

            if(roomDoc.exists()){
                binding.tvReservationRoom.text = "예약 장소 : ${roomDoc.getString("name")}"
                val imageUrl = roomDoc.getString("image")
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(imageUrl).into(binding.ivRoomBackground)
                } else {
                    binding.ivRoomBackground.setImageResource(R.drawable.sample_room) // 기본 이미지
                }
            } else {
                binding.tvReservationRoom.text = "예약 장소 : ${reservation.roomID}"
                binding.ivRoomBackground.setImageResource(R.drawable.sample_room) // 기본 이미지
            }
        }
        
        val startCal = reservation.startTime?.let { parseKoreanDateToCalendar(it) }
        val endCal = reservation.endTime?.let { parseKoreanDateToCalendar(it) }

        if (startCal != null && endCal != null) {
            binding.tvReservationTime.text = "대여 시간 : ${formatKoreanTime(startCal)} - ${formatKoreanTime(endCal)}"
            
            // 현재 예약 정보 SharedPreferences에 저장
            val sharedPrefs = requireActivity().getSharedPreferences("ClassroomPrefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putLong("last_end_time", endCal.timeInMillis)
                putString("last_room_id", reservation.roomID)
                putBoolean("has_shown_review", false) // 새로운 예약이 시작되면 리뷰 창 표시 여부 초기화
                apply()
            }
            
            timerRunnable = object : Runnable {
                override fun run() {
                    val now = Calendar.getInstance()
                    val isActive = !now.before(startCal)

                    if (isActive) {
                        val remainingMillis = endCal.timeInMillis - now.timeInMillis
                        if (remainingMillis > 0) {
                            val totalDuration = endCal.timeInMillis - startCal.timeInMillis
                            val elapsedTime = now.timeInMillis - startCal.timeInMillis
                            val progress = if (totalDuration > 0) (elapsedTime * 100 / totalDuration).toInt() else 0

                            binding.pbReservationProgress.progress = progress.coerceIn(0, 100)
                            binding.tvProgressPercentage.text = "${progress.coerceIn(0,100)}%"

                            val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
                            
                            binding.tvRemainingTime.text = if (hours > 0) {
                                String.format("%d시간 %d분 후 종료", hours, minutes)
                            } else {
                                String.format("%d분 후 종료", minutes)
                            }
                            handler.postDelayed(this, 1000 * 30) // 30초마다 업데이트
                        } else {
                            loadMyReservation()
                        }
                    } else { // Upcoming
                        val remainingMillis = startCal.timeInMillis - now.timeInMillis
                        if (remainingMillis > 0) {
                            val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
                            binding.pbReservationProgress.progress = 0
                            binding.tvProgressPercentage.text = "예약대기"
                            binding.tvRemainingTime.text = String.format("%d시간 %d분 후 시작", hours, minutes)
                            handler.postDelayed(this, 1000 * 60) // 1분마다 업데이트
                        } else {
                            loadMyReservation()
                        }
                    }
                }
            }
            handler.post(timerRunnable!!)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null) // 화면 벗어나면 타이머 중지
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}

private fun parseKoreanDateToCalendar(dateStr: String): Calendar? {
    // Firestore에 저장된 '...h시 m분 s초...' 형식 파싱 시도
    val formatWithSeconds = SimpleDateFormat("yyyy년 M월 d일 a h시 m분 s초 'UTC+9'", Locale.KOREAN)
    try {
        return Calendar.getInstance().apply { time = formatWithSeconds.parse(dateStr)!! }
    } catch (e: Exception) {
        // AI가 반환할 수 있는 '...h시 m분' 형식 파싱 시도 (초가 없는 경우)
        val formatWithoutSeconds = SimpleDateFormat("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
        try {
            return Calendar.getInstance().apply { time = formatWithoutSeconds.parse(dateStr)!! }
        } catch (e2: Exception) {
            Log.e("HomeFragment", "DATE_PARSE_ERROR: Could not parse date: '$dateStr'", e2)
            return null
        }
    }
}

private fun formatKoreanTime(calendar: Calendar): String {
    val format = SimpleDateFormat("a h:mm", Locale.KOREAN)
    return format.format(calendar.time)
}