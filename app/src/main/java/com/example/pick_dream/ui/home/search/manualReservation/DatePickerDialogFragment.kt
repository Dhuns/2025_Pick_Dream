package com.example.pick_dream.ui.home.search.manualReservation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.pick_dream.R
import java.util.*
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay

class DatePickerDialogFragment(
    private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker, null)
        val spinnerYear = view.findViewById<Spinner>(R.id.spinnerYear)
        val spinnerMonth = view.findViewById<Spinner>(R.id.spinnerMonth)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)

        val years = (2020..2030).toList()
        val months = (1..12).toList()
        spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)
        spinnerMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)

        // 오늘 날짜로 초기화
        val today = Calendar.getInstance()
        val yearIndex = years.indexOf(today.get(Calendar.YEAR))
        val monthIndex = today.get(Calendar.MONTH) // 0부터 시작 (1월=0)
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex, false)
        spinnerMonth.setSelection(monthIndex, false)
        calendarView.selectedDate = CalendarDay.from(today)
        calendarView.setCurrentDate(CalendarDay.from(today))

        // Spinner에서 연/월 선택 시 달력 이동
        fun updateCalendarView() {
            val year = years[spinnerYear.selectedItemPosition]
            val month = spinnerMonth.selectedItemPosition // 0부터 시작
            val calDay = CalendarDay.from(year, month + 1, 1) // day는 1일로
            calendarView.setCurrentDate(calDay)
        }
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendarView() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendarView() }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 달력에서 날짜 선택 시 콜백
        calendarView.setOnDateChangedListener { _, date, _ ->
            onDateSelected(date.year, date.month, date.day)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("취소", null)
            .create()
    }
} 