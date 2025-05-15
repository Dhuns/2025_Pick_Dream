package com.example.pick_dream

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pick_dream.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 강의실 사용 시간이 끝났는지 확인
        checkClassroomUsageTime()

        val navView: BottomNavigationView = binding.navView

        // 초기 아이콘 설정
        navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_filled)
        navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
        navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)

        // 네비게이션 컨트롤러 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // 하단 네비게이션 뷰와 네비게이션 컨트롤러 연결
        navView.setupWithNavController(navController)
    }

    private fun checkClassroomUsageTime() {
        val sharedPrefs = getSharedPreferences("ClassroomPrefs", MODE_PRIVATE)
        val lastEndTime = sharedPrefs.getLong("last_end_time", 0)
        val hasShownReview = sharedPrefs.getBoolean("has_shown_review", false)

        val currentTime = System.currentTimeMillis()

        // 마지막 종료 시간이 있고, 아직 후기를 보여주지 않았다면
        if (lastEndTime > 0 && !hasShownReview && currentTime > lastEndTime) {
            // 후기 페이지로 이동
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.reviewpageFragment)

            // 후기 표시 여부 저장
            sharedPrefs.edit().putBoolean("has_shown_review", true).apply()
        }
    }
}
