package com.example.pick_dream.ui.home.search

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R

class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvSectionHeader: TextView = itemView.findViewById(R.id.tvSectionHeader)
    fun bind(title: String) {
        tvSectionHeader.text = title
    }
}