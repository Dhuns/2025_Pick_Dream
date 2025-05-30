package com.example.pick_dream.ui.home.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.pick_dream.R
import android.app.Dialog
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.widget.ImageView
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageButton

class ReservationDetailBottomSheet : DialogFragment(R.style.FullScreenDialog) {
    private lateinit var tvInfoAll: TextView
    private var imageRes: Int = R.drawable.sample_room

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        tvInfoAll = view.findViewById(R.id.tv_info_all)
        val ivRoomImage = view.findViewById<ImageView>(R.id.ivRoomImage)
        imageRes = arguments?.getInt(KEY_IMAGE_RES) ?: R.drawable.sample_room
        ivRoomImage.setImageResource(imageRes)
        // 뒤로가기 버튼 클릭 시 닫기
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            dismiss()
        }

        // 전달받은 데이터 표시
        arguments?.let { bundle ->
            val infoList = listOf(
                "강의실" to (bundle.getString(KEY_ROOM) ?: ""),
                "예약일" to (bundle.getString(KEY_DATE) ?: ""),
                "시간" to (bundle.getString(KEY_TIME) ?: ""),
                "인원" to ("총 ${bundle.getString(KEY_PEOPLE)}명"),
                "주요 시설" to (bundle.getString(KEY_FACILITIES) ?: ""),
                "예약자" to (bundle.getString(KEY_RESERVER) ?: "")
            )
            val builder = StringBuilder()
            infoList.forEach { (title, content) ->
                builder.append("$title : $content\n")
            }
            val fullText = builder.toString().trimEnd()
            val spannable = SpannableString(fullText)
            val lines = fullText.lines()
            var start = 0
            for (line in lines) {
                val colonIdx = line.indexOf(':')
                if (colonIdx > 0) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        start + colonIdx,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                start += line.length + 1 // +1 for '\n'
            }
            tvInfoAll.text = spannable
        }
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.6).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.setFragmentResult("close_bottom_sheet", Bundle())
    }

    companion object {
        private const val KEY_ROOM = "room"
        private const val KEY_DATE = "date"
        private const val KEY_TIME = "time"
        private const val KEY_PEOPLE = "people"
        private const val KEY_FACILITIES = "facilities"
        private const val KEY_RESERVER = "reserver"
        private const val KEY_IMAGE_RES = "imageRes"

        fun newInstance(
            room: String,
            date: String,
            time: String,
            people: String,
            facilities: String,
            reserver: String,
            imageRes: Int
        ): ReservationDetailBottomSheet {
            return ReservationDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(KEY_ROOM, room)
                    putString(KEY_DATE, date)
                    putString(KEY_TIME, time)
                    putString(KEY_PEOPLE, people)
                    putString(KEY_FACILITIES, facilities)
                    putString(KEY_RESERVER, reserver)
                    putInt(KEY_IMAGE_RES, imageRes)
                }
            }
        }
    }
} 