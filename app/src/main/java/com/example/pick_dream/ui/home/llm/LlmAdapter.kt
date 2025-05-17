package com.example.pick_dream.ui.home.llm

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.databinding.ItemLlmMessageBinding
import com.example.pick_dream.R

class LlmAdapter(private val messages: List<LlmMessage>) :
    RecyclerView.Adapter<LlmAdapter.LlmViewHolder>() {

    inner class LlmViewHolder(val binding: ItemLlmMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LlmViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLlmMessageBinding.inflate(inflater, parent, false)
        return LlmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LlmViewHolder, position: Int) {
        val message = messages[position]
        val context = holder.itemView.context

        holder.binding.textMessage.text = message.text

        if (message.isUser) {
            holder.binding.layoutRoot.gravity = Gravity.END
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_user_bubble)
            holder.binding.textMessage.setTextColor(context.getColor(android.R.color.white))
            holder.binding.imageBotIcon.visibility = android.view.View.GONE
        } else {
            holder.binding.layoutRoot.gravity = Gravity.START
            holder.binding.textMessage.setBackgroundResource(R.drawable.bg_bot_bubble)
            holder.binding.textMessage.setTextColor(context.getColor(android.R.color.black))
            holder.binding.imageBotIcon.visibility = android.view.View.VISIBLE
        }
    }

    override fun getItemCount() = messages.size
}