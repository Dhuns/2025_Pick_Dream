package com.example.pick_dream

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pick_dream.databinding.ActivityMainBinding
import com.example.pick_dream.ui.FavoriteFragment
import com.example.pick_dream.ui.mypage.MypageFragment
import com.example.pickdream.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Navigation Component와 BottomNavigationView 연결
        //val navController = findNavController(R.id.nav_host_fragment_activity_main)
        //binding.navView.setupWithNavController(navController)
        // 툴바를 앱 전체의 기본 ActionBar로 설정
        // setSupportActionBar(binding.mainToolbar)

        // 기본 Fragment 설정
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.nav_host_fragment_activity_main, HomeFragment())
//            .commit()
        // supportActionBar?.title = "홈"

        val navView: BottomNavigationView = binding.navView

        // 초기 아이콘 설정
        navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_filled)
        navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
        navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, HomeFragment())
                        .commit()
                    // 아이콘 변경
                    navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_filled)
                    navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
                    navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)
                    true
                }

                R.id.navigation_favorite -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, FavoriteFragment())
                        .commit()
                    // 아이콘 변경
                    navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home)
                    navView.menu.findItem(R.id.navigation_favorite)
                        .setIcon(R.drawable.ic_favorite_filled)
                    navView.menu.findItem(R.id.navigation_mypage).setIcon(R.drawable.ic_mypage)
                    true
                }

                R.id.navigation_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, MypageFragment())
                        .commit()
                    // 아이콘 변경
                    navView.menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home)
                    navView.menu.findItem(R.id.navigation_favorite).setIcon(R.drawable.ic_favorite)
                    navView.menu.findItem(R.id.navigation_mypage)
                        .setIcon(R.drawable.ic_mypage_filled)
                    true
                }

                else -> false
            }
        }
    }
}