package com.example.pick_dream.ui.home

import android.os.Bundle
import android.os.Handler
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

                val startMillis = binding.root.getTag(R.id.tag_start_millis)?.toString()?.toLongOrNull() ?: 0L // loadMyReservation에서 저장한 값 가져오기
                if (startMillis > 0 && endMillis > startMillis) {
                    val totalDuration = endMillis - startMillis
                    val elapsedTime = now - startMillis
                    val progressPercentage = (elapsedTime * 100 / totalDuration).toInt()
                    val displayProgress = if (progressPercentage > 100) 100 else if (progressPercentage < 0) 0 else progressPercentage
                    _binding?.pbReservationProgress?.progress = displayProgress
                    _binding?.tvProgressPercentage?.text = "$displayProgress%"
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
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("User").document(currentUid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (_binding == null) {
                    return@addOnSuccessListener
                }
                val studentId = userDoc.getString("studentId")
                if (studentId != null) {
                    val now = Date()
                    val pattern = "yyyy년 M월 d일 a h시 m분 s초"
                    val formatter = SimpleDateFormat(pattern, Locale.KOREA)
                    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val nowString = formatter.format(now)

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
                            if (result.isEmpty) {
                                resetReservationInfo()
                                return@addOnSuccessListener
                            }

                            val doc = result.first()
                            val roomID = doc.getString("roomID") ?: ""
                            val userID = doc.getString("userID") ?: ""
                            val startTimeRaw = doc.getString("startTime") ?: ""
                            val endTimeRaw = doc.getString("endTime") ?: ""

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

                            val nowMillis = System.currentTimeMillis()
                            val startMillis = startDate?.time ?: 0L
                            endMillis = endDate?.time ?: 0L

                            binding.root.setTag(R.id.tag_start_millis, startMillis)

                            val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREA)
                            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            val startStr = startDate?.let { timeFormat.format(it) } ?: "-"
                            val endStr = endDate?.let { timeFormat.format(it) } ?: "-"

                            when {
                                nowMillis < startMillis -> {
                                    _binding?.tvReservationRoom?.text = "예약 장소 : $roomID"
                                    _binding?.tvReservationTime?.text = "대여 시간 : $startStr - $endStr"
                                    _binding?.tvRemainingTime?.text = "아직 예약 시간이 아닙니다"
                                    _binding?.ivRoomBackground?.setImageResource(R.drawable.sample_room)
                                    _binding?.pbReservationProgress?.progress = 0
                                    _binding?.tvProgressPercentage?.text = "0%"
                                    handler.removeCallbacks(timeUpdateRunnable)
                                }
                                nowMillis in startMillis until endMillis -> {
                                    _binding?.tvReservationRoom?.text = "예약 장소 : $roomID"
                                    _binding?.tvReservationTime?.text = "대여 시간 : $startStr - $endStr"

                                    val totalDuration = endMillis - startMillis
                                    val elapsedTime = nowMillis - startMillis
                                    val currentElapsedTime = if (elapsedTime < 0) 0 else elapsedTime
                                    val progressPercentage = if (totalDuration > 0) (currentElapsedTime * 100 / totalDuration).toInt() else 0
                                    val displayProgress = progressPercentage.coerceIn(0, 100)

                                    _binding?.ivRoomBackground?.setImageResource(R.drawable.sample_room)
                                    _binding?.pbReservationProgress?.progress = displayProgress
                                    _binding?.tvProgressPercentage?.text = "$displayProgress%"
                                    
                                    handler.removeCallbacks(timeUpdateRunnable)
                                    handler.post(timeUpdateRunnable)
                                }
                                else -> {
                                    resetReservationInfo()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            if (_binding == null) {
                                return@addOnFailureListener
                            }
                            resetReservationInfo("예약 정보를 불러오지 못했습니다.")
                        }
                } else {
                    if (_binding == null) {
                        return@addOnSuccessListener
                    }
                    resetReservationInfo("학번 정보가 없습니다.")
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) {
                    return@addOnFailureListener
                }
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
    }

    private fun resetReservationInfo(message: String = "예약 내역이 없습니다.") {
        _binding?.tvReservationRoom?.text = ""
        _binding?.tvReservationTime?.text = ""
        _binding?.tvRemainingTime?.text = message
        _binding?.ivRoomBackground?.setImageResource(R.color.default_reservation_bg_color)
        _binding?.pbReservationProgress?.progress = 0
        _binding?.tvProgressPercentage?.text = "0%"
        handler.removeCallbacks(timeUpdateRunnable)
    }
}
