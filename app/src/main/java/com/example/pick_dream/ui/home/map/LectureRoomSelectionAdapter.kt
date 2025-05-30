package com.example.pick_dream.ui.home.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.databinding.ItemLectureRoomSelectionBinding

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
            binding.apply {
                tvRoomName.text = lectureRoom.name
                tvCapacity.text = "수용 인원: ${lectureRoom.capacity}명"
                tvRoomDetail.text = "위치: ${lectureRoom.location}"
                
                val equipmentText = lectureRoom.equipment.joinToString(", ")
                tvAvailability.text = if (equipmentText.isNotEmpty()) "구비: $equipmentText" else "구비된 장비 없음"

                root.setOnClickListener {
                    onItemClick(lectureRoom)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LectureRoom>() {
            override fun areItemsTheSame(oldItem: LectureRoom, newItem: LectureRoom): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: LectureRoom, newItem: LectureRoom): Boolean {
                return oldItem == newItem
            }
        }
    }
} 