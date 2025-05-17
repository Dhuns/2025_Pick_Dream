package com.example.pick_dream.ui.home.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.pick_dream.R

class ImagePreviewDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGES = "arg_image"
        fun newInstance(imageResList: ArrayList<Int>) = ImagePreviewDialogFragment().apply {
            arguments = bundleOf(ARG_IMAGES to imageResList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 풀스크린 스타일 적용
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.image_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 뒤로가기 아이콘 눌렀을 때 다이얼로그 닫기
        view.findViewById<Toolbar>(R.id.dialogToolbar)
            .setNavigationOnClickListener { dismiss() }

        // 이미지 리스트 가져오기
        val images = requireArguments().getIntegerArrayList(ARG_IMAGES) ?: arrayListOf()
        // ViewPager2 설정
        val pager = view.findViewById<ViewPager2>(R.id.imagePager)
        val adapter = ImagePagerAdapter(images)
        pager.adapter = adapter

        // ① dotContainer 를 LinearLayout 으로 받기
        val dotContainer = view.findViewById<LinearLayout>(R.id.dotContainer)

// ② childCount 만큼 getChildAt 을 호출해서 리스트 생성
        val dots = List(dotContainer.childCount) { index ->
            dotContainer.getChildAt(index)
        }

// ③ 페이지 변화 콜백에서 pos 로 업데이트
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                super.onPageSelected(pos)
                dots.forEachIndexed { i : Int, dot: View ->
                    dot.setBackgroundResource(
                        if (i == pos) R.drawable.dot_selected
                        else R.drawable.dot_unselected
                    )
                }
            }
        })

        // 예약 버튼 클릭
        view.findViewById<Button>(R.id.btnReserveFull)
            .setOnClickListener {
                // 예약 로직 (예: 네비게이션 이동)
                findNavController().navigate(R.id.action_mapsFragment_to_reservationFragment)
                dismiss()
            }
        // X 버튼
        view.findViewById<ImageButton>(R.id.btnClose)
            .setOnClickListener { dismiss() }
    }

}