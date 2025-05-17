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
import com.example.pick_dream.ui.home.llm.LlmFragment

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
        startTimeCountdown()
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
                // 검색 기능 실행
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
    //시간 카운트 및 마지막 종료 시간 저장 후 후기 표시 여부 초기화
    private fun startTimeCountdown() {
        handler = Handler()
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    remainingSeconds--
                    updateTimeDisplay()
                    handler.postDelayed(this, 1000)
                } else {
                    binding.tvRemainingTime.text = "대여 시간 종료"
                    // 시간이 종료되면 마지막 종료 시간 저장
                    val sharedPrefs = requireActivity().getSharedPreferences("ClassroomPrefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putLong("last_end_time", System.currentTimeMillis())
                        putBoolean("has_shown_review", false)  // 후기 표시 여부 초기화
                        apply()
                    }
                }
            }
        }
        handler.post(timeUpdateRunnable)
    }

    private fun updateTimeDisplay() {
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60

        binding.tvRemainingTime.text = if (hours > 0) {
            "남은 시간: ${hours}시간 ${minutes}분"
        } else {
            "남은 시간: ${minutes}분 ${seconds}초"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeUpdateRunnable)
        _binding = null
    }
}
