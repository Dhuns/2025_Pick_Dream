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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.comment.text = review.comment
        holder.rating.rating = review.rating.toFloat()
        holder.room.text = "강의실 ${review.roomID}"

        // ✅ createdAt: Timestamp → Date → String 변환
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        holder.date.text = review.createdAt?.let { formatter.format(it.toDate()) } ?: "날짜 없음"
    }

    override fun getItemCount(): Int = reviews.size
}
