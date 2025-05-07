package com.example.pickdream.ui.home.notice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView   // ← 이 줄 추가!
import com.example.pick_dream.databinding.ItemNoticeBinding

class NoticeAdapter(
    private var items: List<Notice>,
    private val onClick: (Notice) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvIcon.text = notice.iconEmoji
            binding.tvTitle.text = notice.title
            binding.tvDate.text = notice.date
            binding.root.setOnClickListener { onClick(notice) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoticeBinding.inflate(inflater, parent, false)
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    // 데이터 갱신 함수
    fun updateItems(newItems: List<Notice>) {
        items = newItems
        notifyDataSetChanged()
    }
}