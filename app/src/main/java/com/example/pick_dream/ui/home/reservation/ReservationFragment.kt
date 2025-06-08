package com.example.pick_dream.ui.home.reservation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentReservationBinding
import com.example.pick_dream.model.Reservation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.*

sealed class ReservationListItem {
    data class Header(val title: String) : ReservationListItem()
    data class ReservationItem(val reservation: Reservation, val imageUrl: String?) : ReservationListItem()
}

class ReservationFragment : Fragment() {
    private var _binding: FragmentReservationBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReservationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadReservations()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(
            onCancelClick = { reservation -> confirmCancellation(reservation) },
            onDetailClick = { reservation -> showReservationDetails(reservation) },
            onWriteReviewClick = { reservation -> navigateToWriteReview(reservation) }
        )
        binding.rvReservations.layoutManager = LinearLayoutManager(context)
        binding.rvReservations.adapter = adapter
    }

    private fun loadReservations() {
        binding.progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("User").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
            val studentId = userDoc.getString("studentId") ?: userDoc.getString("userID")
            if (studentId.isNullOrBlank()) {
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                return@addOnSuccessListener
            }

            db.collection("Reservations")
                .whereEqualTo("userID", studentId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { reservationSnapshot ->
                    binding.progressBar.visibility = View.GONE
                    if (reservationSnapshot.isEmpty) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    val reservations = reservationSnapshot.documents.mapNotNull {
                        it.toObject<Reservation>()?.apply { documentId = it.id }
                    }
                    fetchRoomDetailsFor(reservations)
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "예약 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchRoomDetailsFor(reservations: List<Reservation>) {
        val db = FirebaseFirestore.getInstance()
        val roomIds = reservations.map { it.roomID }.distinct().filter { it.isNotBlank() }

        if (roomIds.isEmpty()) {
            processAndDisplayReservations(reservations, emptyMap())
            return
        }

        val tasks = roomIds.map { db.collection("rooms").document(it).get() }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
            val roomImageUrls = documents.filter { it.exists() }
                .associate { it.id to it.getString("image") }
            processAndDisplayReservations(reservations, roomImageUrls)
        }.addOnFailureListener {
            Log.e("ReservationFragment", "Failed to fetch room details", it)
            processAndDisplayReservations(reservations, emptyMap())
        }
    }

    private fun parseFlexibleDate(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null

        // Normalize AM/PM markers for Korean locale
        val normalizedDateString = dateString
            .replace("PM", "오후", ignoreCase = true)
            .replace("AM", "오전", ignoreCase = true)

        // List of possible date formats
        val dateFormats = listOf(
            SimpleDateFormat("yyyy년 M월 d일 a h시 m분 s초 'UTC+9'", Locale.KOREAN),
            SimpleDateFormat("yyyy년 M월 d일 a h시 m분 s초", Locale.KOREAN),
            SimpleDateFormat("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
        )

        for (format in dateFormats) {
            try {
                return format.parse(normalizedDateString)
            } catch (e: java.text.ParseException) {
                // Ignore and try the next format
            }
        }

        Log.e("ReservationFragment", "Could not parse date: $dateString with any known format.")
        return null
    }

    private fun processAndDisplayReservations(reservations: List<Reservation>, roomImageUrls: Map<String, String?>) {
        val now = Date()
        val upcomingReservations = mutableListOf<Reservation>()
        val pastReservations = mutableListOf<Reservation>()

        reservations.forEach { reservation ->
            val endTime = parseFlexibleDate(reservation.endTime)
            if (endTime != null && endTime.after(now)) {
                upcomingReservations.add(reservation)
            } else {
                // Also handles cases where endTime is null or unparseable
                pastReservations.add(reservation)
            }
        }
        
        // 다가오는 예약은 시작 시간 오름차순으로 정렬
        upcomingReservations.sortBy {
            parseFlexibleDate(it.startTime)?.time ?: Long.MAX_VALUE
        }

        val listItems = mutableListOf<ReservationListItem>()
        if (upcomingReservations.isNotEmpty()) {
            listItems.add(ReservationListItem.Header("현재 예약 및 예정된 예약"))
            upcomingReservations.forEach {
                val imageUrl = roomImageUrls[it.roomID]
                listItems.add(ReservationListItem.ReservationItem(it, imageUrl))
            }
        }
        if (pastReservations.isNotEmpty()) {
            listItems.add(ReservationListItem.Header("지난 예약"))
            pastReservations.forEach {
                val imageUrl = roomImageUrls[it.roomID]
                listItems.add(ReservationListItem.ReservationItem(it, imageUrl))
            }
        }

        if (listItems.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvReservations.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvReservations.visibility = View.VISIBLE
        }
        adapter.submitList(listItems)
    }

    private fun confirmCancellation(reservation: Reservation) {
        AlertDialog.Builder(requireContext())
            .setTitle("예약 취소")
            .setMessage("${reservation.roomID} 예약을 정말로 취소하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                cancelReservation(reservation)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun cancelReservation(reservation: Reservation) {
        val docId = reservation.documentId
        if (docId.isNullOrEmpty()) {
            Toast.makeText(context, "예약 ID가 없어 취소할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("Reservations").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                loadReservations() // 목록 새로고침
            }
            .addOnFailureListener {
                Toast.makeText(context, "예약 취소에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReservationDetails(reservation: Reservation) {
        // 상세 정보 보기 로직 (예: BottomSheetDialogFragment)
        // Toast.makeText(context, "${reservation.roomID} 상세 보기", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToWriteReview(reservation: Reservation){
        val roomId = reservation.roomID
        if (roomId.isNullOrEmpty()) {
            Toast.makeText(context, "강의실 정보가 없어 후기를 작성할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val bundle = bundleOf("roomId" to roomId)
        findNavController().navigate(R.id.action_reservationFragment_to_reviewFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 