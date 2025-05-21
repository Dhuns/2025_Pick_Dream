package com.example.pick_dream.ui.home.search.manualReservation

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import androidx.core.content.ContextCompat
import com.example.pick_dream.R

class SaturdayDecorator(private val context: Context) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val calendar = day.calendar
        return calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY
    }
    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.primary_400)))
    }
} 