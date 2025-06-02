package com.example.pick_dream.ui.reviewpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pick_dream.databinding.FragmentReviewBinding
import com.example.pick_dream.R
import androidx.navigation.fragment.findNavController

class ReviewpageFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    private val starIds = listOf(
        R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5
    )
    private var selectedStars = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE

        val starViews = starIds.map { binding.root.findViewById<ImageView>(it) }
        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedStars = index + 1
                updateStars(starViews, selectedStars)
            }
        }
        updateStars(starViews, selectedStars)

        for (i in 0 until binding.layoutPurpose.childCount) {
            val cb = binding.layoutPurpose.getChildAt(i)
            if (cb is CheckBox) {
                cb.buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.checkbox_selector)
                cb.setOnCheckedChangeListener { buttonView, isChecked ->
                }
            }
        }

        for (i in 0 until binding.layoutEquip.childCount) {
            val cb = binding.layoutEquip.getChildAt(i)
            if (cb is CheckBox) {
                cb.buttonDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.checkbox_selector)
                cb.setOnCheckedChangeListener { buttonView, isChecked ->
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(R.id.action_reviewpageFragment_to_reviewCompleteFragment)
        }

        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_reviewpageFragment_to_homeFragment)
        }
    }

    private fun updateStars(starViews: List<ImageView>, selectedCount: Int) {
        starViews.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < selectedCount) R.drawable.ic_star_color else R.drawable.ic_star_empty
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
        _binding = null
    }
}