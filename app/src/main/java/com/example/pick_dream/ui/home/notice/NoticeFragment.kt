package com.example.pick_dream.ui.home.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pick_dream.databinding.FragmentNoticeBinding
import com.example.pick_dream.databinding.ItemNoticeBinding

// 데이터 클래스
data class Notice(
    val id: String,
    val title: String,
    val date: String,
    val content: String
)

// 어댑터
class NoticeAdapter(
    private val onClick: (Notice) -> Unit
) : ListAdapter<Notice, NoticeAdapter.NoticeViewHolder>(NoticeDiffCallback()) {

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvTitle.text = notice.title
            binding.root.setOnClickListener { onClick(notice) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// DiffUtil
class NoticeDiffCallback : DiffUtil.ItemCallback<Notice>() {
    override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem == newItem
    }
}

// 프래그먼트
class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoticeAdapter { notice ->
            // TODO: 상세화면 이동 구현
        }

        binding.rvNotice.adapter = adapter

        // 예시 데이터
        val sampleList = listOf(
            Notice("1", "제목1", "2024-06-01", "내용1"),
            Notice("2", "제목2", "2024-06-02", "내용2")
        )
        adapter.submitList(sampleList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}