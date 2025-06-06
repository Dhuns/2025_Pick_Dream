package com.example.pick_dream.ui.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentLectureRoomDetailBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class LectureRoomDetailFragment : Fragment() {
    private val viewModel: LectureRoomDetailViewModel by viewModels()
    private var _binding: FragmentLectureRoomDetailBinding? = null
    private val binding get() = _binding!!
    private val args: LectureRoomDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLectureRoomDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE

        viewModel.loadRoomDetail(args.roomName)

        viewModel.roomDetail.observe(viewLifecycleOwner) { room ->
            if (room != null) {
                binding.tvRoomName.text = room.name
                binding.tvRoomDesc.text = """
                    ${room.buildingName} - ${room.location}
                    수용 인원: 최대 ${room.capacity}명
                    기자재: ${room.equipment.joinToString(", ")}
                """.trimIndent()

                binding.btnFavorite.isSelected = room.isFavorite
                binding.btnFavorite.setOnClickListener {
                    LectureRoomRepository.toggleFavorite(room.name)
                    it.isSelected = !it.isSelected
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnReserve.setOnClickListener {
            val room = viewModel.roomDetail.value
            if (room != null) {
                val action = LectureRoomDetailFragmentDirections.actionLectureRoomDetailFragmentToManualReservationFragment(
                    building = "${room.buildingName} (${room.buildingDetail})",
                    roomName = room.name
                )
                findNavController().navigate(action)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}