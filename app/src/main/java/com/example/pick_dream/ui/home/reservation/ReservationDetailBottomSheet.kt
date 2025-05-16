package com.example.pick_dream.ui.home.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.pick_dream.R
import android.app.Dialog
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface

class ReservationDetailBottomSheet : BottomSheetDialogFragment(R.style.CustomBottomSheetDialog) {
    private lateinit var tvInfoAll: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        tvInfoAll = view.findViewById(R.id.tv_info_all)

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet_rounded)
            dialog.window?.setDimAmount(0f)
        }
        return dialog
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

        fun newInstance(
            room: String,
            date: String,
            time: String,
            people: String,
            facilities: String,
            reserver: String
        ): ReservationDetailBottomSheet {
            return ReservationDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(KEY_ROOM, room)
                    putString(KEY_DATE, date)
                    putString(KEY_TIME, time)
                    putString(KEY_PEOPLE, people)
                    putString(KEY_FACILITIES, facilities)
                    putString(KEY_RESERVER, reserver)
                }
            }
        }
    }
} 