package com.example.pick_dream.ui.home.search.manualReservation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.pick_dream.R

class TimePickerDialogFragment(
    private val onTimeSelected: (startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)
        val spinnerStartHour = view.findViewById<Spinner>(R.id.spinnerStartHour)
        val spinnerStartMinute = view.findViewById<Spinner>(R.id.spinnerStartMinute)
        val spinnerEndHour = view.findViewById<Spinner>(R.id.spinnerEndHour)
        val spinnerEndMinute = view.findViewById<Spinner>(R.id.spinnerEndMinute)

        val hours = (0..23).toList()
        val minutes = listOf(0, 15, 30, 45)
        val hourAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hours)
        val minuteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, minutes)
        spinnerStartHour.adapter = hourAdapter
        spinnerEndHour.adapter = hourAdapter
        spinnerStartMinute.adapter = minuteAdapter
        spinnerEndMinute.adapter = minuteAdapter

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("확인") { _, _ ->
                val sh = spinnerStartHour.selectedItem as Int
                val sm = spinnerStartMinute.selectedItem as Int
                val eh = spinnerEndHour.selectedItem as Int
                val em = spinnerEndMinute.selectedItem as Int
                onTimeSelected(sh, sm, eh, em)
            }
            .setNegativeButton("취소", null)
            .create()
    }
} 