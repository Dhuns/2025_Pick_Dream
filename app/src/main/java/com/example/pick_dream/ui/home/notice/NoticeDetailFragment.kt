package com.example.pick_dream.ui.home.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.pick_dream.databinding.FragmentNoticeDetailBinding

class NoticeDetailFragment : Fragment() {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: NoticeDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = args.title
        binding.tvNoticeTitle.text = args.title
        binding.tvNoticeDate.text = args.date
        binding.tvNoticeContent.text = args.content

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
