package com.example.pick_dream.ui.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.Room

class FavoriteRoomsAdapter(private val rooms: List<Room>) :
    RecyclerView.Adapter<FavoriteRoomsAdapter.RoomViewHolder>() {

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgRoom)
        val name: TextView = view.findViewById(R.id.tvBuilding)
        val number: TextView = view.findViewById(R.id.tvRoomNumber)
        val features: TextView = view.findViewById(R.id.tvFacilities)
        val reserveBtn: Button = view.findViewById(R.id.btnReserve)
        val detailBtn: Button = view.findViewById(R.id.btnDetails)
        val btnFavorite: View = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.image.setImageResource(room.imageResId)
        holder.name.text = room.name
        holder.number.text = room.number
        holder.features.text = room.features
        holder.btnFavorite.isSelected = true
        // 클릭 시 하트 토글
        holder.btnFavorite.setOnClickListener {
            holder.btnFavorite.isSelected = !holder.btnFavorite.isSelected
        }
    }

    override fun getItemCount() = rooms.size
}
