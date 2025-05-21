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
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import androidx.core.content.ContextCompat

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
        val tvDateSelectTitle = view.findViewById<TextView>(R.id.tvDateSelectTitle)
        val tvTimeSelect = view.findViewById<TextView>(R.id.tvTimeSelect)
        val tvEquipmentSelect = view.findViewById<TextView>(R.id.tvEquipmentSelect)
        val layoutDateDropdown = view.findViewById<View>(R.id.layoutDateDropdown)
        val spinnerYear = view.findViewById<android.widget.Spinner>(R.id.spinnerYear)
        val spinnerMonth = view.findViewById<android.widget.Spinner>(R.id.spinnerMonth)
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        val layoutTimeDropdown = view.findViewById<View>(R.id.layoutTimeDropdown)
        val spinnerStartHour = view.findViewById<android.widget.Spinner>(R.id.spinnerStartHour)
        val spinnerStartMinute = view.findViewById<android.widget.Spinner>(R.id.spinnerStartMinute)
        val spinnerEndHour = view.findViewById<android.widget.Spinner>(R.id.spinnerEndHour)
        val spinnerEndMinute = view.findViewById<android.widget.Spinner>(R.id.spinnerEndMinute)
        val layoutEquipmentDropdown = view.findViewById<View>(R.id.layoutEquipmentDropdown)
        val layoutEquipmentList = view.findViewById<android.widget.LinearLayout>(R.id.layoutEquipmentList)
        val checkboxSelectAll = view.findViewById<android.widget.CheckBox>(R.id.checkboxSelectAll)
        val cardDateSelect = view.findViewById<View>(R.id.cardDateSelect)
        val cardTimeSelect = view.findViewById<View>(R.id.cardTimeSelect)
        val cardEquipmentSelect = view.findViewById<View>(R.id.cardEquipmentSelect)
        val imgArrowDateHeader = view.findViewById<android.widget.ImageView>(R.id.imgArrowDateHeader)
        val imgArrowTime = view.findViewById<android.widget.ImageView>(R.id.imgArrowTime)
        val imgArrowEquipment = view.findViewById<android.widget.ImageView>(R.id.imgArrowEquipment)
        val imgArrowDateDropdown = view.findViewById<android.widget.ImageView>(R.id.imgArrowDateDropdown)
        val tvYearSelect = view.findViewById<TextView>(R.id.tvYearSelect)
        val tvMonthSelect = view.findViewById<TextView>(R.id.tvMonthSelect)
        val layoutDateHeader = view.findViewById<View>(R.id.layoutDateHeader)

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
        val months = (1..12).map { "${it}월" }
        val monthAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter
        spinnerMonth.post {
            spinnerMonth.dropDownWidth = spinnerMonth.width
        }

        // 오늘 날짜로 초기화
        val today = java.util.Calendar.getInstance()
        val yearIndex = years.indexOf(today.get(java.util.Calendar.YEAR))
        val monthIndex = today.get(java.util.Calendar.MONTH)
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex, false)
        spinnerMonth.setSelection(monthIndex, false)
        calendarView.selectedDate = CalendarDay.from(today)
        calendarView.setCurrentDate(CalendarDay.from(today))

        // 오늘 날짜로 상단 연/월 텍스트 초기화
        tvYearSelect.text = "${today.get(java.util.Calendar.YEAR)}년"
        tvMonthSelect.text = "${today.get(java.util.Calendar.MONTH) + 1}월"

        var isUserSelecting = false

        // Spinner에서 연/월 선택 시 달력 이동
        fun updateCalendarView() {
            val year = years[spinnerYear.selectedItemPosition]
            val month = spinnerMonth.selectedItemPosition // 0부터 시작 (1월=0)
            val calDay = CalendarDay.from(year, month, 1)
            calendarView.setCurrentDate(calDay)
        }
        // 연/월 텍스트 클릭 시 Spinner 드롭다운 표시
        tvYearSelect.setOnClickListener {
            spinnerYear.performClick()
        }
        tvMonthSelect.setOnClickListener {
            val months = (1..12).map { "${it}월" }.toTypedArray()
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("월 선택")
                .setItems(months) { _, which ->
                    val yearIndex = spinnerYear.selectedItemPosition
                    val year = if (yearIndex >= 0) years[yearIndex] else java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val calDay = CalendarDay.from(year, which, 1)
                    calendarView.setCurrentDate(calDay)
                    tvMonthSelect.text = months[which]
                    spinnerMonth.setSelection(which)
                }
                .show()
        }
        imgArrowDateHeader.setOnClickListener {
            val months = (1..12).map { "${it}월" }.toTypedArray()
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("월 선택")
                .setItems(months) { _, which ->
                    val yearIndex = spinnerYear.selectedItemPosition
                    val year = if (yearIndex >= 0) years[yearIndex] else java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val calDay = CalendarDay.from(year, which, 1)
                    calendarView.setCurrentDate(calDay)
                    tvMonthSelect.text = months[which]
                    spinnerMonth.setSelection(which)
                }
                .show()
        }
        // Spinner 선택 시 텍스트 갱신 및 달력 이동
        spinnerYear.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                tvYearSelect.text = "${years[position]}년"
                updateCalendarView()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        spinnerMonth.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                tvMonthSelect.text = "${months[position]}"
                updateCalendarView()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        // 달력에서 날짜 선택 시 콜백
        calendarView.setOnDateChangedListener { _, date, _ ->
            isUserSelecting = true
            tvDateSelectTitle.text = String.format("%d년 %d월 %d일", date.year, date.month + 1, date.day)
            spinnerYear.setSelection(years.indexOf(date.year))
            spinnerMonth.setSelection(date.month) // 0부터 시작
            isUserSelecting = false
            layoutDateDropdown.visibility = View.GONE
        }
        cardDateSelect.setOnClickListener {
            val isOpen = layoutDateDropdown.visibility != View.VISIBLE
            layoutDateDropdown.visibility = if (isOpen) View.VISIBLE else View.GONE
            layoutTimeDropdown.visibility = View.GONE
            layoutEquipmentDropdown.visibility = View.GONE
            imgArrowDateHeader.rotation = 0f
            imgArrowDateDropdown.rotation = if (isOpen) 180f else 0f
            imgArrowTime.rotation = 0f
            imgArrowEquipment.rotation = 0f
            tvDateSelectTitle.visibility = if (isOpen) View.GONE else View.VISIBLE
            layoutDateHeader.visibility = if (isOpen) View.VISIBLE else View.GONE
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
        cardTimeSelect.setOnClickListener {
            val isOpen = layoutTimeDropdown.visibility != View.VISIBLE
            layoutTimeDropdown.visibility = if (isOpen) View.VISIBLE else View.GONE
            layoutDateDropdown.visibility = View.GONE
            layoutEquipmentDropdown.visibility = View.GONE
            imgArrowTime.rotation = if (isOpen) 180f else 0f
            imgArrowDateHeader.rotation = 0f
            imgArrowEquipment.rotation = 0f
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
        cardEquipmentSelect.setOnClickListener {
            val isOpen = layoutEquipmentDropdown.visibility != View.VISIBLE
            layoutEquipmentDropdown.visibility = if (isOpen) View.VISIBLE else View.GONE
            layoutDateDropdown.visibility = View.GONE
            layoutTimeDropdown.visibility = View.GONE
            imgArrowEquipment.rotation = if (isOpen) 180f else 0f
            imgArrowDateHeader.rotation = 0f
            imgArrowTime.rotation = 0f
        }

        // MaterialCalendarView 커스텀 데코레이터 적용
        calendarView.addDecorator(SundayDecorator(requireContext()))
        calendarView.addDecorator(SaturdayDecorator(requireContext()))
        calendarView.setSelectionColor(ContextCompat.getColor(requireContext(), R.color.primary_500))

        // 요일별 색상 커스텀 WeekDayFormatter 적용
        calendarView.setWeekDayFormatter { dayOfWeek ->
            val text = when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> "일"
                java.util.Calendar.MONDAY -> "월"
                java.util.Calendar.TUESDAY -> "화"
                java.util.Calendar.WEDNESDAY -> "수"
                java.util.Calendar.THURSDAY -> "목"
                java.util.Calendar.FRIDAY -> "금"
                java.util.Calendar.SATURDAY -> "토"
                else -> ""
            }
            val color = when (dayOfWeek) {
                java.util.Calendar.SUNDAY -> ContextCompat.getColor(requireContext(), R.color.Red)
                java.util.Calendar.SATURDAY -> ContextCompat.getColor(requireContext(), R.color.primary_400)
                else -> ContextCompat.getColor(requireContext(), R.color.neutral_500)
            }
            android.text.SpannableString(text).apply {
                setSpan(android.text.style.ForegroundColorSpan(color), 0, text.length, 0)
                setSpan(android.text.style.AbsoluteSizeSpan(16, true), 0, text.length, 0)
            }
        }

        // MaterialCalendarView 상단 타이틀 바 숨기기
        calendarView.post {
            val titleLayout = calendarView.getChildAt(0)
            titleLayout?.visibility = View.GONE
        }

        // 달력의 월이 바뀔 때 상단 연/월 텍스트 동기화
        calendarView.setOnMonthChangedListener { _, date ->
            tvYearSelect.text = "${date.year}년"
            tvMonthSelect.text = "${date.month + 1}월"
        }
    }
} 