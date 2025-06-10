package com.example.pick_dream.ui.mypage.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.R
import com.example.pick_dream.model.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviews = mutableListOf<Review>()

    fun submitList(list: List<Review>) {
        reviews.clear()
        reviews.addAll(list)
        notifyDataSetChanged()
    }

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val comment: TextView = view.findViewById(R.id.review_comment)
        val rating: RatingBar = view.findViewById(R.id.review_rating)
        val date: TextView = view.findViewById(R.id.review_date)
        val room: TextView = view.findViewById(R.id.review_room)
        val purpose: TextView = view.findViewById(R.id.text_review_purpose)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.comment.text = review.comment
        holder.rating.stepSize = 0.5f
        holder.rating.rating = review.rating
        holder.room.text = "${review.roomID} 강의실"
        
        val purposeAndEquipment = (review.purpose + review.equipment).joinToString(", ")
        holder.purpose.text = "사용 목적 및 기자재 : $purposeAndEquipment"

        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        holder.date.text = review.createdAt?.let { formatter.format(it) } ?: "날짜 없음"

        val ratingScoreText = holder.itemView.findViewById<TextView>(R.id.text_rating_score)
        ratingScoreText.text = String.format("%.1f/5.0", review.rating)
    }

    override fun getItemCount(): Int = reviews.size
}
