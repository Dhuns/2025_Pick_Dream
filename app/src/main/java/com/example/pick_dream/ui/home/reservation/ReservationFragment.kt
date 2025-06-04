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
import android.widget.FrameLayout


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
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        loadInitialData()
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

    private fun loadInitialData() {
        if (_binding == null || !isAdded) return
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
                                        people = (doc?.get("eventParticipants") as? Long)?.toInt(),
                                        documentId = doc.id
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
                        equipment = doc?.get("equipment") as? List<String> ?: emptyList(),
                    )
                }
        }
        Tasks.whenAllSuccess<ReservationData>(tasks)
            .addOnSuccessListener { mergedListWithRoomDetails ->
                if (_binding == null || !isAdded) return@addOnSuccessListener
                
                val now = Calendar.getInstance()

                // 1. 아직 종료되지 않은 모든 예약 필터링 (대기, 확정 상태 포함)
                val validReservations = mergedListWithRoomDetails.filter { reservation ->
                    val endCal = parseKoreanDateToCalendar(reservation.endTime)
                    endCal != null && now.before(endCal) // 종료 시간이 현재 이후
                }

                // 2. 활성 예약과 예정된 예약으로 분리
                val activeReservations = validReservations.filter { reservation ->
                    val startCal = parseKoreanDateToCalendar(reservation.startTime)
                    // 현재 시간이 시작 시간 이후이거나 같고, 종료 시간 이전인 경우 (위에서 endTime은 이미 필터링됨)
                    startCal != null && !now.before(startCal) 
                }.sortedBy { parseKoreanDateToCalendar(it.startTime)?.time } // 시작 시간 오름차순

                val upcomingReservations = validReservations.filter { reservation ->
                    val startCal = parseKoreanDateToCalendar(reservation.startTime)
                    startCal != null && now.before(startCal) // 시작 시간이 현재보다 미래
                }.sortedBy { parseKoreanDateToCalendar(it.startTime)?.time } // 시작 시간 오름차순
                
                // 3. 현재 표시할 예약 결정
                val currentReservationToDisplay: ReservationData? = 
                    if (activeReservations.isNotEmpty()) {
                        activeReservations.first() // 활성 예약 우선
                    } else if (upcomingReservations.isNotEmpty()) {
                        upcomingReservations.first() // 그 다음으로 가장 임박한 예정 예약
                    } else {
                        null
                    }

                setupCurrentReservationCard(currentReservationToDisplay)

                // 이용 완료 내역은 status가 "종료"인 것만 필터링
                val doneList = mergedListWithRoomDetails.filter { it.status == "종료" }
                    .sortedByDescending { item -> parseKoreanDateToCalendar(item.startTime)?.time }
                setupHistoryList(doneList)
                
                showEmptyState(doneList.isEmpty() && currentReservationToDisplay == null)
                setupPagination() // 페이지네이션은 historyList 기준이므로 위치는 적절
                onUiReady()
            }
            .addOnFailureListener {
                if (_binding == null || !isAdded) return@addOnFailureListener
                showError("강의실 정보를 불러오지 못했습니다.")
                setupCurrentReservationCard(null) // 오류 시 현재 예약 없음 처리
                setupHistoryList(emptyList())   // 오류 시 히스토리 없음 처리
                showEmptyState(true)
                onUiReady()
            }
    }

    private fun setupCurrentReservationCard(currentReservation: ReservationData?) {
        if (_binding == null || !isAdded) return
        
        val cardView = binding.root.findViewById<View>(R.id.cardCurrentReservation)
        val layoutReservationDetails = cardView?.findViewById<LinearLayout>(R.id.layoutReservationDetails_item)
        val tvNoCurrentMessage = cardView?.findViewById<TextView>(R.id.tvNoCurrentReservationMessage_item)

        if (currentReservation == null) {
            layoutReservationDetails?.visibility = View.GONE
            tvNoCurrentMessage?.visibility = View.VISIBLE
            return
        }

        layoutReservationDetails?.visibility = View.VISIBLE
        tvNoCurrentMessage?.visibility = View.GONE

        val imgRoom = layoutReservationDetails?.findViewById<ImageView>(R.id.imgRoom)
        val tvRoomName = layoutReservationDetails?.findViewById<TextView>(R.id.tvRoomName)
        val tvDate = layoutReservationDetails?.findViewById<TextView>(R.id.tvDate)
        val tvTime = layoutReservationDetails?.findViewById<TextView>(R.id.tvTime)
        val btnCancel = layoutReservationDetails?.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = layoutReservationDetails?.findViewById<Button>(R.id.btnConfirm)

        val building = if (currentReservation.building.isNotBlank()) currentReservation.building else "강의동 정보 없음"
        val room = if (currentReservation.roomName.isNotBlank()) currentReservation.roomName else "강의실 정보 없음"
        
        var roomNameText = "$building\n$room".trim()
        // 상태가 "대기"이고 아직 시작 전이면 방 이름에 (대기중) 표시 (선택적)
        // val nowCal = Calendar.getInstance()
        // val startCalForStatus = parseKoreanDateToCalendar(currentReservation.startTime)
        // if (currentReservation.status == "대기" && startCalForStatus != null && nowCal.before(startCalForStatus)) {
        //     roomNameText += " (대기중)" 
        // }

        val roomSpannable = SpannableString(roomNameText)
        roomSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, building.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (building.isNotEmpty() && room.isNotEmpty()) {
           roomSpannable.setSpan(StyleSpan(Typeface.BOLD), building.length + 1, roomSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tvRoomName?.text = roomSpannable

        val startCal = parseKoreanDateToCalendar(currentReservation.startTime)
        val endCal = parseKoreanDateToCalendar(currentReservation.endTime)
        if (startCal != null && endCal != null) {
            val dayOfWeek = getKoreanDayOfWeek(startCal)
            tvDate?.text = "${startCal.get(Calendar.YEAR)}년 ${startCal.get(Calendar.MONTH) + 1}월 ${startCal.get(Calendar.DAY_OF_MONTH)}일 (${dayOfWeek})"
            
            var timeText = "${formatKoreanTime(startCal)} - ${formatKoreanTime(endCal)}"
            // 상태가 "대기"이고 아직 시작 전이면 시간에 (대기중) 표시
            val nowCalCheck = Calendar.getInstance()
            if (currentReservation.status == "대기" && nowCalCheck.before(startCal)) {
                timeText += " (대기중)"
            }
            tvTime?.text = timeText

        } else {
            tvDate?.text = "날짜 정보 없음"
            tvTime?.text = "시간 정보 없음"
        }

        imgRoom?.setImageResource(currentReservation.imageRes)
        btnCancel?.isEnabled = true
        btnConfirm?.isEnabled = true

        btnCancel?.setOnClickListener { 
            // ... 기존 예약 취소 로직 ...
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
                // documentId를 사용하여 직접 삭제 (더 안전하고 명확)
                if (currentReservation.documentId != null) {
                    db.collection("Reservations").document(currentReservation.documentId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                            loadInitialData() // 데이터 새로고침
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "예약 취소 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                     // Document ID가 없는 이전 방식 (여러 필드 일치 확인 - 덜 안전)
                    db.collection("Reservations")
                        .whereEqualTo("userID", userId) // userId는 Fragment 멤버 변수
                        .whereEqualTo("roomID", currentReservation.roomID)
                        .whereEqualTo("startTime", currentReservation.startTime)
                        .whereEqualTo("endTime", currentReservation.endTime)
                        .get()
                        .addOnSuccessListener { result ->
                            val batch = db.batch()
                            for (docInResult in result) {
                                batch.delete(docInResult.reference)
                            }
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                                loadInitialData()
                            }.addOnFailureListener {
                                Toast.makeText(context, "예약 취소 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                             Toast.makeText(context, "예약 정보 조회 중 오류 (취소 실패).", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            dialogView.findViewById<TextView>(R.id.btnDialogNo)?.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        btnConfirm?.setOnClickListener { // 예약 확인 (상세보기) 버튼
            val bottomSheet = ReservationDetailBottomSheet.newInstance(
                room = "${currentReservation.building} ${currentReservation.roomName}".trim(),
                date = currentReservation.startTime.let { startTimeString ->
                    val cal = parseKoreanDateToCalendar(startTimeString)
                    if (cal != null) {
                        val dayOfWeek = getKoreanDayOfWeek(cal)
                        "${cal.get(Calendar.YEAR)}년 ${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일 (${dayOfWeek})"
                    } else {
                        "날짜 정보 없음"
                    }
                },
                time = currentReservation.startTime.let { startTimeString ->
                    val startCalFormat = parseKoreanDateToCalendar(startTimeString)
                    val endCalFormat = parseKoreanDateToCalendar(currentReservation.endTime)
                    if (startCalFormat != null && endCalFormat != null) {
                        var timeDetailText = "${formatKoreanTime(startCalFormat)} - ${formatKoreanTime(endCalFormat)}"
                        // 여기서도 (대기중) 표시 추가 가능
                        if (currentReservation.status == "대기" && Calendar.getInstance().before(startCalFormat)) {
                             timeDetailText += " (대기중)"
                        }
                        timeDetailText
                    } else {
                        "시간 정보 없음"
                    }
                },
                people = (currentReservation.people ?: 0).toString(),
                facilities = currentReservation.equipment.joinToString(", ").ifEmpty { "정보 없음" },
                reserver = userName, // userName은 Fragment 멤버 변수
                imageRes = currentReservation.imageRes
            )
            bottomSheet.show(childFragmentManager, "ReservationDetailBottomSheet")
            val overlayRoomPhoto = view?.findViewById<FrameLayout>(R.id.overlayRoomPhoto)
            overlayRoomPhoto?.visibility = View.VISIBLE
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
    val people: Int? = null,
    val documentId: String? = null
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