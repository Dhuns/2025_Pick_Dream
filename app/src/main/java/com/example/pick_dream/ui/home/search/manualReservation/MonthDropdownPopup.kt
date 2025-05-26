package com.example.pick_dream.ui.home.search.manualReservation

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R

class MonthDropdownPopup(
    private val context: Context
) {
    fun show(anchor: View, selectedMonth: Int?, onSelect: (Int) -> Unit) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_month_dropdown, null)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recyclerMonth)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val selected = selectedMonth ?: currentMonth
        val adapter = MonthAdapter(selected) { month ->
            onSelect(month)
            popupWindow?.dismiss()
        }
        recyclerView.adapter = adapter
        val minWidth = (55 * context.resources.displayMetrics.density).toInt()
        val width = maxOf(anchor.width, minWidth)
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val yOffset = (8 * context.resources.displayMetrics.density).toInt()
        popupWindow = PopupWindow(popupView, width, height, true).apply {
            setBackgroundDrawable(ColorDrawable(0x00000000))
            isOutsideTouchable = true
            isFocusable = true
            elevation = 16f
            showAsDropDown(anchor, 0, yOffset)
        }
    }
    private var popupWindow: PopupWindow? = null
    class MonthAdapter(
        val selected: Int,
        val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_month_dropdown, parent, false)
            return MonthViewHolder(v)
        }
        override fun getItemCount() = 12
        override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
            holder.bind(position, position == selected, onClick)
        }
        class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(month: Int, isSelected: Boolean, onClick: (Int) -> Unit) {
                val tv = itemView.findViewById<TextView>(R.id.tvMonth)
                tv.text = "${month + 1}월"
                tv.isSelected = isSelected
                tv.setTextColor(
                    if (isSelected) itemView.context.getColor(R.color.white)
                    else itemView.context.getColor(R.color.neutral_700)
                )
                itemView.setOnClickListener { onClick(month) }
            }
        }
    }
} 