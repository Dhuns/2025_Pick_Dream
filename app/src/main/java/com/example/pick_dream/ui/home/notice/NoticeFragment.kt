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


data class Notice(
    val id: String,
    val iconEmoji: String, // "📢" 또는 "🎉"
    val title: String,
    val date: String,
    val content: String
)

// DiffUtil
class NoticeDiffCallback : DiffUtil.ItemCallback<Notice>() {
    override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem == newItem
    }
}

// 어댑터
class NoticeAdapter(
    private val onClick: (Notice) -> Unit
) : ListAdapter<Notice, NoticeAdapter.NoticeViewHolder>(NoticeDiffCallback()) {

    inner class NoticeViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvIcon.text = notice.iconEmoji
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

// 프래그먼트
class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoticeAdapter

    private lateinit var sampleList: List<Notice>
    private lateinit var allNotices: List<Notice>
    private var currentPage = 1
    private val pageSize = 8 // 한 페이지에 보여줄 공지 개수
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

        // 뒤로가기 버튼 클릭 시 HomeFragment로 이동
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

        // 데이터 불러오기
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

//        sampleList = listOf(
//            Notice("1", "📢", "[공지사항] 덕문관 5001 강의실 대여 불가 안내", "25.04.07", "내용1"),
//            Notice("2", "🎉", "[이벤트] 강의실 사용 후기 이벤트 안내", "25.04.02", "내용2"),
//            Notice("3", "📢", "[공지사항] 종합강의동 202 강의실 대여 불가 안내", "25.03.029", "내용3"),
//            Notice("4", "📢", "[공지사항] 호연관 9301 강의실 대여 불가 안내", "25.03.27", "내용4"),
//            Notice("5", "🎉", "[이벤트] 시험기간 픽드림에서 치킨 쏜다~!", "25.03.20", "내용5"),
//            Notice("6", "📢", "[공지사항] 제2공학관 304 강의실 대여 불가 안내", "25.03.20", "내용6"),
//            Notice("7", "📢", "[공지사항] 덕문관 5406 강의실 대여 불가 안내", "25.03.18", "내용7"),
//            Notice("8", "🎉", "[이벤트] 개강 맞이 픽드림 이용 후기 이벤트 안내", "25.03.14", "내용8")
//        allNotices = sampleList
//        totalPages = (allNotices.size + pageSize - 1) / pageSize
//        showPage(currentPage)
//        setupPagination()

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                // 검색 로직: allNotices에서 제목/내용에 query가 포함된 것만 필터링
                val filtered = allNotices.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
                // 페이지네이션을 적용하려면 filtered를 allNotices에 할당하고 페이지/버튼 다시 세팅
                allNotices = filtered
                totalPages = (allNotices.size + pageSize - 1) / pageSize
                currentPage = 1
                showPage(currentPage)
                setupPagination()
            } else {
                // 검색어가 없으면 전체 리스트로 복원
                // sampleList를 어딘가에 저장해두고 복원해야 함
                allNotices = sampleList // sampleList는 전체 데이터
                totalPages = (allNotices.size + pageSize - 1) / pageSize
                currentPage = 1
                showPage(currentPage)
                setupPagination()
            }
        }
    }

    private fun setupPagination() {
        val paginationLayout = binding.paginationLayout
        paginationLayout.removeAllViews()

        // 이전 페이지 버튼
        val prevBtn = createPageButton("<", false) {
            if (currentPage > 1) {
                currentPage--
                showPage(currentPage)
                setupPagination()
            }
        }
        paginationLayout.addView(prevBtn)

        // 페이지 숫자 버튼
        for (i in 1..totalPages) {
            val btn = createPageButton(i.toString(), i == currentPage) {
                currentPage = i
                showPage(currentPage)
                setupPagination()
            }
            paginationLayout.addView(btn)
        }

        // 다음 페이지 버튼
        val nextBtn = createPageButton(">", false) {
            if (currentPage < totalPages) {
                currentPage++
                showPage(currentPage)
                setupPagination()
            }
        }
        paginationLayout.addView(nextBtn)
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