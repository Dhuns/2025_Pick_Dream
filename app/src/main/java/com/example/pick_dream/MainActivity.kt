package com.example.pick_dream

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
        val sharedPrefs = getSharedPreferences("ClassroomPrefs", MODE_PRIVATE)
        val lastEndTime = sharedPrefs.getLong("last_end_time", 0)
        val hasShownReview = sharedPrefs.getBoolean("has_shown_review", false)

        val currentTime = System.currentTimeMillis()

        if (lastEndTime > 0 && !hasShownReview && currentTime > lastEndTime) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.reviewpageFragment)

            sharedPrefs.edit().putBoolean("has_shown_review", true).apply()
        }
    }
}
