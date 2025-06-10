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
                val floorNumber = room.name.substring(0, 2).toIntOrNull()?.let { "${it/10}층" } ?: "층수 정보 없음"
                binding.tvRoomDesc.text = "${room.buildingName} ${floorNumber}\n수용 인원: 최대 ${room.capacity}명\n기자재: ${room.equipment.joinToString(", ")}"

                // 하드코딩된 랜덤 데이터 생성 향후 데이터 추가하면 사용 가능
                val randomChairType = listOf("일체형 의자", "분리형 의자", "이동식 의자").random()
                val randomProjector = Math.random() < 0.7 // 70% 확률로 사용 가능
                val randomBlackboard = Math.random() < 0.5 // 50% 확률로 사용 가능

                // 상세 정보 박스 채우기
                binding.infoBoxRoomName.text = "강의실 : ${room.name} "
                binding.infoBoxEquipment.text = "기자재 목록 : ${room.equipment.joinToString(", ")}"
                binding.infoBoxChairType.text = "의자 : $randomChairType"
                binding.infoBoxProjector.text = "빔 프로젝터 대여 여부 : ${if (randomProjector) "사용가능" else "사용불가"}"
                binding.infoBoxBlackboard.text = "전자 칠판 대여 여부 : ${if (randomBlackboard) "사용가능" else "사용불가"}"
                binding.infoBoxRentalAvailability.text = if (room.isRentalAvailable) "앱에서 바로 예약 가능" else "예약 불가"

                // 대여 가능 여부에 따라 버튼 상태 변경
                if (room.isRentalAvailable) {
                    binding.btnReserve.isEnabled = true
                    binding.btnReserve.setBackgroundColor(resources.getColor(R.color.primary_400, null))
                    binding.btnReserve.setTextColor(resources.getColor(android.R.color.white, null))
                } else {
                    binding.btnReserve.isEnabled = false
                    binding.btnReserve.setBackgroundColor(resources.getColor(R.color.neutral_200, null))
                    binding.btnReserve.setTextColor(resources.getColor(R.color.neutral_400, null))
                }

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