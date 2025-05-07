package com.example.pick_dream.ui.mypage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.pick_dream.databinding.FragmentMypageBinding



class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        // 뒤로가기 버튼 동작 설정
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        //로그아웃 밑줄
        binding.logoutTextView.paintFlags = binding.logoutTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        //로그아웃 이동
        binding.logoutTextView.setOnClickListener {
            val intent = Intent(requireContext(), com.example.pick_dream.ui.login.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // 작성한 후기 버튼 클릭 시 ReviewActivity로 이동
        binding.reviewButtonCard.setOnClickListener {
            val intent = Intent(requireContext(), com.example.pick_dream.ui.mypage.review.ReviewActivity::class.java)
            startActivity(intent)
        }

        binding.inquiryButtonCard.setOnClickListener {
            val intent = Intent(requireContext(), com.example.pick_dream.ui.mypage.inquiry.InquiryActivity::class.java)
            startActivity(intent)
        }

        binding.settingButtonCard.setOnClickListener {
            val intent = Intent(requireContext(), com.example.pick_dream.ui.mypage.setting.SettingActivity::class.java)
            startActivity(intent)
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
