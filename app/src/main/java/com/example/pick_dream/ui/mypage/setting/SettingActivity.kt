package com.example.pick_dream.ui.mypage.setting

import android.os.Bundle
import android.widget.ImageButton
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pick_dream.R

class SettingActivity : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var switch1: SwitchCompat
    private lateinit var switch2: SwitchCompat
    private lateinit var switch3: SwitchCompat
    private lateinit var checkKorean: ImageView
    private lateinit var checkEnglish: ImageView
    private lateinit var checkJapanese: ImageView
    private lateinit var checkChinese: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        // 뷰 초기화
        initializeViews()
        // 이벤트 리스너 설정
        setupListeners()
        // 초기 상태 설정
        setupInitialState()

    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        switch1 = findViewById(R.id.switch1)
        switch2 = findViewById(R.id.switch2)
        switch3 = findViewById(R.id.switch3)
        checkKorean = findViewById(R.id.checkKorean)
        checkEnglish = findViewById(R.id.checkEnglish)
        checkJapanese = findViewById(R.id.checkJapanese)
        checkChinese = findViewById(R.id.checkChinese)
    }

    private fun setupListeners() {
        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }

        // 스위치 리스너
        switch1.setOnCheckedChangeListener { _, isChecked ->
            // 스위치1 상태 변경 처리
            saveSwitchState("switch1", isChecked)
        }

        switch2.setOnCheckedChangeListener { _, isChecked ->
            // 스위치2 상태 변경 처리
            saveSwitchState("switch2", isChecked)
        }

        switch3.setOnCheckedChangeListener { _, isChecked ->
            // 스위치3 상태 변경 처리
            saveSwitchState("switch3", isChecked)
        }

        // 언어 선택 리스너
        findViewById<View>(R.id.langKorean).setOnClickListener {
            setLanguage("ko")
        }

        findViewById<View>(R.id.langEnglish).setOnClickListener {
            setLanguage("en")
        }

        findViewById<View>(R.id.langJapanese).setOnClickListener {
            setLanguage("ja")
        }

        findViewById<View>(R.id.langChinese).setOnClickListener {
            setLanguage("zh")
        }
    }

    private fun setupInitialState() {
        // 저장된 스위치 상태 불러오기
        switch1.isChecked = getSwitchState("switch1")
        switch2.isChecked = getSwitchState("switch2")
        switch3.isChecked = getSwitchState("switch3")

        // 저장된 언어 설정 불러오기
        val currentLang = getCurrentLanguage()
        setLanguage(currentLang)
    }

    private fun saveSwitchState(key: String, isChecked: Boolean) {
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putBoolean(key, isChecked)
            .apply()
    }

    private fun getSwitchState(key: String): Boolean {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean(key, false)
    }

    private fun setLanguage(langCode: String) {
        // 모든 체크 표시 숨기기
        checkKorean.visibility = View.GONE
        checkEnglish.visibility = View.GONE
        checkJapanese.visibility = View.GONE
        checkChinese.visibility = View.GONE

        // 선택된 언어 체크 표시
        when (langCode) {
            "ko" -> checkKorean.visibility = View.VISIBLE
            "en" -> checkEnglish.visibility = View.VISIBLE
            "ja" -> checkJapanese.visibility = View.VISIBLE
            "zh" -> checkChinese.visibility = View.VISIBLE
        }

        // 언어 설정 저장
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("language", langCode)
            .apply()
    }

    private fun getCurrentLanguage(): String {
        return getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "ko") ?: "ko"
    }
}