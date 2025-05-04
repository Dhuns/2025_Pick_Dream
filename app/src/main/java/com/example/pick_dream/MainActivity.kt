package com.example.pick_dream


import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.pick_dream.R
import com.example.pick_dream.databinding.ActivityMainBinding
import com.example.pick_dream.ui.mypage.MypageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.pickdream.ui.home.HomeFragment
import com.example.pick_dream.ui.FavoriteFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바를 앱 전체의 기본 ActionBar로 설정
        setSupportActionBar(binding.mainToolbar)

        // 기본 Fragment 설정
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, HomeFragment())
            .commit()
        supportActionBar?.title = "홈"

        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, HomeFragment())
                        .commit()
                    supportActionBar?.title = "홈"
                    // 커스텀 타이틀 제거
                    binding.mainToolbar.removeAllViews()
                    true
                }
                R.id.navigation_favorite -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, FavoriteFragment())
                        .commit()
                    // 커스텀 타이틀 중앙에 표시
                    binding.mainToolbar.removeAllViews()
                    val customTitle = LayoutInflater.from(this).inflate(R.layout.toolbar_title, binding.mainToolbar, false)
                    binding.mainToolbar.addView(customTitle)
                    true
                }
                R.id.navigation_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, MypageFragment())
                        .commit()
                    supportActionBar?.title = "마이페이지"
                    // 커스텀 타이틀 제거
                    binding.mainToolbar.removeAllViews()
                    true
                }
                else -> false
            }
        }
    }
}
