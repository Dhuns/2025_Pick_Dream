package com.example.pick_dream.ui.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.LectureRoom

class FavoriteRoomsAdapter(
    private var rooms: List<LectureRoom>,
    private val onFavoriteClick: (LectureRoom) -> Unit,
    private val onDetailClick: (LectureRoom) -> Unit,
    private val onReserveClick: (LectureRoom) -> Unit
) : RecyclerView.Adapter<FavoriteRoomsAdapter.RoomViewHolder>() {

    fun updateRooms(newRooms: List<LectureRoom>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomNumberTextView: TextView = itemView.findViewById(R.id.tvRoomNumber)
        private val buildingTextView: TextView = itemView.findViewById(R.id.tvBuilding)
        private val facilitiesTextView: TextView = itemView.findViewById(R.id.tvFacilities)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.btnFavorite)
        private val detailButton: View = itemView.findViewById(R.id.btnDetails)
        private val reserveButton: View = itemView.findViewById(R.id.btnReserve)

        fun bind(room: LectureRoom) {
            roomNumberTextView.text = room.name
            buildingTextView.text = "${room.buildingName} (${room.buildingDetail})"
            facilitiesTextView.text = room.equipment.joinToString(", ")
            
            favoriteButton.setImageResource(R.drawable.ic_heart_filled)

            favoriteButton.setOnClickListener { onFavoriteClick(room) }
            detailButton.setOnClickListener { onDetailClick(room) }
            reserveButton.setOnClickListener { onReserveClick(room) }
        }
    }
}
