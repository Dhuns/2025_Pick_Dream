package com.example.pick_dream

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //상단바
        setupToolbar()

        // 작성한 후기 버튼 연결
        val reviewButton = findViewById<CardView>(R.id.reviewButtonCard)
        reviewButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            startActivity(intent)
        }
        //문의처 버튼 연결
        val inquiryCard = findViewById<CardView>(R.id.contactButtonCard)  // 문의처 카드 id로!
        inquiryCard.setOnClickListener {
            val intent = Intent(this, InquiryActivity::class.java)
            startActivity(intent)
        }
        //설정 버튼 연결
        val settingCard = findViewById<CardView>(R.id.settingButtonCard)
        settingCard.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val logoutTextView = findViewById<TextView>(R.id.logoutTextView)
        logoutTextView.paintFlags = logoutTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
}

