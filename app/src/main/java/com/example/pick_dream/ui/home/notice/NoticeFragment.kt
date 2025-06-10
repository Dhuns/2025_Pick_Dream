package com.example.pick_dream.ui.home.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.graphics.Typeface
import android.widget.TextView
import com.example.pick_dream.databinding.FragmentNoticeBinding
import com.example.pick_dream.databinding.ItemNoticeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout


data class Notice(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val date: String,
    val content: String
)

class NoticeDiffCallback : DiffUtil.ItemCallback<Notice>() {
    override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem == newItem
    }
}

class NoticeAdapter(
    private val onClick: (Notice) -> Unit
) : ListAdapter<Notice, NoticeAdapter.NoticeViewHolder>(NoticeDiffCallback()) {

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvIcon.text = if (notice.title.contains("[이벤트]")) "🎉" else "📢"
            binding.tvTitle.text = notice.title
            binding.tvDate.text = notice.date
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

class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoticeAdapter

    private lateinit var sampleList: List<Notice>
    private lateinit var allNotices: List<Notice>
    private var currentPage = 1
    private val pageSize = 8
    private var totalPages = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun showPage(page: Int) {
        val fromIndex = (page - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, allNotices.size)
        if (fromIndex < toIndex) {
            val pageList = allNotices.subList(fromIndex, toIndex)
            adapter.submitList(pageList)
        } else {
            adapter.submitList(emptyList())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = NoticeAdapter { notice ->
            val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                title = notice.title,
                date = notice.date,
                content = notice.content
            )
            findNavController().navigate(action)
        }
        binding.rvNotice.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotice.adapter = adapter

        val db = Firebase.firestore
        val formatter = SimpleDateFormat("yy.MM.dd", Locale.getDefault())

        db.collection("Notices")
            .get()
            .addOnSuccessListener { result ->
                allNotices = result.map { doc ->
                    val timestamp = doc.getTimestamp("createdAt")
                    val formattedDate = timestamp?.toDate()?.let { formatter.format(it) } ?: ""

                    Notice(
                        id = doc.id,
                        iconEmoji = "📢",
                        title = doc.getString("title") ?: "",
                        date = formattedDate,
                        content = doc.getString("content") ?: ""
                    )
                }
                totalPages = (allNotices.size + pageSize - 1) / pageSize
                currentPage = 1
                showPage(currentPage)
                setupPagination()
            }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch() {
            val query = binding.etSearch.text.toString().trim()
        val db = Firebase.firestore
        val formatter = SimpleDateFormat("yy.MM.dd", Locale.getDefault())

        db.collection("Notices")
            .get()
            .addOnSuccessListener { result ->
                val originalNotices = result.map { doc ->
                    val timestamp = doc.getTimestamp("createdAt")
                    val formattedDate = timestamp?.toDate()?.let { formatter.format(it) } ?: ""
                    Notice(
                        id = doc.id,
                        iconEmoji = "📢",
                        title = doc.getString("title") ?: "",
                        date = formattedDate,
                        content = doc.getString("content") ?: ""
                    )
                }

                if (query.isNotEmpty()) {
                    val filtered = originalNotices.filter {
                        it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
                }
                allNotices = filtered
            } else {
                    allNotices = originalNotices
                }

                totalPages = (allNotices.size + pageSize - 1) / pageSize
                if (totalPages == 0) totalPages = 1
                currentPage = 1
                showPage(currentPage)
                setupPagination()
        }
    }

    private fun setupPagination() {
        val pagesContainer = binding.pagesContainer
        pagesContainer.removeAllViews()

        binding.tvPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                showPage(currentPage)
                setupPagination()
            }
        }

        // 페이지 번호 생성
        for (i in 1..totalPages) {
            val pageButton = createPageButton(i.toString(), i == currentPage) {
                currentPage = i
                showPage(currentPage)
                setupPagination()
            }
            pagesContainer.addView(pageButton)
        }

        binding.tvNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                showPage(currentPage)
                setupPagination()
            }
        }
    }

    private fun createPageButton(text: String, isSelected: Boolean = false, onClick: () -> Unit): View {
        val tv = TextView(requireContext())
        tv.text = text
        tv.textSize = 16f
        tv.setPadding(8, 0, 8, 0)
        tv.setTextColor(if (isSelected) Color.parseColor("#3C5ABD") else Color.parseColor("#888888"))
        tv.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
        tv.setOnClickListener { onClick() }
        tv.isClickable = true
        tv.isFocusable = true
        return tv
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}