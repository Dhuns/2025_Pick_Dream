package com.example.pick_dream.ui.home.search.manualReservation

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import androidx.core.content.ContextCompat
import com.example.pick_dream.R

class SundayDecorator(
    private val context: Context,
    private val getSelectedDay: () -> CalendarDay?
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val calendar = day.calendar
        val isSunday = calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
        val isSelected = day == getSelectedDay()
        return isSunday && !isSelected
    }
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.Red)))
    }
} 