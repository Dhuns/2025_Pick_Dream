package com.example.pick_dream.ui.home.search.manualReservation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.pick_dream.R
import java.util.*

class DatePickerDialogFragment(
    private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker, null)
        val spinnerYear = view.findViewById<Spinner>(R.id.spinnerYear)
        val spinnerMonth = view.findViewById<Spinner>(R.id.spinnerMonth)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        // 연/월 스피너 세팅
        val years = (2020..2030).toList()
        val months = (1..12).toList()
        spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)
        spinnerMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)

        // 스피너 변경 시 달력 이동
        fun updateCalendar() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, years[spinnerYear.selectedItemPosition])
            cal.set(Calendar.MONTH, spinnerMonth.selectedItemPosition)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            calendarView.date = cal.timeInMillis
        }
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendar() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendar() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 날짜 선택 시 콜백
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            onDateSelected(year, month + 1, dayOfMonth)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("취소", null)
            .create()
    }
} 