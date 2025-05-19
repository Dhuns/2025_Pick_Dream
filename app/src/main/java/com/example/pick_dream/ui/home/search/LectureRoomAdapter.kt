package com.example.pick_dream.ui.home.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R

// 강의실 데이터 모델
data class LectureRoom(
    val name: String, // ex: 5022 강의실
    val buildingName: String, // ex: 덕문관
    val buildingDetail: String, // ex: 5강의동
    val roomInfo: String, // ex: 빔프로젝터, 마이크, 콘센트, 스크린
    var isFavorite: Boolean = false
)

// 섹션 헤더와 강의실 아이템을 구분하는 sealed class
sealed class SectionedItem {
    data class Header(val buildingName: String, val buildingDetail: String) : SectionedItem()
    data class Room(val room: LectureRoom) : SectionedItem()
}

class LectureRoomAdapter(
    private val items: List<SectionedItem>,
    private val onItemClick: (LectureRoom) -> Unit,
    private val onFavoriteChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ROOM = 1
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
            tvBuilding.text = item.roomInfo
            btnFavorite as android.widget.ImageButton
            btnFavorite.setImageResource(
                if (item.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
            )
            btnFavorite.setOnClickListener {
                LectureRoomRepository.toggleFavorite(item.name)
                notifyDataSetChanged()
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