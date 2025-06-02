package com.example.pick_dream.ui.home.search.manualReservation

import android.content.Context
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import androidx.core.content.ContextCompat
import com.example.pick_dream.R

class PastDayDecorator(context: Context) : DayViewDecorator {
    private val today = CalendarDay.today()
    private val color = ContextCompat.getColor(context, R.color.neutral_300)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.date.compareTo(today.date) < 0
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true)
        view.addSpan(android.text.style.ForegroundColorSpan(color))
    }
} 