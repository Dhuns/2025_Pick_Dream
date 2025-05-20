package com.example.pick_dream.ui.home.search.manualReservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pick_dream.R
import android.widget.Button
import androidx.navigation.fragment.findNavController

class ManualReservationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvBuilding = view.findViewById<TextView>(R.id.tvBuildingInfo)
        val tvRoomName = view.findViewById<TextView>(R.id.tvRoomName)
        val btnNext = view.findViewById<Button>(R.id.btnNext)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val tvDateSelect = view.findViewById<TextView>(R.id.tvDateSelect)
        val tvTimeSelect = view.findViewById<TextView>(R.id.tvTimeSelect)
        val tvEquipmentSelect = view.findViewById<TextView>(R.id.tvEquipmentSelect)
        val layoutDateDropdown = view.findViewById<View>(R.id.layoutDateDropdown)
        val spinnerYear = view.findViewById<android.widget.Spinner>(R.id.spinnerYear)
        val spinnerMonth = view.findViewById<android.widget.Spinner>(R.id.spinnerMonth)
        val calendarView = view.findViewById<android.widget.CalendarView>(R.id.calendarView)
        val layoutTimeDropdown = view.findViewById<View>(R.id.layoutTimeDropdown)
        val spinnerStartHour = view.findViewById<android.widget.Spinner>(R.id.spinnerStartHour)
        val spinnerStartMinute = view.findViewById<android.widget.Spinner>(R.id.spinnerStartMinute)
        val spinnerEndHour = view.findViewById<android.widget.Spinner>(R.id.spinnerEndHour)
        val spinnerEndMinute = view.findViewById<android.widget.Spinner>(R.id.spinnerEndMinute)
        val layoutEquipmentDropdown = view.findViewById<View>(R.id.layoutEquipmentDropdown)
        val layoutEquipmentList = view.findViewById<android.widget.LinearLayout>(R.id.layoutEquipmentList)
        val checkboxSelectAll = view.findViewById<android.widget.CheckBox>(R.id.checkboxSelectAll)

        // 예시: arguments로 강의실 정보 전달받아 표시
        val building = arguments?.getString("building") ?: ""
        val roomName = arguments?.getString("roomName") ?: ""
        tvBuilding.text = building
        tvRoomName.text = roomName

        btnNext.setOnClickListener {
            // 다음 단계로 이동 (구현 필요)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 날짜 선택 드롭다운 토글 및 세팅
        val years = (2020..2030).toList()
        val months = (1..12).toList()
        spinnerYear.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)
        spinnerMonth.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)

        fun updateCalendar() {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.YEAR, years[spinnerYear.selectedItemPosition])
            cal.set(java.util.Calendar.MONTH, spinnerMonth.selectedItemPosition)
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendarView.date = cal.timeInMillis
        }
        spinnerYear.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendar() }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        spinnerMonth.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) { updateCalendar() }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            tvDateSelect.text = String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth)
            layoutDateDropdown.visibility = View.GONE
        }
        tvDateSelect.setOnClickListener {
            layoutDateDropdown.visibility = if (layoutDateDropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            layoutTimeDropdown.visibility = View.GONE
            layoutEquipmentDropdown.visibility = View.GONE
        }

        // 시간 선택 드롭다운 토글 및 세팅
        val hours = (0..23).toList()
        val minutes = listOf(0, 15, 30, 45)
        spinnerStartHour.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hours)
        spinnerEndHour.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hours)
        spinnerStartMinute.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, minutes)
        spinnerEndMinute.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, minutes)
        fun updateTimeText() {
            val sh = spinnerStartHour.selectedItem as Int
            val sm = spinnerStartMinute.selectedItem as Int
            val eh = spinnerEndHour.selectedItem as Int
            val em = spinnerEndMinute.selectedItem as Int
            tvTimeSelect.text = String.format("%02d:%02d ~ %02d:%02d", sh, sm, eh, em)
        }
        val timeListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) { updateTimeText() }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        spinnerStartHour.onItemSelectedListener = timeListener
        spinnerStartMinute.onItemSelectedListener = timeListener
        spinnerEndHour.onItemSelectedListener = timeListener
        spinnerEndMinute.onItemSelectedListener = timeListener
        tvTimeSelect.setOnClickListener {
            layoutTimeDropdown.visibility = if (layoutTimeDropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            layoutDateDropdown.visibility = View.GONE
            layoutEquipmentDropdown.visibility = View.GONE
        }

        // 기자재 선택 드롭다운 토글 및 세팅
        val equipmentList = listOf("마이크", "빔 프로젝터", "전자칠판", "스크린", "포인터")
        val checkBoxList = mutableListOf<android.widget.CheckBox>()
        layoutEquipmentList.removeAllViews()
        equipmentList.forEach { item ->
            val cb = android.widget.CheckBox(requireContext())
            cb.text = item
            cb.setOnCheckedChangeListener { _, _ ->
                val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
                tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
                checkboxSelectAll.isChecked = checkBoxList.all { it.isChecked }
            }
            layoutEquipmentList.addView(cb)
            checkBoxList.add(cb)
        }
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            checkBoxList.forEach { it.setOnCheckedChangeListener(null); it.isChecked = isChecked; it.setOnCheckedChangeListener { _, _ ->
                val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
                tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
                checkboxSelectAll.isChecked = checkBoxList.all { it.isChecked }
            } }
            val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
            tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
        }
        tvEquipmentSelect.setOnClickListener {
            layoutEquipmentDropdown.visibility = if (layoutEquipmentDropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            layoutDateDropdown.visibility = View.GONE
            layoutTimeDropdown.visibility = View.GONE
        }
    }
} 