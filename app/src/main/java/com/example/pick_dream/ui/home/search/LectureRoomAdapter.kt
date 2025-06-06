package com.example.pick_dream.ui.home.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.databinding.ItemLectureRoomBinding
import com.example.pick_dream.databinding.ItemSectionHeaderBinding
import com.example.pick_dream.model.LectureRoom
import com.squareup.picasso.Picasso

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ROOM = 1

class LectureRoomAdapter(
    private var items: List<ListItem>,
    private val onItemClick: (LectureRoom) -> Unit,
    private val onFavoriteClick: (LectureRoom) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.HeaderItem -> VIEW_TYPE_HEADER
            is ListItem.RoomItem -> VIEW_TYPE_ROOM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_ROOM -> {
                val binding = ItemLectureRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                LectureRoomViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerItem = items[position] as ListItem.HeaderItem
                holder.bind(headerItem.buildingName)
            }
            is LectureRoomViewHolder -> {
                val roomItem = items[position] as ListItem.RoomItem
                holder.bind(roomItem.lectureRoom, onItemClick, onFavoriteClick)
            }
        }
    }

    override fun getItemCount(): Int = items.size
    
    fun submitList(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged() // DiffUtil을 사용하면 더 효율적이지만, 단순화를 위해 notifyDataSetChanged 사용
    }

    class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(buildingName: String) {
            binding.tvSectionHeader.text = buildingName
        }
    }

    class LectureRoomViewHolder(private val binding: ItemLectureRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            room: LectureRoom,
            onItemClick: (LectureRoom) -> Unit,
            onFavoriteClick: (LectureRoom) -> Unit
        ) {
            binding.tvRoomName.text = room.name // 헤더에 건물 이름이 있으므로 강의실 이름만 표시
            
            val equipmentText = room.equipment.joinToString(separator = " · ")
            val capacityText = "최대 ${room.capacity}명"
            binding.tvRoomInfo.text = "$capacityText · $equipmentText"

            binding.tvAvailable.text = if(room.isAvailable) "대여 가능" else "대여 불가"
            binding.tvAvailable.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (room.isAvailable) R.color.primary_blue else R.color.Red
                )
            )

            if (!room.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(room.imageUrl).into(binding.ivRoomImage)
            } else {
                binding.ivRoomImage.setImageResource(R.drawable.sample_room)
            }

            if (room.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                binding.btnFavorite.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.Red))
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_border)
                binding.btnFavorite.colorFilter = null
            }
            
            binding.root.setOnClickListener { onItemClick(room) }
            binding.btnFavorite.setOnClickListener { onFavoriteClick(room) }
        }
    }
}