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
import androidx.fragment.app.activityViewModels

class ManualReservationFragment : Fragment() {
    private val reservationViewModel: ManualReservationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 완전히 새로 진입한 경우 ViewModel 값 초기화
        if (arguments == null || savedInstanceState == null) {
            reservationViewModel.selectedDay = null
            reservationViewModel.startHour = null
            reservationViewModel.startMinute = null
            reservationViewModel.endHour = null
            reservationViewModel.endMinute = null
            reservationViewModel.selectedEquipments = emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manual_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedDay: CalendarDay? = null
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

        // 버튼 상태 업데이트 함수
        fun updateButtonState() {
            val isDateSelected = tvDateSelectTitle.text != "날짜 선택"
            val isTimeSelected = tvTimeSelect.text != "시간 선택"

            btnNext.isEnabled = isDateSelected && isTimeSelected
            btnNext.setBackgroundColor(
                if (isDateSelected && isTimeSelected)
                    ContextCompat.getColor(requireContext(), R.color.primary_400)
                else
                    ContextCompat.getColor(requireContext(), R.color.primary_050)
            )
            btnNext.setTextColor(
                if (isDateSelected && isTimeSelected)
                    ContextCompat.getColor(requireContext(), R.color.white)
                else
                    ContextCompat.getColor(requireContext(), R.color.primary_400)
            )
            btnNext.elevation = 0f
        }

        // 시간 선택 시 버튼 상태 업데이트
        fun updateTimeText() {
            val sh = spinnerStartHour.selectedItem as Int
            val sm = spinnerStartMinute.selectedItem as Int
            val eh = spinnerEndHour.selectedItem as Int
            val em = spinnerEndMinute.selectedItem as Int
            reservationViewModel.startHour = sh
            reservationViewModel.startMinute = sm
            reservationViewModel.endHour = eh
            reservationViewModel.endMinute = em

            val startTotal = sh * 60 + sm
            val endTotal = eh * 60 + em
            val diff = endTotal - startTotal

            val hour = diff / 60
            val min = diff % 60
            val durationText = when {
                diff <= 0 -> ""
                hour > 0 && min > 0 -> " (${hour}시간 ${min}분)"
                hour > 0 -> " (${hour}시간)"
                else -> " (${min}분)"
            }
            tvTimeSelect.text = String.format("%02d:%02d ~ %02d:%02d%s", sh, sm, eh, em, durationText)
            updateButtonState()
        }

        // 예시: arguments로 강의실 정보 전달받아 표시
        val building = arguments?.getString("building") ?: ""
        val roomName = arguments?.getString("roomName") ?: ""
        tvBuilding.text = building
        tvRoomName.text = roomName

        btnNext.setOnClickListener {
            val bundle = Bundle().apply {
                putString("building", building)
                putString("roomName", roomName)
                // 날짜 정보
                selectedDay?.let {
                    putInt("selectedYear", it.year)
                    putInt("selectedMonth", it.month)
                    putInt("selectedDay", it.day)
                }
                // 시간 정보
                putInt("startHour", spinnerStartHour.selectedItem as Int)
                putInt("startMinute", spinnerStartMinute.selectedItem as Int)
                putInt("endHour", spinnerEndHour.selectedItem as Int)
                putInt("endMinute", spinnerEndMinute.selectedItem as Int)
            }
            findNavController().navigate(R.id.action_manualReservationFragment_to_manualReservationInputFragment, bundle)
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 오늘 날짜로 초기화
        val today = java.util.Calendar.getInstance()
        val yearIndex = (2020..2030).toList().indexOf(today.get(java.util.Calendar.YEAR))
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex, false)
        calendarView.selectedDate = CalendarDay.from(today)
        calendarView.setCurrentDate(CalendarDay.from(today))

        // ViewModel 값으로 초기화
        reservationViewModel.selectedDay?.let {
            selectedDay = it
            calendarView.selectedDate = it
            calendarView.setCurrentDate(it)
            val weekDays = arrayOf("일", "월", "화", "수", "목", "금", "토")
            val cal = java.util.Calendar.getInstance()
            cal.set(it.year, it.month, it.day)
            val dayOfWeek = weekDays[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            tvDateSelectTitle.text = String.format("%d년 %d월 %d일(%s)", it.year, it.month + 1, it.day, dayOfWeek)
            spinnerYear.setSelection(it.year - 2020)
        }
        reservationViewModel.startHour?.let { spinnerStartHour.setSelection((9..21).indexOf(it)) }
        reservationViewModel.startMinute?.let { spinnerStartMinute.setSelection(listOf(0,10,20,30,40,50).indexOf(it)) }
        reservationViewModel.endHour?.let { spinnerEndHour.setSelection((9..21).indexOf(it)) }
        reservationViewModel.endMinute?.let { spinnerEndMinute.setSelection(listOf(0,10,20,30,40,50).indexOf(it)) }

        // 오늘 날짜로 상단 연/월 텍스트 초기화
        tvYearSelect.text = "${today.get(java.util.Calendar.YEAR)}년"
        tvMonthSelect.text = "${today.get(java.util.Calendar.MONTH) + 1}월"

        var isUserSelecting = false

        val monthDropdown = MonthDropdownPopup(requireContext())
        tvMonthSelect.setOnClickListener {
            val selectedMonth = tvMonthSelect.text.toString().replace("월", "").toIntOrNull()?.minus(1)
            monthDropdown.show(tvMonthSelect, selectedMonth) { monthIndex ->
                tvMonthSelect.text = "${monthIndex + 1}월"
                val year = tvYearSelect.text.toString().replace("년", "").toIntOrNull() ?: today.get(java.util.Calendar.YEAR)
                calendarView.setCurrentDate(CalendarDay.from(year, monthIndex, 1))
            }
        }
        imgArrowDateHeader.setOnClickListener {
            val selectedMonth = tvMonthSelect.text.toString().replace("월", "").toIntOrNull()?.minus(1)
            monthDropdown.show(tvMonthSelect, selectedMonth) { monthIndex ->
                tvMonthSelect.text = "${monthIndex + 1}월"
                val year = tvYearSelect.text.toString().replace("년", "").toIntOrNull() ?: today.get(java.util.Calendar.YEAR)
                calendarView.setCurrentDate(CalendarDay.from(year, monthIndex, 1))
            }
        }
        // Spinner 선택 시 텍스트 갱신 및 달력 이동
        spinnerYear.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                tvYearSelect.text = "${(2020..2030).toList()[position]}년"
                calendarView.setCurrentDate(CalendarDay.from(position + 2020, 0, 1))
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        // 달력에서 날짜 선택 시 콜백
        selectedDay = calendarView.selectedDate
        calendarView.addDecorator(SundayDecorator(requireContext()) { selectedDay })
        calendarView.addDecorator(SaturdayDecorator(requireContext()) { selectedDay })
        val selectedDayDecorator = SelectedDayDecorator(requireContext()) { selectedDay }
        calendarView.addDecorator(selectedDayDecorator)
        calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDay = date
            reservationViewModel.selectedDay = date
            calendarView.invalidateDecorators()
            isUserSelecting = true
            val weekDays = arrayOf("일", "월", "화", "수", "목", "금", "토")
            val cal = java.util.Calendar.getInstance()
            cal.set(date.year, date.month, date.day)
            val dayOfWeek = weekDays[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            tvDateSelectTitle.text = String.format("%d년 %d월 %d일(%s)", date.year, date.month + 1, date.day, dayOfWeek)
            spinnerYear.setSelection(date.year - 2020)
            isUserSelecting = false
            layoutDateDropdown.visibility = View.GONE
            imgArrowDateHeader.rotation = 0f
            imgArrowDateDropdown.rotation = 0f
            tvDateSelectTitle.visibility = View.VISIBLE
            layoutDateHeader.visibility = View.GONE
            updateButtonState()
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
        val hours = (9..21).toList()
        val minutes = listOf(0, 10, 20, 30, 40, 50)
        spinnerStartHour.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hours)
        spinnerEndHour.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hours)
        spinnerStartMinute.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, minutes)
        spinnerEndMinute.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, minutes)

        // 초기 버튼 상태 설정
        updateButtonState()

        // 시작 시간 또는 시작 분 선택 시 종료 시간/분 동적 제한
        fun updateEndTimeSpinners() {
            val startHour = spinnerStartHour.selectedItem as Int
            val startMinute = spinnerStartMinute.selectedItem as Int
            val startTotal = startHour * 60 + startMinute
            val maxEndTotal = minOf(startTotal + 6 * 60, 21 * 60 + 30)
            val minutes = listOf(0, 10, 20, 30, 40, 50)

            // 종료 시간 후보 시/분 리스트 생성
            val endTimes = mutableListOf<Pair<Int, Int>>()
            for (h in hours) {
                for (m in minutes) {
                    val total = h * 60 + m
                    if (total > startTotal && total <= maxEndTotal && total <= 21 * 60 + 30) {
                        endTimes.add(h to m)
                    }
                }
            }
            val endHours = endTimes.map { it.first }.distinct()
            val endMinutesMap = endHours.associateWith { h -> endTimes.filter { it.first == h }.map { it.second } }

            // 현재 종료 시/분 값 저장
            val prevEndHour = spinnerEndHour.selectedItem as? Int ?: endHours.firstOrNull() ?: hours.first()
            val prevEndMinute = spinnerEndMinute.selectedItem as? Int ?: 0

            // 종료 시 어댑터 갱신
            val endHourAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, endHours)
            spinnerEndHour.adapter = endHourAdapter
            val endHourIndex = endHours.indexOf(prevEndHour).takeIf { it >= 0 } ?: 0
            spinnerEndHour.setSelection(endHourIndex)

            // 종료 분 어댑터 갱신 (종료 시 선택 후에 동기화)
            spinnerEndHour.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedEndHour = endHours[position]
                    val endMinutes = endMinutesMap[selectedEndHour] ?: listOf(0)
                    val endMinuteAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, endMinutes)
                    spinnerEndMinute.adapter = endMinuteAdapter
                    val endMinuteIndex = endMinutes.indexOf(prevEndMinute).takeIf { it >= 0 } ?: 0
                    spinnerEndMinute.setSelection(endMinuteIndex)
                    updateTimeText()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }
        }

        // 시작 시/분 선택 시 종료 시/분 동적 제한
        spinnerStartHour.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                updateEndTimeSpinners()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        spinnerStartMinute.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                updateEndTimeSpinners()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // 최초 진입 시에도 동적 제한 적용
        updateEndTimeSpinners()

        // 종료 시/분 선택 시 시간 텍스트 갱신
        spinnerEndHour.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                // 분 어댑터는 updateEndTimeSpinners에서 이미 갱신됨
                updateTimeText()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        spinnerEndMinute.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                updateTimeText()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

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
        var isProgrammaticChange = false
        layoutEquipmentList.removeAllViews()
        equipmentList.forEach { item ->
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding((8 * resources.displayMetrics.density).toInt(), (4 * resources.displayMetrics.density).toInt(), 0, (4 * resources.displayMetrics.density).toInt())
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val tv = android.widget.TextView(requireContext()).apply {
                text = item
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_600))
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val cb = android.widget.CheckBox(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                scaleX = 1.1f
                scaleY = 1.1f
                buttonTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.checkbox_equipment)
                background = null
            }
            cb.setOnCheckedChangeListener { _, _ ->
                if (isProgrammaticChange) return@setOnCheckedChangeListener
                val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
                tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
                isProgrammaticChange = true
                checkboxSelectAll.isChecked = checkBoxList.all { it.isChecked }
                isProgrammaticChange = false
                // ViewModel에 저장
                reservationViewModel.selectedEquipments = selected
            }
            row.addView(tv)
            row.addView(cb)
            layoutEquipmentList.addView(row)
            checkBoxList.add(cb)
        }
        // ViewModel 값으로 체크박스 상태 복원
        if (reservationViewModel.selectedEquipments.isNotEmpty()) {
            isProgrammaticChange = true
            equipmentList.forEachIndexed { idx, item ->
                checkBoxList[idx].isChecked = reservationViewModel.selectedEquipments.contains(item)
            }
            isProgrammaticChange = false
            val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
            tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
            checkboxSelectAll.isChecked = checkBoxList.all { it.isChecked }
        }
        // 전체 선택 체크박스에도 동일한 스타일 적용
        checkboxSelectAll.apply {
            buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.checkbox_equipment)
            background = null
        }
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticChange) return@setOnCheckedChangeListener
            isProgrammaticChange = true
            checkBoxList.forEach { it.isChecked = isChecked }
            isProgrammaticChange = false
            val selected = equipmentList.filterIndexed { idx, _ -> checkBoxList[idx].isChecked }
            tvEquipmentSelect.text = if (selected.isEmpty()) "사용할 기자재 선택" else selected.joinToString(", ")
            // ViewModel에 저장
            reservationViewModel.selectedEquipments = selected
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

    private fun showMonthPopupMenu(anchor: View) {
        val months = (1..12).map { "${it}월" }
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        months.forEachIndexed { idx, month ->
            popup.menu.add(0, idx, idx, month)
        }
        popup.setOnMenuItemClickListener { item ->
            val monthIndex = item.itemId // 0부터 시작
            // anchor가 TextView일 때만 텍스트 갱신
            if (anchor is TextView) {
                anchor.text = months[monthIndex]
            }
            // 달력 월 이동 등 동기화 코드 추가
            // ... 필요시 추가 ...
            true
        }
        popup.show()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
} 