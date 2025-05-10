package com.example.pick_dream.ui.reviewpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.databinding.FragmentReviewCompleteBinding
import com.example.pick_dream.R
import androidx.activity.OnBackPressedCallback

class ReviewpageCompleteFragment : Fragment() {

    private var _binding: FragmentReviewCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 네비게이션 바 숨기기
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE

        // 뒤로가기 무시 (아무 동작도 하지 않음)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 아무 동작도 하지 않음
                }
            })

        //리뷰완료 -> 홈
        binding.btnGoHome.setOnClickListener {
            findNavController().navigate(R.id.action_reviewCompleteFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트가 사라질 때 다시 보이게
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
        _binding = null
    }
}