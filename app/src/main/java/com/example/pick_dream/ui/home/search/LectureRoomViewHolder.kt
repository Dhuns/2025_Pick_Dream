package com.example.pick_dream.ui.home.search

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R

class LectureRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivRoomImage: ImageView = itemView.findViewById(R.id.ivRoomImage)
    private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
    private val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)
    private val tvAvailable: TextView = itemView.findViewById(R.id.tvAvailable)
    private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)

    fun bind(
        room: LectureRoom,
        isFavorite: Boolean = false,
        onClick: (LectureRoom) -> Unit,
        onFavoriteClick: ((LectureRoom) -> Unit)? = null
    ) {
        ivRoomImage.setImageResource(R.drawable.sample_room)
        tvRoomName.text = "${room.name} (${room.buildingName})"
        if (room.equipment.isNotEmpty()) {
            tvRoomInfo.text = room.equipment.joinToString(", ")
        } else {
            tvRoomInfo.text = "기자재 정보 없음"
        }
        tvAvailable.text = if (room.isAvailable) "대여 가능" else "대여 불가"
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
        )
        itemView.setOnClickListener { onClick(room) }
        btnFavorite.setOnClickListener { onFavoriteClick?.invoke(room) }
    }
}