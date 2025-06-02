package com.example.pick_dream.ui.home.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pick_dream.databinding.FragmentLectureRoomSelectionBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName

class LectureRoomSelectionFragment : Fragment() {
    private var _binding: FragmentLectureRoomSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: LectureRoomSelectionFragmentArgs by navArgs()
    private lateinit var adapter: LectureRoomSelectionAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLectureRoomSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadAvailableRooms()
    }

    private fun setupUI() {
        binding.tvBuildingName.text = "${args.buildingName} (${args.buildingDetail})"
        
        adapter = LectureRoomSelectionAdapter { lectureRoom ->
            val action = LectureRoomSelectionFragmentDirections
                .actionLectureRoomSelectionFragmentToManualReservationFragment(
                    building = args.buildingDetail,
                    roomName = lectureRoom.name
                )
            findNavController().navigate(action)
        }

        binding.rvLectureRooms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LectureRoomSelectionFragment.adapter
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadAvailableRooms() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyMessage.visibility = View.GONE
        
        db.collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                val rooms = documents.mapNotNull { doc ->
                    try {
                        val room = doc.toObject(LectureRoom::class.java)

                        if (room.buildingDetail.replace(" ", "") == args.buildingDetail.replace(" ", "")) room else null
                    } catch (e: Exception) {
                        null
                    }
                }

                val availableRooms = rooms.filter { it.isAvailable }
                
                adapter.submitList(availableRooms)
                binding.progressBar.visibility = View.GONE
                
                if (availableRooms.isEmpty()) {
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = "현재 사용 가능한 강의실이 없습니다."
                } else {
                    binding.tvEmptyMessage.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyMessage.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = "강의실 정보를 불러오는데 실패했습니다."
                Toast.makeText(context, "강의실 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class LectureRoom(
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("buildingName") @set:PropertyName("buildingName")
    var buildingName: String = "",

    @get:PropertyName("buildingDetail") @set:PropertyName("buildingDetail")
    var buildingDetail: String = "",

    @get:PropertyName("location") @set:PropertyName("location")
    var location: String = "",

    @get:PropertyName("capacity") @set:PropertyName("capacity")
    var capacity: Int = 0,

    @get:PropertyName("equipment") @set:PropertyName("equipment")
    var equipment: List<String> = emptyList(),

    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = false
) {
    constructor() : this("", "", "", "", 0, emptyList(), false)
} 