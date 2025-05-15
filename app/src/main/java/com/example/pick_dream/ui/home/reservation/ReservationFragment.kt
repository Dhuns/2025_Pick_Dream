package com.example.pick_dream.ui.home.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.databinding.FragmentReservationBinding
import com.example.pick_dream.databinding.ItemReservationCurrentBinding
import com.example.pick_dream.databinding.ItemReservationHistoryBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReservationFragment : Fragment() {
    private var _binding: FragmentReservationBinding? = null
    private val binding get() = _binding!!

    // 임시 데이터
    private val currentReservation = ReservationData(
        imageRes = R.drawable.sample_room,
        roomName = "덕문관 (5강의동)\n5022 강의실",
        date = "2025년 4월 22일 (월)",
        time = "오후 3:00 - 오후 5:00"
    )
    private val historyList = listOf(
        ReservationData(R.drawable.sample_room, "호연관 (9강의동)\n9203 강의실", "2025년 4월 21일 (월)", "오후 3:00 - 오후 5:00"),
        ReservationData(R.drawable.sample_room, "덕문관 (5강의동)\n5105 강의실", "2025년 4월 18일 (금)", "오후 3:00 - 오후 5:00"),
        ReservationData(R.drawable.sample_room, "덕문관 (5강의동)\n5022 강의실", "2025년 4월 17일 (목)", "오후 3:00 - 오후 5:00")
    )

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
        setupCurrentReservationCard()
        setupHistoryList()
        setupPagination()

        // 오버레이 관련 뷰 바인딩
        val overlayRoomPhoto = view.findViewById<android.widget.FrameLayout>(R.id.overlayRoomPhoto)
        val btnOverlayBack = view.findViewById<ImageButton>(R.id.btnOverlayBack)

        // 오버레이 뒤로가기 버튼 클릭 시 바텀시트 닫기
        btnOverlayBack?.setOnClickListener {
            val fragment = childFragmentManager.findFragmentByTag("ReservationDetailBottomSheet")
            if (fragment is ReservationDetailBottomSheet) {
                fragment.dismiss()
            }
            overlayRoomPhoto?.visibility = View.GONE
        }

        // 바텀시트 닫힐 때 오버레이도 GONE 처리
        childFragmentManager.setFragmentResultListener("close_bottom_sheet", viewLifecycleOwner) { _, _ ->
            overlayRoomPhoto?.visibility = View.GONE
        }
    }

    private fun setupCurrentReservationCard() {
        val imgRoom = binding.root.findViewById<ImageView>(R.id.imgRoom)
        val tvRoomName = binding.root.findViewById<TextView>(R.id.tvRoomName)
        val tvDate = binding.root.findViewById<TextView>(R.id.tvDate)
        val tvTime = binding.root.findViewById<TextView>(R.id.tvTime)
        val btnCancel = binding.root.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = binding.root.findViewById<Button>(R.id.btnConfirm)

        imgRoom?.setImageResource(currentReservation.imageRes)
        tvRoomName?.text = currentReservation.roomName
        tvDate?.text = currentReservation.date
        tvTime?.text = currentReservation.time

        btnCancel?.setOnClickListener {
            Toast.makeText(requireContext(), "예약취소 클릭", Toast.LENGTH_SHORT).show()
        }
        btnConfirm?.setOnClickListener {
            // 오버레이 페이드 인
            val overlayRoomPhoto = requireView().findViewById<android.widget.FrameLayout>(R.id.overlayRoomPhoto)
            overlayRoomPhoto?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            overlayRoomPhoto?.alpha = 0f
            overlayRoomPhoto?.visibility = View.VISIBLE
            overlayRoomPhoto?.animate()
                ?.alpha(1f)
                ?.setDuration(300)
                ?.start()

            // 바텀시트 띄우기
            val bottomSheet = ReservationDetailBottomSheet.newInstance(
                room = currentReservation.roomName,
                date = currentReservation.date,
                time = currentReservation.time,
                people = "6",
                facilities = "전자칠판, 콘센트, 와이파이",
                reserver = "김예시 (2024123123)"
            )
            bottomSheet.show(childFragmentManager, "ReservationDetailBottomSheet")
        }
    }

    private fun setupHistoryList() {
        val container = binding.root.findViewById<LinearLayout>(R.id.layoutHistoryList)
        container?.removeAllViews()
        for (item in historyList) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_reservation_history, container, false)
            itemView.findViewById<ImageView>(R.id.imgRoom).setImageResource(item.imageRes)
            itemView.findViewById<TextView>(R.id.tvRoomName).text = item.roomName
            itemView.findViewById<TextView>(R.id.tvDate).text = item.date
            itemView.findViewById<TextView>(R.id.tvTime).text = item.time
            container?.addView(itemView)
        }
    }

    private fun setupPagination() {
        val pageCount = 5
        // 전체 레이아웃에서 layoutPageNumbers만 찾아서 페이지 번호만 동적으로 추가
        val layoutPageNumbers = binding.root.findViewById<LinearLayout>(R.id.layoutPageNumbers)
        layoutPageNumbers?.removeAllViews()
        for (i in 1..pageCount) {
            val tv = TextView(requireContext())
            tv.text = i.toString()
            tv.textSize = 16f
            tv.setPadding(16, 8, 16, 8)
            tv.setTextColor(if (i == 1) 0xFF222222.toInt() else 0xFFB0B0B0.toInt())
            layoutPageNumbers?.addView(tv)
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
}

data class ReservationData(
    val imageRes: Int,
    val roomName: String,
    val date: String,
    val time: String
)

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
        holder.binding.tvRoomName.text = item.roomName
        holder.binding.tvDate.text = item.date
        holder.binding.tvTime.text = item.time
    }

    override fun getItemCount() = items.size
} 