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

class FavoriteRoomsAdapter(private var rooms: List<Room>) :
    RecyclerView.Adapter<FavoriteRoomsAdapter.RoomViewHolder>() {

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgRoom)
        val name: TextView = view.findViewById(R.id.tvBuilding)
        val number: TextView = view.findViewById(R.id.tvRoomNumber)
        val features: TextView = view.findViewById(R.id.tvFacilities)
        val reserveBtn: Button = view.findViewById(R.id.btnReserve)
        val detailBtn: Button = view.findViewById(R.id.btnDetails)
        val btnFavorite: View = view.findViewById(R.id.btnFavorite)
        val capacityView: TextView = view.findViewById(R.id.tvCapacity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.image.setImageResource(R.drawable.sample_room)
        holder.name.text = "${room.id} 강의실"
        holder.number.text = room.location
        holder.features.text = room.equiment.joinToString(", ")
        holder.capacityView.text = "정원: ${room.capacity}명"
        holder.btnFavorite.isSelected = true
        holder.btnFavorite.setOnClickListener {
            holder.btnFavorite.isSelected = !holder.btnFavorite.isSelected
        }
    }

    fun updateRooms(newRooms: List<Room>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    override fun getItemCount() = rooms.size
}
