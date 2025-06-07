package com.example.pick_dream.ui.home.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.databinding.ItemReservationBinding
import com.example.pick_dream.databinding.ItemSectionHeaderBinding
import com.example.pick_dream.model.Reservation
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_RESERVATION = 1

class ReservationAdapter(
    private val onCancelClick: (Reservation) -> Unit,
    private val onDetailClick: (Reservation) -> Unit,
    private val onWriteReviewClick: (Reservation) -> Unit
) : ListAdapter<ReservationListItem, RecyclerView.ViewHolder>(ReservationDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ReservationListItem.Header -> VIEW_TYPE_HEADER
            is ReservationListItem.ReservationItem -> VIEW_TYPE_RESERVATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_RESERVATION -> {
                val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReservationViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ReservationListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is ReservationListItem.ReservationItem -> (holder as ReservationViewHolder).bind(
                item.reservation,
                item.imageUrl,
                onCancelClick,
                onDetailClick,
                onWriteReviewClick
            )
        }
    }

    class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvSectionHeader.text = title
        }
    }

    class ReservationViewHolder(private val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            reservation: Reservation,
            imageUrl: String?,
            onCancelClick: (Reservation) -> Unit,
            onDetailClick: (Reservation) -> Unit,
            onWriteReviewClick: (Reservation) -> Unit
        ) {
            binding.tvRoomName.text = "${reservation.roomID} 강의실"
            binding.tvDate.text = formatDisplayDate(reservation.startTime)
            binding.tvTime.text = "${formatDisplayTime(reservation.startTime)} - ${formatDisplayTime(reservation.endTime)}"

            val imageRes = when {
                reservation.roomID.startsWith("5") -> R.drawable.p_5kang
                reservation.roomID.startsWith("7") -> R.drawable.p_7kang
                reservation.roomID.startsWith("8") -> R.drawable.p_8kang
                else -> 0
            }

            if (imageRes != 0) {
                binding.imgRoom.setImageResource(imageRes)
            } else if (!imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image_rounded)
                    .error(R.drawable.sample_room)
                    .into(binding.imgRoom)
            } else {
                binding.imgRoom.setImageResource(R.drawable.sample_room)
            }

            val isUpcoming = isUpcoming(reservation.endTime)
            binding.btnCancel.visibility = if (isUpcoming) View.VISIBLE else View.GONE
            binding.btnWriteReview.visibility = if (!isUpcoming) View.VISIBLE else View.GONE
            
            binding.btnCancel.setOnClickListener { onCancelClick(reservation) }
            binding.btnWriteReview.setOnClickListener { onWriteReviewClick(reservation) }
            itemView.setOnClickListener { onDetailClick(reservation) }
        }
    }
}

class ReservationDiffCallback : DiffUtil.ItemCallback<ReservationListItem>() {
    override fun areItemsTheSame(oldItem: ReservationListItem, newItem: ReservationListItem): Boolean {
        return when {
            oldItem is ReservationListItem.ReservationItem && newItem is ReservationListItem.ReservationItem ->
                oldItem.reservation.documentId == newItem.reservation.documentId
            oldItem is ReservationListItem.Header && newItem is ReservationListItem.Header ->
                oldItem.title == newItem.title
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ReservationListItem, newItem: ReservationListItem): Boolean {
         return oldItem == newItem
    }
}

private fun isUpcoming(endTimeStr: String?): Boolean {
    if (endTimeStr.isNullOrEmpty()) return false
    return try {
        val sdf = SimpleDateFormat("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
        val endTime = sdf.parse(endTimeStr)
        endTime?.after(Date()) ?: false
    } catch (e: Exception) {
        false
    }
}

private fun formatDisplayDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
        formatter.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}

private fun formatDisplayTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy년 M월 d일 a h시 m분", Locale.KOREAN)
        val date = parser.parse(dateString)
        val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
        formatter.format(date!!)
    } catch (e: Exception) {
        ""
    }
} 