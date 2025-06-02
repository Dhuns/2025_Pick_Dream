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

class LlmFragment : Fragment() {
    private var _binding: FragmentLlmBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<LlmMessage>()
    private lateinit var adapter: LlmAdapter

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

        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE

        binding.toolbarTitle.text = "AI 예약"

        binding.btnBack.setOnClickListener {
            navigateToHome()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }
            }
        )

        adapter = LlmAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        showSuggestions()

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
            messages.add(LlmMessage(text, true))
            adapter.notifyItemInserted(messages.size - 1)
            binding.recyclerView.scrollToPosition(messages.size - 1)
            binding.editTextMessage.text.clear()

            if (!isFirstMessageSent) {
                hideSuggestions()
                isFirstMessageSent = true
            }

            val user = FirebaseAuth.getInstance().currentUser
            user?.getIdToken(true)?.addOnSuccessListener { result ->
                val idToken = result.token ?: ""

                FirebaseFunctionService.sendMessageToFunction(
                    message = text,
                    idToken = idToken,
                    onSuccess = { reply ->
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
                messages.add(LlmMessage("로그인이 필요합니다.", false))
                adapter.notifyItemInserted(messages.size - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
        _binding = null
    }
}