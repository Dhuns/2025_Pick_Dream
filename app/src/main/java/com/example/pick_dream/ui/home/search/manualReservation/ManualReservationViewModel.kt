package com.example.pick_dream.ui.home.search.manualReservation

import androidx.lifecycle.ViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay

class ManualReservationViewModel : ViewModel() {
    var selectedDay: CalendarDay? = null
    var startHour: Int? = null
    var startMinute: Int? = null
    var endHour: Int? = null
    var endMinute: Int? = null
    var selectedEquipments: List<String> = emptyList()
}