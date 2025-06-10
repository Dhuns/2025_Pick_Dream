package com.example.pick_dream.ui.home.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.databinding.ItemLectureRoomSelectionBinding
import com.example.pick_dream.model.LectureRoom

class LectureRoomSelectionAdapter(
    private val onItemClick: (LectureRoom) -> Unit
) : ListAdapter<LectureRoom, LectureRoomSelectionAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLectureRoomSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLectureRoomSelectionBinding,
        private val onItemClick: (LectureRoom) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lectureRoom: LectureRoom) {
            binding.tvRoomName.text = lectureRoom.name
            binding.tvCapacity.text = "최대 ${lectureRoom.capacity}명"
            
            val equipmentText = if (lectureRoom.equipment.isNotEmpty()) {
                lectureRoom.equipment.joinToString(", ")
            } else {
                "기자재 정보 없음"
            }
            val floorNumber = lectureRoom.name.substring(0, 2).toIntOrNull()?.let { "${it/10}층" } ?: "층수 정보 없음"
            binding.tvRoomDetail.text = "${floorNumber}\n${equipmentText}"
            
            binding.root.setOnClickListener { onItemClick(lectureRoom) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LectureRoom>() {
            override fun areItemsTheSame(oldItem: LectureRoom, newItem: LectureRoom): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LectureRoom, newItem: LectureRoom): Boolean {
                return oldItem == newItem
            }
        }
    }
} 