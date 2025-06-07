package com.example.pick_dream

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pick_dream.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import com.example.pick_dream.ui.home.search.LectureRoomRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 앱 시작 시 강의실 및 찜 목록 데이터 미리 로드
        LectureRoomRepository.fetchRooms()
        LectureRoomRepository.fetchFavoriteIds()

        checkClassroomUsageTime()

        val navView: BottomNavigationView = binding.navView

        navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home)
        navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
        navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)

        if (navView.selectedItemId == R.id.navigation_home) {
            navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_filled)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setOnItemSelectedListener { item ->
            navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home)
            navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
            navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)

            when (item.itemId) {
                R.id.navigation_home -> {
                    navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_filled)
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.navigation_favorite -> {
                    navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite_filled)
                    navController.navigate(R.id.favoriteFragment)
                    true
                }
                R.id.navigation_mypage -> {
                    navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage_filled)
                    navController.navigate(R.id.mypageFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun checkClassroomUsageTime() {
        val sharedPrefs = getSharedPreferences("ClassroomPrefs", Context.MODE_PRIVATE)
        val lastEndTime = sharedPrefs.getLong("last_end_time", 0)
        val hasShownReview = sharedPrefs.getBoolean("has_shown_review", false)
        val lastRoomId = sharedPrefs.getString("last_room_id", null)

        val currentTime = System.currentTimeMillis()

        if (lastRoomId != null && lastEndTime > 0 && !hasShownReview && currentTime > lastEndTime) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val navController = navHostFragment.navController
            
            // ReviewFragment로 roomId 전달
            val bundle = Bundle()
            bundle.putString("roomId", lastRoomId)
            navController.navigate(R.id.reviewFragment, bundle)

            with(sharedPrefs.edit()) {
                putBoolean("has_shown_review", true)
                // 선택: 리뷰 창을 보여준 후에는 last_end_time을 초기화하여 중복 실행 방지
                // remove("last_end_time")
                // remove("last_room_id")
                apply()
            }
        }
    }
}
