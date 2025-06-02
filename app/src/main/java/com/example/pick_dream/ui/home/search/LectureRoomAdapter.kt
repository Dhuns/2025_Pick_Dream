package com.example.pick_dream.ui.home.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import androidx.recyclerview.widget.DiffUtil

data class LectureRoom(
    val id: String = "",
    val name: String = "",
    val buildingName: String = "",
    val buildingDetail: String = "",
    val location: String = "",
    val equipment: List<String> = listOf(),
    val capacity: Int = 0,
    var isFavorite: Boolean = false,
    val isAvailable: Boolean = true
)

sealed class SectionedItem {
    data class Header(val buildingName: String, val buildingDetail: String) : SectionedItem()
    data class Room(val room: LectureRoom) : SectionedItem()
}

class LectureRoomAdapter(
    private var items: List<SectionedItem>,
    private val onItemClick: (LectureRoom) -> Unit,
    private val onFavoriteChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ROOM = 1
    }

    fun updateList(newItems: List<SectionedItem>) {
        val diffCallback = SectionedItemDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvSectionHeader)
        fun bind(header: SectionedItem.Header) {
            tvHeader.text = "${header.buildingName} (${header.buildingDetail})"
        }
    }

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvRoomName)
        private val tvBuilding: TextView = itemView.findViewById(R.id.tvRoomInfo)
        private val btnFavorite: View = itemView.findViewById(R.id.btnFavorite)

        fun bind(item: LectureRoom) {
            tvName.text = item.name
            tvBuilding.text = item.equipment.joinToString(", ").toString()
            btnFavorite as android.widget.ImageButton
            btnFavorite.setImageResource(
                if (item.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
            )
            btnFavorite.setOnClickListener {
                LectureRoomRepository.toggleFavorite(item.name)
                notifyItemChanged(adapterPosition)
                onFavoriteChanged?.invoke()
            }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SectionedItem.Header -> VIEW_TYPE_HEADER
        is SectionedItem.Room -> VIEW_TYPE_ROOM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lecture_room, parent, false)
                RoomViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SectionedItem.Header -> (holder as HeaderViewHolder).bind(item)
            is SectionedItem.Room -> (holder as RoomViewHolder).bind(item.room)
        }
    }

    override fun getItemCount(): Int = items.size
}

class SectionedItemDiffCallback(
    private val oldList: List<SectionedItem>,
    private val newList: List<SectionedItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return when {
            oldItem is SectionedItem.Header && newItem is SectionedItem.Header ->
                oldItem.buildingName == newItem.buildingName && oldItem.buildingDetail == newItem.buildingDetail
            oldItem is SectionedItem.Room && newItem is SectionedItem.Room ->
                oldItem.room.name == newItem.room.name
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}