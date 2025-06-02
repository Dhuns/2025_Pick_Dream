package com.example.pick_dream.ui.home.reservation

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentReservationBinding
import com.example.pick_dream.databinding.ItemReservationHistoryBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import java.text.SimpleDateFormat
import java.util.*

class ReservationFragment : Fragment() {
    private var _binding: FragmentReservationBinding? = null
    private val binding get() = _binding!!
    private var userName: String = ""
    private var userId: String = ""
    private var currentPage = 1
    private var totalPages = 1
    private var historyList: List<ReservationData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.progressBar.visibility = View.VISIBLE
        binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.GONE
        binding.root.findViewById<LinearLayout>(R.id.layoutHistoryList)?.visibility = View.GONE
        binding.root.findViewById<TextView>(R.id.tvEmptyState)?.visibility = View.GONE
        val db = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            db.collection("User").document(uid).get()
                .addOnSuccessListener { userDoc ->
                    if (_binding == null || !isAdded) return@addOnSuccessListener
                    userName = userDoc.getString("name") ?: ""
                    userId = userDoc.getString("studentId") ?: userDoc.getString("userID") ?: ""
                    if (userId.isNotBlank()) {
                        db.collection("Reservations")
                            .whereEqualTo("userID", userId)
                            .get()
                            .addOnSuccessListener { result ->
                                if (_binding == null || !isAdded) return@addOnSuccessListener
                                val reservations = result.map { doc ->
                                    ReservationData(
                                        imageRes = R.drawable.sample_room,
                                        roomID = doc.getString("roomID") ?: "",
                                        startTime = doc.getString("startTime") ?: "",
                                        endTime = doc.getString("endTime") ?: "",
                                        status = doc.getString("status") ?: "",
                                        equipment = doc?.get("equipment") as? List<String> ?: emptyList(),
                                        people = (doc?.get("eventParticipants") as? Long)?.toInt()
                                    )
                                }
                                fetchRoomInfosAndDisplay(reservations) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.VISIBLE
                                }
                            }
                            .addOnFailureListener {
                                if (_binding == null || !isAdded) return@addOnFailureListener
                                binding.progressBar.visibility = View.GONE
                                binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.VISIBLE
                                showError("예약 정보를 불러오지 못했습니다.")
                            }
                    } else {
                        if (_binding == null || !isAdded) return@addOnSuccessListener
                        binding.progressBar.visibility = View.GONE
                        binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.VISIBLE
                        showError("사용자 정보를 찾을 수 없습니다.")
                    }
                }
                .addOnFailureListener {
                    if (_binding == null || !isAdded) return@addOnFailureListener
                    binding.progressBar.visibility = View.GONE
                    binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.VISIBLE
                    showError("사용자 정보를 불러오지 못했습니다.")
                }
        } else {
            binding.progressBar.visibility = View.GONE
            binding.root.findViewById<View>(R.id.scrollViewReservation)?.visibility = View.VISIBLE
            showError("로그인된 사용자가 없습니다.")
        }
        val overlayRoomPhoto = view.findViewById<android.widget.FrameLayout>(R.id.overlayRoomPhoto)
        val btnOverlayBack = view.findViewById<ImageButton>(R.id.btnOverlayBack)
        btnOverlayBack?.setOnClickListener {
            val fragment = childFragmentManager.findFragmentByTag("ReservationDetailBottomSheet")
            if (fragment is ReservationDetailBottomSheet) {
                fragment.dismiss()
            }
            overlayRoomPhoto?.visibility = View.GONE
        }
        childFragmentManager.setFragmentResultListener("close_bottom_sheet", viewLifecycleOwner) { _, _ ->
            overlayRoomPhoto?.visibility = View.GONE
        }
    }

    private fun fetchRoomInfosAndDisplay(reservations: List<ReservationData>, onUiReady: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        if (_binding == null || !isAdded) return
        if (reservations.isEmpty()) {
            setupCurrentReservationCard(null)
            setupHistoryList(emptyList())
            showEmptyState(true)
            onUiReady()
            return
        }
        val tasks = reservations.map { reservation ->
            val roomIdOnly = reservation.roomID.replace(Regex("[^0-9]"), "")
            db.collection("rooms").document(roomIdOnly)
                .get()
                .continueWith { task ->
                    val doc = task.result
                    reservation.copy(
                        building = (doc?.getString("buildingName") ?: "") +
                            (if (doc?.getString("buildingDetail")?.isNotBlank() == true) " (" + doc.getString("buildingDetail") + ")" else ""),
                        roomName = doc?.getString("name") ?: "",
                        equipment = doc?.get("equipment") as? List<String> ?: emptyList()
                    )
                }
        }
        Tasks.whenAllSuccess<ReservationData>(tasks)
            .addOnSuccessListener { mergedList ->
                if (_binding == null || !isAdded) return@addOnSuccessListener
                fun ReservationData.startDate(): Calendar? = parseKoreanDateToCalendar(this.startTime)
                val doneList = mergedList.filter { it.status == "종료" }
                    .sortedByDescending { item: ReservationData -> item.startDate()?.time }
                val confirmedList = mergedList.filter { it.status == "확정" }.sortedBy { item: ReservationData -> item.startDate()?.time }
                val waitingList = mergedList.filter { it.status == "대기" }.sortedBy { item: ReservationData -> item.startDate()?.time }
                val currentReservation = when {
                    confirmedList.isNotEmpty() -> confirmedList.first()
                    waitingList.isNotEmpty() -> waitingList.first()
                    else -> null
                }
                setupCurrentReservationCard(currentReservation)
                setupHistoryList(doneList)
                showEmptyState(doneList.isEmpty() && currentReservation == null)
                setupPagination()
                onUiReady()
            }
            .addOnFailureListener {
                if (_binding == null || !isAdded) return@addOnFailureListener
                showError("강의실 정보를 불러오지 못했습니다.")
                onUiReady()
            }
    }

    private fun setupCurrentReservationCard(currentReservation: ReservationData?) {
        if (_binding == null || !isAdded) return
        val cardLayout = binding.root.findViewById<View>(R.id.cardCurrentReservation)
        if (currentReservation == null) {
            cardLayout?.visibility = View.GONE
            return
        }
        cardLayout?.visibility = View.VISIBLE
        val imgRoom = cardLayout?.findViewById<ImageView>(R.id.imgRoom)
        val tvRoomName = cardLayout?.findViewById<TextView>(R.id.tvRoomName)
        val tvDate = cardLayout?.findViewById<TextView>(R.id.tvDate)
        val tvTime = cardLayout?.findViewById<TextView>(R.id.tvTime)
        val btnCancel = cardLayout?.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = cardLayout?.findViewById<Button>(R.id.btnConfirm)
        val building = if (currentReservation.building.isNotBlank()) currentReservation.building else "강의동 정보 없음"
        val room = if (currentReservation.roomName.isNotBlank()) currentReservation.roomName else "강의실 정보 없음"
        val roomSpannable = SpannableString("$building\n$room")
        roomSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, building.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        roomSpannable.setSpan(StyleSpan(Typeface.BOLD), building.length + 1, roomSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvRoomName?.text = roomSpannable
        val startCal = parseKoreanDateToCalendar(currentReservation.startTime)
        val endCal = parseKoreanDateToCalendar(currentReservation.endTime)
        if (startCal != null && endCal != null) {
            val dayOfWeek = getKoreanDayOfWeek(startCal)
            tvDate?.text = "${startCal.get(Calendar.YEAR)}년 ${startCal.get(Calendar.MONTH)+1}월 ${startCal.get(Calendar.DAY_OF_MONTH)}일 (${dayOfWeek})"
            tvTime?.text = "${formatKoreanTime(startCal)} - ${formatKoreanTime(endCal)}"
        } else {
            tvDate?.text = ""
            tvTime?.text = ""
        }
        imgRoom?.setImageResource(currentReservation.imageRes)
        btnCancel?.isEnabled = true
        btnConfirm?.isEnabled = true
        btnCancel?.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reservation_cancel, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            val background = android.graphics.drawable.GradientDrawable()
            background.setColor(android.graphics.Color.WHITE)
            val radius = resources.displayMetrics.density * 16 // 16dp
            background.cornerRadius = radius
            dialog.setOnShowListener {
                val width = (resources.displayMetrics.widthPixels * 0.6).toInt()
                dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.window?.setBackgroundDrawable(background)
            }
            dialogView.findViewById<TextView>(R.id.btnDialogYes)?.setOnClickListener {
                dialog.dismiss()
                val db = FirebaseFirestore.getInstance()
                db.collection("Reservations")
                    .whereEqualTo("userID", userId)
                    .whereEqualTo("roomID", currentReservation.roomID)
                    .whereEqualTo("startTime", currentReservation.startTime)
                    .whereEqualTo("endTime", currentReservation.endTime)
                    .get()
                    .addOnSuccessListener { result ->
                        for (doc in result) {
                            db.collection("Reservations").document(doc.id).delete()
                        }
                        onViewCreated(requireView(), null)
                    }
                val doneView = LayoutInflater.from(context).inflate(R.layout.dialog_reservation_cancel_done, null)
                val doneDialog = AlertDialog.Builder(requireContext())
                    .setView(doneView)
                    .create()
                val doneBackground = android.graphics.drawable.GradientDrawable()
                doneBackground.setColor(android.graphics.Color.WHITE)
                doneBackground.cornerRadius = radius
                doneDialog.setOnShowListener {
                    val width = (resources.displayMetrics.widthPixels * 0.6).toInt()
                    doneDialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                    doneDialog.window?.setBackgroundDrawable(doneBackground)
                }
                doneView.findViewById<TextView>(R.id.btnDialogOk)?.setOnClickListener {
                    doneDialog.dismiss()
                }
                doneDialog.show()
            }
            dialogView.findViewById<TextView>(R.id.btnDialogNo)?.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        btnConfirm?.setOnClickListener {
            val facilities = currentReservation.equipment.joinToString(", ")
            val reserver = if (userName.isNotBlank() && userId.isNotBlank()) "$userName ($userId)" else userName
            val bottomSheet = ReservationDetailBottomSheet.newInstance(
                room = "$building ${room}",
                date = tvDate?.text?.toString() ?: "",
                time = tvTime?.text?.toString() ?: "",
                people = currentReservation.people?.toString() ?: "1",
                facilities = facilities,
                reserver = reserver,
                imageRes = currentReservation.imageRes
            )
            bottomSheet.show(parentFragmentManager, "ReservationDetailBottomSheet")
        }
    }

    private fun setupHistoryList(list: List<ReservationData>) {
        historyList = list
        totalPages = if (list.isEmpty()) 1 else ((list.size - 1) / 3 + 1)
        currentPage = 1
        showHistoryPage(currentPage)
        setupPagination()
    }

    private fun showHistoryPage(page: Int) {
        val container = binding.root.findViewById<LinearLayout>(R.id.layoutHistoryList)
        container?.removeAllViews()
        if (historyList.isEmpty()) {
            container?.visibility = View.GONE
            return
        }
        container?.visibility = View.VISIBLE
        val startIdx = (page - 1) * 3
        val endIdx = minOf(startIdx + 3, historyList.size)
        for (item in historyList.subList(startIdx, endIdx)) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_reservation_history, container, false)
            val imgRoom = itemView.findViewById<ImageView>(R.id.imgRoom)
            val tvRoomName = itemView.findViewById<TextView>(R.id.tvRoomName)
            val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
            val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
            imgRoom.setImageResource(item.imageRes)
            val building = if (item.building.isNotBlank()) item.building else "강의동 정보 없음"
            val room = if (item.roomName.isNotBlank()) item.roomName else "강의실 정보 없음"
            val roomSpannable = SpannableString("$building\n$room")
            roomSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, building.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            roomSpannable.setSpan(StyleSpan(Typeface.BOLD), building.length + 1, roomSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvRoomName.text = roomSpannable
            val startCal = parseKoreanDateToCalendar(item.startTime)
            val endCal = parseKoreanDateToCalendar(item.endTime)
            if (startCal != null && endCal != null) {
                val dayOfWeek = getKoreanDayOfWeek(startCal)
                tvDate.text = "${startCal.get(Calendar.YEAR)}년 ${startCal.get(Calendar.MONTH)+1}월 ${startCal.get(Calendar.DAY_OF_MONTH)}일 (${dayOfWeek})"
                tvTime.text = "${formatKoreanTime(startCal)} - ${formatKoreanTime(endCal)}"
            } else {
                tvDate.text = ""
                tvTime.text = ""
            }
            container?.addView(itemView)
        }
    }

    private fun setupPagination() {
        val layoutPageNumbers = binding.root.findViewById<LinearLayout>(R.id.layoutPageNumbers)
        layoutPageNumbers?.removeAllViews()
        if (totalPages <= 1) return

        val maxPageButtons = 5
        var startPage = maxOf(1, currentPage - 2)
        var endPage = minOf(totalPages, startPage + maxPageButtons - 1)
        if (endPage - startPage < maxPageButtons - 1) {
            startPage = maxOf(1, endPage - maxPageButtons + 1)
        }

        val showArrows = totalPages > maxPageButtons

        if (showArrows && startPage > 1) {
            val prevBtn = TextView(requireContext())
            prevBtn.text = "<"
            prevBtn.textSize = 18f
            prevBtn.setPadding(16, 8, 16, 8)
            prevBtn.setOnClickListener {
                currentPage = maxOf(1, currentPage - 1)
                showHistoryPage(currentPage)
                setupPagination()
            }
            layoutPageNumbers?.addView(prevBtn)
        }

        for (i in startPage..endPage) {
            val tv = TextView(requireContext())
            tv.text = i.toString()
            tv.textSize = 16f
            tv.setPadding(16, 8, 16, 8)
            tv.setTextColor(if (i == currentPage) 0xFF222222.toInt() else 0xFFB0B0B0.toInt())
            tv.setTypeface(null, if (i == currentPage) Typeface.BOLD else Typeface.NORMAL)
            tv.setOnClickListener {
                currentPage = i
                showHistoryPage(currentPage)
                setupPagination()
            }
            layoutPageNumbers?.addView(tv)
        }

        if (showArrows && endPage < totalPages) {
            val nextBtn = TextView(requireContext())
            nextBtn.text = ">"
            nextBtn.textSize = 18f
            nextBtn.setPadding(16, 8, 16, 8)
            nextBtn.setOnClickListener {
                currentPage = minOf(totalPages, currentPage + 1)
                showHistoryPage(currentPage)
                setupPagination()
            }
            layoutPageNumbers?.addView(nextBtn)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showEmptyState(show: Boolean) {
        if (_binding == null) return
        val emptyView = binding.root.findViewById<TextView>(R.id.tvEmptyState)
        emptyView?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        if (_binding == null) return
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        showEmptyState(true)
    }
}

data class ReservationData(
    val imageRes: Int = R.drawable.sample_room,
    val roomID: String = "",
    val building: String = "",
    val roomName: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "",
    val equipment: List<String> = emptyList(),
    val people: Int? = null
)

fun parseKoreanDateToCalendar(dateStr: String): Calendar? {
    val format = SimpleDateFormat("yyyy년 M월 d일 a h시 m분 s초 'UTC+9'", Locale.KOREAN)
    return try {
        Calendar.getInstance().apply { time = format.parse(dateStr) }
    } catch (e: Exception) {
        null
    }
}

fun getKoreanDayOfWeek(calendar: Calendar): String {
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "일"
        Calendar.MONDAY -> "월"
        Calendar.TUESDAY -> "화"
        Calendar.WEDNESDAY -> "수"
        Calendar.THURSDAY -> "목"
        Calendar.FRIDAY -> "금"
        Calendar.SATURDAY -> "토"
        else -> ""
    }
}

fun formatKoreanTime(calendar: Calendar): String {
    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val ampm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
    val hour12 = if (hour == 0) 12 else hour
    return String.format("%s %d:%02d", ampm, hour12, minute)
}

class HistoryAdapter(private val items: List<ReservationData>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemReservationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemReservationHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.binding.imgRoom.setImageResource(item.imageRes)
        val building = if (item.building.isNotBlank()) item.building else "강의동 정보 없음"
        val room = if (item.roomName.isNotBlank()) item.roomName else "강의실 정보 없음"
        val roomSpannable = SpannableString("$building\n$room")
        roomSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, building.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        roomSpannable.setSpan(StyleSpan(Typeface.BOLD), building.length + 1, roomSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.binding.tvRoomName.text = roomSpannable
        val startCal = parseKoreanDateToCalendar(item.startTime)
        val endCal = parseKoreanDateToCalendar(item.endTime)
        if (startCal != null && endCal != null) {
            val dayOfWeek = getKoreanDayOfWeek(startCal)
            holder.binding.tvDate.text = "${startCal.get(Calendar.YEAR)}년 ${startCal.get(Calendar.MONTH)+1}월 ${startCal.get(Calendar.DAY_OF_MONTH)}일 (${dayOfWeek})"
            holder.binding.tvTime.text = "${formatKoreanTime(startCal)} - ${formatKoreanTime(endCal)}"
        } else {
            holder.binding.tvDate.text = ""
            holder.binding.tvTime.text = ""
        }
    }

    override fun getItemCount() = items.size
} 