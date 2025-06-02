package com.example.pick_dream.ui.mypage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pick_dream.databinding.FragmentMypageBinding
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.pick_dream.R

class MypageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyPageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.userName.text = user.name
            binding.userEmail.text = user.email
            binding.userMajor.text = user.major
            binding.userId.text = user.studentId
        }

        viewModel.loadUserData()

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.logoutTextView.paintFlags = binding.logoutTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.logoutTextView.setOnClickListener {
            val intent = Intent(requireContext(), com.example.pick_dream.ui.login.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

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

    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.visibility = View.VISIBLE
        if (navView?.selectedItemId != R.id.navigation_mypage) {
            navView?.selectedItemId = R.id.navigation_mypage
        }
    }
}
