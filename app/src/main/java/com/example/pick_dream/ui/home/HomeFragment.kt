package com.example.pickdream.ui.home

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
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentHomeBinding
import com.example.pick_dream.ui.home.reservation.ReservationFragment

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
                // LLM 기능 실행
            }
            R.id.btn_search -> {
                // 검색 기능 실행
            }
            R.id.btn_inquiry -> {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, ReservationFragment())
                    .addToBackStack(null)
                    .commit()
            }
            R.id.btn_map -> {
                // 지도 기능 실행
            }
        }
    }

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
