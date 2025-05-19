package com.example.pick_dream.ui.home.llm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pick_dream.databinding.FragmentLlmBinding
import com.example.pick_dream.R
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import android.util.Log


class LlmFragment : Fragment() {
    private var _binding: FragmentLlmBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<LlmMessage>()
    private lateinit var adapter: LlmAdapter

    // 예시 질문 리스트
    private val suggestions = listOf(
        "6명이서 이용하기 좋은 강의실은 어디야?",
        "지금 예약 가능한 강의실 알려줘",
        "전자칠판 있는 강의실 추천해줘",
        "빔프로젝터 있는 강의실 추천해줘"
    )
    private var isFirstMessageSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLlmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 네비게이션 바 숨기기
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE

        // 커스텀 상단바 타이틀 설정
        binding.toolbarTitle.text = "AI 예약"

        // 뒤로가기 버튼 클릭 시 홈화면으로 이동 (스택 정리)
        binding.btnBack.setOnClickListener {
            navigateToHome()
        }

        // 시스템 뒤로가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }
            }
        )

        // RecyclerView 설정
        adapter = LlmAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 추천질문 동적 추가
        showSuggestions()

        // 전송 버튼 클릭
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build()
        )
    }

    private fun showSuggestions() {
        binding.layoutSuggestions.removeAllViews()
        suggestions.forEach { text ->
            val chip = LayoutInflater.from(context)
                .inflate(R.layout.item_suggestion_chip, binding.layoutSuggestions, false) as TextView
            chip.text = text
            chip.setOnClickListener {
                binding.editTextMessage.setText(text)
                binding.editTextMessage.setSelection(text.length)
            }
            binding.layoutSuggestions.addView(chip)
        }
        binding.suggestionScroll.visibility = View.VISIBLE
    }

    private fun hideSuggestions() {
        binding.suggestionScroll.visibility = View.GONE
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            // 1. 내 메시지 RecyclerView에 추가 (UI 스레드니까 문제 없음)
            messages.add(LlmMessage(text, true))
            adapter.notifyItemInserted(messages.size - 1)
            binding.recyclerView.scrollToPosition(messages.size - 1)
            binding.editTextMessage.text.clear()

            // 추천 질문 숨기기
            if (!isFirstMessageSent) {
                hideSuggestions()
                isFirstMessageSent = true
            }

            // 2. Firebase 사용자 토큰 가져오기
            val user = FirebaseAuth.getInstance().currentUser
            user?.getIdToken(true)?.addOnSuccessListener { result ->
                val idToken = result.token ?: ""

                // 3. Function 호출
                FirebaseFunctionService.sendMessageToFunction(
                    message = text,
                    idToken = idToken,
                    onSuccess = { reply ->
                        // UI 스레드에서 실행
                        requireActivity().runOnUiThread {
                            messages.add(LlmMessage(reply, false))
                            adapter.notifyItemInserted(messages.size - 1)
                            binding.recyclerView.scrollToPosition(messages.size - 1)
                        }
                    },
                    onFailure = { e ->
                        requireActivity().runOnUiThread {
                            messages.add(LlmMessage("오류 발생: ${e.localizedMessage}", false))
                            adapter.notifyItemInserted(messages.size - 1)
                        }
                    }
                )
            } ?: run {
                // 비로그인 상태 처리
                messages.add(LlmMessage("로그인이 필요합니다.", false))
                adapter.notifyItemInserted(messages.size - 1)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        // 하단 네비게이션 바 다시 보이게 하기
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
        _binding = null
    }
}