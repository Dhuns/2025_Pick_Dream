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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var endMillis: Long = 0L
    private var currentReservationStartMillis: Long = 0L
    private var currentReservationStatus: String = "NONE"

    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            if (_binding == null) return
            val now = System.currentTimeMillis()

            when (currentReservationStatus) {
                "PENDING" -> {
                    val timeToStart = currentReservationStartMillis - now
                    if (timeToStart > 0) {
                        val minutes = (timeToStart / 1000 / 60) % 60
                        val hours = (timeToStart / 1000 / 60 / 60)
                        _binding?.tvRemainingTime?.text = when {
                            hours > 0 -> String.format(Locale.KOREA, "%d시간 %d분 후 시작", hours, minutes)
                            minutes > 0 -> String.format(Locale.KOREA, "%d분 후 시작", minutes)
                            else -> "잠시 후 시작됩니다"
                        }
                        _binding?.pbReservationProgress?.visibility = View.INVISIBLE
                        _binding?.tvProgressPercentage?.text = "예약대기"
                        handler.postDelayed(this, 1000)
                    } else {
                        loadMyReservation()
                    }
                }
                "RUNNING" -> {
                    val remainingMillis = endMillis - now
                    if (remainingMillis > 0) {
                        val minutes = (remainingMillis / 1000 / 60) % 60
                        val hours = (remainingMillis / 1000 / 60 / 60)
                        _binding?.tvRemainingTime?.text = if (hours > 0) {
                            String.format(Locale.KOREA, "남은 시간: %d시간 %d분", hours, minutes)
                        } else {
                            String.format(Locale.KOREA, "남은 시간: %d분", minutes)
                        }

                        val startMillisTag = binding.root.getTag(R.id.tag_start_millis)?.toString()?.toLongOrNull() ?: 0L
                        if (startMillisTag > 0 && endMillis > startMillisTag) {
                            val totalDuration = endMillis - startMillisTag
                            val elapsedTime = now - startMillisTag
                            if (totalDuration > 0) {
                                val progressPercentage = (elapsedTime * 100 / totalDuration).toInt()
                                val displayProgress = progressPercentage.coerceIn(0, 100)
                                _binding?.pbReservationProgress?.visibility = View.VISIBLE
                                _binding?.pbReservationProgress?.progress = displayProgress
                                _binding?.tvProgressPercentage?.text = "$displayProgress%"
                            } else {
                                _binding?.pbReservationProgress?.progress = 0
                                _binding?.tvProgressPercentage?.text = "0%"
                            }
                        }
                        handler.postDelayed(this, 1000)
                    } else {
                        resetReservationInfo("예약 시간이 종료되었습니다.")
                    }
                }
                "NONE" -> {
                    handler.removeCallbacks(this)
                }
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
    }

    private fun setupClickListeners() {
        listOf(binding.btnLlm, binding.btnSearch, binding.btnInquiry, binding.btnMap).forEach { button ->
            button.setOnClickListener {
                onButtonClick(it)
            }
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

    private fun loadMyReservation() {
        val db = FirebaseFirestore.getInstance()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            resetReservationInfo("로그인이 필요합니다.")
            return
        }

        db.collection("User").document(currentUid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (_binding == null) {
                    Log.w("HomeFragment", "Binding is null in userDoc successListener")
                    return@addOnSuccessListener
                }
                val studentId = userDoc.getString("studentId")
                if (studentId != null) {
                    val now = Date()
                    val queryPattern = "yyyy년 M월 d일 a h시 m분 s초"
                    val queryFormatter = SimpleDateFormat(queryPattern, Locale.KOREA)
                    queryFormatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")

                    db.collection("Reservations")
                        .whereEqualTo("userID", studentId)
                        .whereIn("status", listOf("대기", "확정"))
                        .orderBy("startTime")
                        .limit(1)
                        .get()
                        .addOnSuccessListener { result ->
                            if (_binding == null) {
                                Log.w("HomeFragment", "Binding is null in reservations successListener")
                                return@addOnSuccessListener
                            }
                            if (result.isEmpty) {
                                Log.d("HomeFragment", "No upcoming or pending reservations found for studentId: $studentId")
                                resetReservationInfo()
                                return@addOnSuccessListener
                            }

                            val doc = result.first()
                            val roomID = doc.getString("roomID") ?: ""
                            val userID = doc.getString("userID") ?: ""
                            val startTimeRaw = doc.getString("startTime") ?: ""
                            val endTimeRaw = doc.getString("endTime") ?: ""
                            val status = doc.getString("status") ?: "대기"

                            // val cleanedStartTime = startTimeRaw.replace(Regex("\\s*UTC\\+9\\s*$"), "") // 이전 로직으로 되돌리기 위해 주석 처리
                            // val cleanedEndTime = endTimeRaw.replace(Regex("\\s*UTC\\+9\\s*$"), "")   // 이전 로직으로 되돌리기 위해 주석 처리
                            
                            val dateTimeParser = SimpleDateFormat("yyyy년 M월 d일 a h시 m분 s초", Locale.KOREA)
                            dateTimeParser.timeZone = TimeZone.getTimeZone("Asia/Seoul")

                            var startDate: Date? = null
                            var endDate: Date? = null
                            try {
                                startDate = dateTimeParser.parse(startTimeRaw) // Firestore 원본 문자열 파싱
                            } catch (e: Exception) {
                                Log.e("HomeFragment", "startTime parse error: $startTimeRaw, ${e.message}")
                            }
                            try {
                                endDate = dateTimeParser.parse(endTimeRaw) // Firestore 원본 문자열 파싱
                            } catch (e: Exception) {
                                Log.e("HomeFragment", "endTime parse error: $endTimeRaw, ${e.message}")
                            }

                            val nowMillis = System.currentTimeMillis()

                            if (startDate == null || endDate == null || endDate.time < nowMillis) {
                                Log.d("HomeFragment", "Fetched reservation is invalid or already ended. Start: $startDate, End: $endDate")
                                resetReservationInfo("유효하지 않거나 이미 종료된 예약입니다.")
                                return@addOnSuccessListener
                            }
                            
                            _binding?.layoutNoReservation?.visibility = View.GONE
                            _binding?.layoutReservationDetails?.visibility = View.VISIBLE
                            _binding?.flReservationStatusVisual?.visibility = View.VISIBLE
                            _binding?.ivRoomBackground?.setImageResource(R.drawable.sample_room)

                            val fetchedStartMillis = startDate.time
                            val fetchedEndMillis = endDate.time
                            binding.root.setTag(R.id.tag_start_millis, fetchedStartMillis)

                            val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)
                            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            val startStr = timeFormat.format(startDate)
                            val endStr = timeFormat.format(endDate)

                            _binding?.tvReservationRoom?.text = "예약 장소 : $roomID"
                            _binding?.tvReservationTime?.text = "대여 시간 : $startStr - $endStr"

                            if (status == "대기" && nowMillis < fetchedStartMillis) {
                                Log.d("HomeFragment", "Reservation is PENDING: $roomID")
                                currentReservationStatus = "PENDING"
                                currentReservationStartMillis = fetchedStartMillis
                                this.endMillis = fetchedEndMillis
                            } else if ((status == "확정" || status == "대기") && nowMillis >= fetchedStartMillis && nowMillis < fetchedEndMillis) {
                                Log.d("HomeFragment", "Reservation is RUNNING: $roomID (Status: $status)")
                                currentReservationStatus = "RUNNING"
                                this.endMillis = fetchedEndMillis
                                this.currentReservationStartMillis = fetchedStartMillis
                            } else {
                                Log.d("HomeFragment", "Reservation is in an unexpected state or past: $roomID, Status: $status")
                                resetReservationInfo()
                                return@addOnSuccessListener
                            }
                            
                            handler.removeCallbacks(timeUpdateRunnable)
                            handler.post(timeUpdateRunnable)

                        }
                        .addOnFailureListener { e ->
                            if (_binding == null) return@addOnFailureListener
                            Log.e("HomeFragment", "Failed to fetch reservations: ${e.message}", e)
                            resetReservationInfo("예약 정보를 불러오지 못했습니다.")
                        }
                } else {
                    if (_binding == null) return@addOnSuccessListener
                    Log.w("HomeFragment", "studentId is null")
                    resetReservationInfo("학번 정보가 없습니다.")
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Log.e("HomeFragment", "Failed to fetch user document: ${e.message}", e)
                resetReservationInfo("사용자 정보를 불러오지 못했습니다.")
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
        loadMyReservation()
    }

    private fun resetReservationInfo(message: String? = null) {
        if (_binding == null) {
            Log.w("HomeFragment", "Binding is null in resetReservationInfo")
            return
        }
        Log.d("HomeFragment", "Resetting reservation info. Message: $message")
        currentReservationStatus = "NONE"
        _binding?.layoutNoReservation?.visibility = View.VISIBLE
        _binding?.layoutReservationDetails?.visibility = View.GONE
        _binding?.flReservationStatusVisual?.visibility = View.GONE

        _binding?.tvNoReservationMessage?.text = message ?: "현재 진행 중인 예약이 없네요.\n지금 바로 예약해보세요!"
        try {
            _binding?.ivNoReservationIcon?.setImageResource(android.R.drawable.ic_menu_today) 
        } catch (e: Exception) {
            Log.w("HomeFragment", "Failed to set no reservation icon: ${e.localizedMessage}")
            _binding?.ivNoReservationIcon?.visibility = View.GONE
        }

        handler.removeCallbacks(timeUpdateRunnable)
        binding.root.setTag(R.id.tag_start_millis, 0L)
        endMillis = 0L
        currentReservationStartMillis = 0L
    }
}
